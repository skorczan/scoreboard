package com.github.skorczan.scoreboard.game;


abstract class GameContext {

    protected abstract void setState(GameState gameState);

    protected abstract void setStartedAt();

    protected abstract void setFinishedAt();

    protected abstract void setCancelledAt();

    protected abstract void setHomeTeamScore(int newScore);

    protected abstract void increaseHomeTeamScoreBy(int gainedPoints);

    protected abstract void setAwayTeamScore(int newScore);

    protected abstract void increaseAwayTeamScoreBy(int gainedPoints);
}
