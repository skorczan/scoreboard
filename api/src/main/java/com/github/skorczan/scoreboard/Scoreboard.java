package com.github.skorczan.scoreboard;

import com.github.skorczan.scoreboard.game.Game;
import com.github.skorczan.scoreboard.game.GameObserver;
import com.github.skorczan.scoreboard.game.GameState;
import jakarta.annotation.Nonnull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;

public final class Scoreboard implements Iterable<Game> {

    private static final Comparator<Integer> BUCKETID_COMPARATOR = Comparator.comparingInt(Integer::intValue).reversed();

    private static final Comparator<Game> GAME_COMPARATOR = Comparator.comparing(Game::startedAt);

    private final SortedMap<Integer, SortedSet<Game>> gameBuckets = new ConcurrentSkipListMap<>(BUCKETID_COMPARATOR);

    private final Map<Game, Integer> bucketForGame = new ConcurrentHashMap<>();

    private final GameObserver gameObserver = new GameObserver() {

        @Override
        public void onStateChanged(Game game, GameState newState) {
            moveGameToCorrectBucketWhenNeeded(game);
        }

        @Override
        public void onHomeTeamScoreChanged(Game game, int newHomeTeamScore) {
            moveGameToCorrectBucketWhenNeeded(game);
        }

        @Override
        public void onAwayTeamScoreChanged(Game game, int newAwayTeamScore) {
            moveGameToCorrectBucketWhenNeeded(game);
        }

        private void moveGameToCorrectBucketWhenNeeded(Game game) {
            var oldBucketId = bucketForGame.get(game);
            var newBucketId = gameToBucketId(game);
            bucketForGame.put(game, newBucketId);

            if (!Objects.equals(oldBucketId, newBucketId)) {
                gameBuckets.computeIfPresent(oldBucketId, removeGameFromBucketAndBucketWhenEmpty(game));

                var newBucket = gameBuckets.computeIfAbsent(newBucketId, Scoreboard::newBucket);
                newBucket.add(game);
            }
        }
    };

    public boolean add(Game game) {
        game.addObserver(gameObserver);

        var bucketId = bucketForGame.computeIfAbsent(game, Scoreboard::gameToBucketId);
        var bucket = gameBuckets.computeIfAbsent(bucketId, Scoreboard::newBucket);

        return bucket.add(game);
    }

    public boolean contains(Game game) {
        return bucketForGame.containsKey(game);
    }

    public boolean remove(Game game) {
        game.removeObserver(gameObserver);

        var bucketId = bucketForGame.remove(game);

        if (bucketId != null) {
            gameBuckets.computeIfPresent(bucketId, removeGameFromBucketAndBucketWhenEmpty(game));
            return true;
        } else {
            return false;
        }
    }

    private static int gameToBucketId(Game game) {
        return game.homeTeamScore() + game.awayTeamScore();
    }

    private static SortedSet<Game> newBucket(int bucketId) {
        return new ConcurrentSkipListSet<>(GAME_COMPARATOR);
    }
    private static BiFunction<Integer, SortedSet<Game>, SortedSet<Game>> removeGameFromBucketAndBucketWhenEmpty(Game game) {
        return (id, oldBucket) -> {
            oldBucket.remove(game);

            return oldBucket.isEmpty() ? null : oldBucket;
        };
    }

    @Nonnull
    @Override
    public Iterator<Game> iterator() {
        return new GameIterator(this);
    }

    @Override
    public Spliterator<Game> spliterator() {
        return Iterable.super.spliterator();
    }

    private static final class GameIterator implements Iterator<Game> {

        private final Scoreboard scoreboard;

        private final Iterator<Integer> bucketIds;

        private Iterator<Game> gamesInBucket = null;

        private GameIterator(Scoreboard scoreboard) {
            this.scoreboard = scoreboard;
            this.bucketIds = scoreboard.gameBuckets.sequencedKeySet().iterator();
            this.gamesInBucket = Collections.emptyIterator();
        }

        @Override
        public boolean hasNext() {
            return bucketIds.hasNext() || gamesInBucket.hasNext();
        }

        @Override
        public Game next() {
            if (gamesInBucket.hasNext()) {
                return gamesInBucket.next();
            }

            while (bucketIds.hasNext()) {
                var bucketId = bucketIds.next();
                gamesInBucket = scoreboard.gameBuckets.get(bucketId).iterator();

                if (gamesInBucket.hasNext()) {
                    return gamesInBucket.next();
                }
            }

            throw new NoSuchElementException();
        }
    }
}
