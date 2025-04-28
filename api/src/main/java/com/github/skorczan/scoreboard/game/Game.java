package com.github.skorczan.scoreboard.game;

import com.github.skorczan.scoreboard.team.Team;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Accessors(fluent = true)
public final class Game extends GameContext implements GameSummary {

    private static final VarHandle HOME_TEAM_SCORE;

    private static final VarHandle AWAY_TEAM_SCORE;

    static {
        try {
            var lookup = MethodHandles.lookup();

            HOME_TEAM_SCORE = lookup.findVarHandle(Game.class, "homeTeamScore", int.class);
            AWAY_TEAM_SCORE = lookup.findVarHandle(Game.class, "awayTeamScore", int.class);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("fields homeTeamScore and awayTeamScore are declared in Game class", ex);
        }
    }

    @Getter(AccessLevel.NONE)
    private final Clock clock;

    private final Team homeTeam;

    private final Team awayTeam;

    private final Set<GameObserver> observers = new LinkedHashSet<>();

    private volatile GameState state;

    private volatile int homeTeamScore;

    private volatile int awayTeamScore;

    private volatile Instant plannedAt;

    private volatile Instant startedAt;

    private volatile Instant finishedAt;

    private volatile Instant cancelledAt;

    @Builder
    private Game(Team homeTeam, Team awayTeam, int homeTeamScore, int awayTeamScore,
                 Instant plannedAt, Instant startedAt, Instant finishedAt, Instant cancelledAt, Clock clock) {
        if (homeTeam != null) {
            this.homeTeam = homeTeam;
        } else {
            throw new IllegalArgumentException("homeTeam must be provided");
        }

        if (awayTeam != null) {
            this.awayTeam = awayTeam;
        } else {
            throw new IllegalArgumentException("awayTeam must be provided");
        }

        if (startedAt != null && finishedAt != null && startedAt.isAfter(finishedAt)) {
            throw new IllegalArgumentException("finishedAt can't be sooner than startedAt");
        }

        if (startedAt != null && cancelledAt != null && startedAt.isAfter(cancelledAt)) {
            throw new IllegalArgumentException("cancelledAt can't be sooner than startedAt");
        }

        if (finishedAt != null && cancelledAt != null) {
            throw new IllegalArgumentException("finishedAt and cancelledAt can't be set at the same time");
        }

        this.state = selectStateForGivenInstants(startedAt, finishedAt, cancelledAt);
        this.homeTeamScore = homeTeamScore;
        this.awayTeamScore = awayTeamScore;

        this.plannedAt = plannedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.cancelledAt = cancelledAt;

        this.clock = Objects.requireNonNullElseGet(clock, Clock::systemUTC);
    }

    public Game start() {
        state.start(this);
        return this;
    }

    public Game homeTeamScore(int newHomeTeamScore) {
        state.updateHomeTeamScore(this, newHomeTeamScore);
        return this;
    }

    public Game homeTeamScoreIncreaseBy(int pointsGained) {
        state.updateHomeTeamScoreByPoints(this, pointsGained);
        return this;
    }

    public Game awayTeamScore(int newAwayTeamScore) {
        state.updateAwayTeamScore(this, newAwayTeamScore);
        return this;
    }

    public Game awayTeamScoreIncreaseBy(int pointsGained) {
        state.updateAwayTeamScoreByPoints(this, pointsGained);
        return this;
    }

    public Game finish() {
        state.finish(this);
        return this;
    }

    public Game cancel() {
        state.cancel(this);
        return this;
    }

    public Game addObserver(GameObserver gameObserver) {
        this.observers.add(gameObserver);
        return this;
    }

    public Game removeObserver(GameObserver gameObserver) {
        this.observers.remove(gameObserver);
        return this;
    }

    @Override
    protected void setState(GameState gameState) {
        this.state = gameState;

        for (var observer: observers) {
            observer.onStateChanged(this, gameState);
        }
    }

    @Override
    protected void setStartedAt() {
        this.startedAt = clock.instant();
    }

    @Override
    protected void setFinishedAt() {
        this.finishedAt = clock.instant();
    }

    @Override
    protected void setCancelledAt() {
        this.cancelledAt = clock.instant();
    }

    @Override
    protected void setHomeTeamScore(int newScore) {
        HOME_TEAM_SCORE.setVolatile(this, newScore);

        for (var observer: observers) {
            observer.onHomeTeamScoreChanged(this, newScore);
        }
    }

    @Override
    protected void increaseHomeTeamScoreBy(int gainedPoints) {
        var previousScore = (int) HOME_TEAM_SCORE.getAndAdd(this, gainedPoints);
        var currentScore = previousScore + gainedPoints;

        for (var observer: observers) {
            observer.onHomeTeamScoreChanged(this, currentScore);
        }
    }

    @Override
    protected void setAwayTeamScore(int newScore) {
        AWAY_TEAM_SCORE.setVolatile(this, newScore);

        for (var observer: observers) {
            observer.onAwayTeamScoreChanged(this, newScore);
        }
    }

    @Override
    protected void increaseAwayTeamScoreBy(int gainedPoints) {
        var previousScore = (int) AWAY_TEAM_SCORE.getAndAdd(this, gainedPoints);
        var currentScore = previousScore + gainedPoints;

        for (var observer: observers) {
            observer.onAwayTeamScoreChanged(this, currentScore);
        }
    }

    static GameState selectStateForGivenInstants(Instant startedAt, Instant finishedAt, Instant cancelledAt) {
        int state = (startedAt != null ? 0b100 : 0) |
                (finishedAt != null ? 0b010 : 0) |
                (cancelledAt != null ? 0b001 : 0);

        return switch (state) {
            case 0b000 -> GameState.PLANNED;
            case 0b001 -> GameState.CANCELLED;
            case 0b100 -> GameState.RUNNING;
            case 0b101 -> GameState.ABANDONED;
            case 0b110 -> GameState.FINISHED;

            default -> throw new IllegalStateException();
        };
    }
}
