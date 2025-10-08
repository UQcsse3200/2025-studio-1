package com.csse3200.game.components.minigames.pool.logic;

/**
 * Defines callbacks for pool game rule-related events.
 * <p>
 * Implementations of this interface handle updates to the UI or game
 * state when turns change, scores update, or fouls occur.
 */
public interface RulesEvents {

    /**
     * Called when the active player's turn changes.
     *
     * @param currentPlayer the player whose turn it is now (1 or 2)
     * @param p1Score       current score for player 1
     * @param p2Score       current score for player 2
     */
    void onTurnChanged(int currentPlayer, int p1Score, int p2Score);

    /**
     * Called when either player's score changes.
     *
     * @param currentPlayer the player whose turn triggered the score change
     * @param p1Score       updated score for player 1
     * @param p2Score       updated score for player 2
     */
    void onScoreUpdated(int currentPlayer, int p1Score, int p2Score);

    /**
     * Called when a foul occurs, such as a cue ball scratch.
     *
     * @param foulingPlayer the player who committed the foul
     * @param reason        short description of the foul reason
     */
    void onFoul(int foulingPlayer, String reason);
}