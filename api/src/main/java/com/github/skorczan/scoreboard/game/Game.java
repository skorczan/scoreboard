package com.github.skorczan.scoreboard.game;

import com.github.skorczan.scoreboard.team.Team;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class Game extends GameContext implements GameSummary {

    @Getter(AccessLevel.NONE)
    private final Clock clock;

    private final Team homeTeam;

    private final Team awayTeam;

    private volatile GameState state;

    private volatile int homeTeamScore;

    private volatile int awayTeamScore;

    private Instant plannedAt;

    private Instant startedAt;

    private Instant finishedAt;

    private Instant cancelledAt;

    private GameObserver gameObserver;

    @Builder
    private Game(Clock clock, Team homeTeam, Team awayTeam, int homeTeamScore, int awayTeamScore,
                 Instant plannedAt, Instant startedAt, Instant finishedAt, Instant cancelledAt, GameObserver gameObserver) {

        if (clock != null) {
            this.clock = clock;
        } else {
            throw new IllegalArgumentException("clock must be provided");
        }

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

        this.gameObserver = Objects.requireNonNullElse(gameObserver, GameObserver.NOOP);
    }

    public Game start() {
        state.start(this);
        return this;
    }

    public Game homeTeamScore(int newHomeTeamScore) {
        state.updateHomeTeamScore(this, newHomeTeamScore);
        return this;
    }

    public Game awayTeamScore(int newAwayTeamScore) {
        state.updateAwayTeamScore(this, newAwayTeamScore);
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

    @Override
    protected void setState(GameState gameState) {
        this.state = gameState;
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
        this.homeTeamScore = newScore;
    }

    @Override
    protected void setAwayTeamScore(int newScore) {
        this.awayTeamScore = newScore;
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
