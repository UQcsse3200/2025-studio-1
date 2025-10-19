package com.csse3200.game.lighting;

import com.badlogic.gdx.graphics.Color;

public final class LightingDefaults {
    private LightingDefaults() {}

    // Engine
    public static final int   BLUR_NUM      = 3;

    // LightingDefaults
    public static final float AMBIENT_LIGHT = 0.70f; // for global default; you can override per-room as above
    public static final int   RAYS     = 96;         // decent perf/quality
    public static final float DIST     = 18f;        // longer reach in a big room
    public static final float CONE_DEG = 60f;        // wider downlights

    // Directions
    public static final float RIGHT = 0f, UP = 90f, LEFT = 180f, DOWN = -90f;

    // Colours
    public static final Color NORMAL_COLOR = new Color(1f, 0.92f, 0.75f, 0.85f); // warm bulb
    public static final Color DETECTED_COLOR = Color.RED;

    // Bonus: neon accents (use in your LightFactory calls)
    public static final Color NEON_MAGENTA = new Color(1f, 0.20f, 0.75f, 0.90f);
    public static final Color NEON_CYAN    = new Color(0.15f, 0.95f, 1f, 0.90f);

    // Security occlusion (only if you use detectors)
    public static final short OCCLUDER = com.csse3200.game.physics.PhysicsLayer.OBSTACLE;

    // Simple sweep
    public static final float START_DEG = 220f;
    public static final float END_DEG   = 320f;
    public static final float ANGULAR_VEL = 25f; // deg/s; skip accel unless you want easing
}