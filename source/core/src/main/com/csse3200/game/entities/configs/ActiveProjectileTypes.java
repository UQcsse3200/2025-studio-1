package com.csse3200.game.entities.configs;

/**
 * The different type of behaviours ActiveProjectiles can take
 */
public enum ActiveProjectileTypes {
    /**
     * Projectile affected by gravity, following an arc trajectory.
     */
    ARC,

    /**
     *  Projectile that continuously adjusts its path to follow a target.
     */
    FOLLOW_TARGET;
}
