package com.csse3200.game.components.minigames.pool.logic;

import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.minigames.pool.physics.BallFactory;

/**
 * Defines the contract for all pool rule sets.
 * <p>
 * A {@code RuleSet} manages turn logic, fouls, scoring, and shot state.
 * Implementations handle how the game responds to events such as shots,
 * scratches, and potted balls.
 */
public interface RuleSet {

    /**
     * Called when a new rack begins.
     * Resets all balls and cue positions, and prepares internal state.
     *
     * @param balls the {@link BallFactory} instance managing all pool balls
     */
    void onNewRack(BallFactory balls);

    /**
     * Called when the player takes a shot.
     * Applies the given impulse direction and power to the cue ball.
     *
     * @param cue   the cue ball body
     * @param dx    x-direction component of the shot
     * @param dy    y-direction component of the shot
     * @param power shot power in range [0..1]
     */
    void onShoot(Body cue, float dx, float dy, float power);

    /**
     * Called when a ball (object or cue) is potted.
     *
     * @param ballId      ID of the ball that was potted (0 = cue ball)
     * @param pocketIndex index of the pocket the ball entered
     */
    void onBallPotted(int ballId, int pocketIndex);

    /**
     * Called when the cue ball is scratched (potted).
     *
     * @param pocketIndex index of the pocket where the cue ball fell
     */
    void onScratch(int pocketIndex);

    /**
     * Updates the rule state once per frame.
     * Detects when motion stops and advances turns accordingly.
     */
    void updateTurn();

    /**
     * Sets the listener used to notify other systems (UI, host)
     * about turn changes, scoring updates, and fouls.
     *
     * @param listener a {@link RulesEvents} listener instance
     */
    void setListener(RulesEvents listener);

    /**
     * Checks whether a shot is currently in progress.
     *
     * @return {@code true} if a shot is active, otherwise {@code false}
     */
    boolean isShotActive();
}