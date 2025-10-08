package com.csse3200.game.components.minigames.pool.logic;

/**
 * Event constants for all pool minigame systems (logic + UI).
 */
public final class GameEvents {
    // Game lifecycle
    public static final String START = "pool:start";
    public static final String RESET = "pool:reset";
    public static final String STOP = "pool:stop";

    // Gameplay events
    public static final String POTTED = "pool:potted";   // (id, pocket)
    public static final String SCRATCH = "pool:scratch";  // (pocket)

    // UI and scoring events
    public static final String TURN = "pool:turn";
    public static final String SCORE = "pool:score";
    public static final String FOUL = "pool:foul";
    public static final String SHOOT = "pool:shoot";
    public static final String INTERACT = "interact";

    private GameEvents() {
    }
}