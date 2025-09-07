package com.csse3200.game.entities.configs.projectiles;

import com.csse3200.game.physics.PhysicsLayer;

/**
 * Configuration for the laser shot projectiles
 */
public class LaserConfig extends ProjectileConfig {
    public short projectileType = PhysicsLayer.ENEMY_PROJECTILE;
    public short target = PhysicsLayer.PLAYER;

    public int baseAttack = 5;
    public float speed = 5f;

    public String texturePath = "images/laser_shot.png";
}
