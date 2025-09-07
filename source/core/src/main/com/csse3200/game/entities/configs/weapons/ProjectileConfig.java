package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.LaserConfig;

/**
 * Defines all projectile configs to be loaded by the projectile Factory.
 */

public class ProjectileConfig {

    public PistolBulletConfig pistolBullet = new PistolBulletConfig();

    // Enemy projectiles
    public static LaserConfig laser = new LaserConfig();
}
