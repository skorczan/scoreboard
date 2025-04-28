package com.github.skorczan.scoreboard.game;


public enum GameState {
    PLANNED {
        @Override
        void start(GameContext context) {
            context.setStartedAt();
            context.setState(RUNNING);
        }

        @Override
        void cancel(GameContext context) {
            context.setCancelledAt();
            context.setState(CANCELLED);
        }
    },
    RUNNING {
        @Override
        void finish(GameContext context) {
            context.setFinishedAt();
            context.setState(FINISHED);
        }

        @Override
        void cancel(GameContext context) {
            context.setCancelledAt();
            context.setState(ABANDONED);
        }

        @Override
        void updateHomeTeamScore(GameContext context, int newScore) {
            context.setHomeTeamScore(newScore);
        }

        @Override
        void updateAwayTeamScore(GameContext context, int newScore) {
            context.setAwayTeamScore(newScore);
        }
    },
    FINISHED,
    CANCELLED,
    ABANDONED;

    void start(GameContext context) {
        throw new UnsupportedOperationException();
    }

    void finish(GameContext context) {
        throw new UnsupportedOperationException();
    }

    void cancel(GameContext context) {
        throw new UnsupportedOperationException();
    }

    void updateHomeTeamScore(GameContext context, int newScore) {
        throw new UnsupportedOperationException();
    }

    void updateAwayTeamScore(GameContext context, int newScore) {
        throw new UnsupportedOperationException();
    }
}
