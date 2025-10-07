package com.csse3200.game.components.minigames.pool.logic;

public interface RulesEvents {
    void onTurnChanged(int currentPlayer, int p1Score, int p2Score);

    void onScoreUpdated(int currentPlayer, int p1Score, int p2Score);

    void onFoul(int foulingPlayer, String reason);
}