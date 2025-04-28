package com.github.skorczan.scoreboard.game;

import com.github.skorczan.scoreboard.team.Team;

public interface GameSummary {

    GameState state();

    Team homeTeam();

    Team awayTeam();

    int homeTeamScore();

    int awayTeamScore();
}
