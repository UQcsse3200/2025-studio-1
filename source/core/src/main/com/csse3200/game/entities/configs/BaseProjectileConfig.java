package com.csse3200.game.entities.configs;

/**
 * Base class for all configurations of projectiles
 */
public class BaseProjectileConfig {
    public int base_attack = 1;
    public float speed = 1f;

    //High health to ensure bullet does not "die" before hitting target
    public int health = 10000;
}
