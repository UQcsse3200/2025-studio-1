package com.csse3200.game.components.minigames.pool.logic;

public final class GameEvents {
    private GameEvents(){}
    public static final String START = "pool:start";
    public static final String RESET = "pool:reset";
    public static final String STOP  = "pool:stop";
    public static final String POTTED = "pool:potted";   // (id, pocket)
    public static final String SCRATCH = "pool:scratch";  // (pocket)
}
