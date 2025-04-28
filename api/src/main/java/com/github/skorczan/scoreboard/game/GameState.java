package com.github.skorczan.scoreboard.game;


public enum GameState {
    PLANNED {
        @Override
        void start(GameContext context) {
            context.setState(RUNNING);
            context.setStartedAt();
        }

        @Override
        void cancel(GameContext context) {
            context.setState(CANCELLED);
            context.setCancelledAt();
        }
    },
    RUNNING {
        @Override
        void finish(GameContext context) {
            context.setState(FINISHED);
            context.setFinishedAt();
        }

        @Override
        void cancel(GameContext context) {
            context.setState(ABANDONED);
            context.setCancelledAt();
        }

        @Override
        void updateHomeTeamScore(GameContext context, int newScore) {
            context.setHomeTeamScore(newScore);
        }


        @Override
        void updateHomeTeamScoreByPoints(GameContext context, int gainedPoints) {
            context.increaseHomeTeamScoreBy(gainedPoints);
        }

        @Override
        void updateAwayTeamScore(GameContext context, int newScore) {
            context.setAwayTeamScore(newScore);
        }

        @Override
        void updateAwayTeamScoreByPoints(GameContext context, int gainedPoints) {
            context.increaseAwayTeamScoreBy(gainedPoints);
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

    void updateHomeTeamScoreByPoints(GameContext context, int gainedPoints) {
        throw new UnsupportedOperationException();
    }

    void updateAwayTeamScore(GameContext context, int newScore) {
        throw new UnsupportedOperationException();
    }

    void updateAwayTeamScoreByPoints(GameContext context, int gainedPoints) {
        throw new UnsupportedOperationException();
    }
}
