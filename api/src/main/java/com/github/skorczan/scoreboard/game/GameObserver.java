package com.github.skorczan.scoreboard.game;

public interface GameObserver {

    void onStateChanged(Game game, GameState newState);

    void onHomeTeamScoreChanged(Game game, int newHomeTeamScore);

    void onAwayTeamScoreChanged(Game game, int newAwayTeamScore);

    GameObserver NOOP = new GameObserver() {
        @Override
        public void onStateChanged(Game game, GameState newState) {
            // this method is empty on purpose
        }

        @Override
        public void onHomeTeamScoreChanged(Game game, int newHomeTeamScore) {
            // this method is empty on purpose
        }

        @Override
        public void onAwayTeamScoreChanged(Game game, int newAwayTeamScore) {
            // this method is empty on purpose
        }
    };
}
