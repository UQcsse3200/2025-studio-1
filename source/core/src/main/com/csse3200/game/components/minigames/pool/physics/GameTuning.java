package com.csse3200.game.components.minigames.pool.physics;

/**
 * Contains global tuning constants for the pool minigame physics.
 * <p>
 * These values control overall gameplay responsiveness and simulation timing.
 * This class is non-instantiable.
 */
public final class GameTuning {

    /**
     * Maximum impulse magnitude that can be applied to the cue ball.
     * Controls shot strength scaling across the game.
     */
    public static final float MAX_IMPULSE = 5f;

    /**
     * Time interval (in seconds) between physics synchronization steps.
     * Approximately 30 updates per second.
     */
    public static final float SYNC_PERIOD = 0.033f; // ~30Hz

    /**
     * Private constructor to prevent instantiation.
     */
    private GameTuning() {
    }
}