package com.github.skorczan.scoreboard.game;

public interface GameObserver {

    void onStateChanged(Game game, GameState newState);

    void onHomeTeamScoreChanged(Game game, int newHomeTeamScore);

    void onAwayTeamScoreChanged(Game game, int newAwayTeamScore);
}
