package com.csse3200.game.entities.configs;

/**
 * Defines all Pistol Bullet configs to be loaded by the projectile Factory.
 */

public class PistolBulletConfig {

    public int base_attack = 8;
    //High health to ensure bullet does not "die" before hitting target
    public int health = 10000;
    public float speed = 3f;
}
