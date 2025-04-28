package com.github.skorczan.scoreboard;

import com.github.skorczan.scoreboard.game.Game;
import com.github.skorczan.scoreboard.team.Team;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class ScoreboardManager {

    private final Clock clock;

    private final Scoreboard scoreboard;

    @Nullable
    public Game getGame(Team homeTeam, Team awayTeam) {
        for (var game: scoreboard) {
            if (Objects.equals(game.homeTeam(), homeTeam) && Objects.equals(game.awayTeam(), awayTeam)) {
                return game;
            }
        }

        return null;
    }

    @Nonnull
    public Game startGame(Team homeTeam, Team awayTeam) {
        var game = getGame(homeTeam, awayTeam);

        if (game == null) {
            game = newGame(homeTeam, awayTeam);
        } else {
            throw new IllegalStateException();
        }

        scoreboard.add(game);
        return game;
    }

    private Game newGame(Team homeTeam, Team awayTeam) {
        return Game.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .homeTeamScore(0)
                .awayTeamScore(0)
                .clock(clock)
                .startedAt(clock.instant())
                .build();
    }

    @Nonnull
    public Game updateScore(Team homeTeam, Team awayTeam, int homeTeamScore, int awayTeamScore) {
        var game = getGame(homeTeam, awayTeam);

        if (game == null) {
            throw new IllegalStateException();
        }

        return game.homeTeamScore(homeTeamScore).awayTeamScore(awayTeamScore);
    }

    @Nullable
    public Game finishGame(Team homeTeam, Team awayTeam) {
        var game = getGame(homeTeam, awayTeam);

        if (game == null) {
            throw new IllegalStateException();
        }

        return game.finish();
    }
}
