package com.csse3200.game.entities.configs.projectiles;

import com.csse3200.game.physics.PhysicsLayer;

/**
 * Defines all Pistol Bullet configs to be loaded by the projectile Factory.
 */

public class PistolBulletConfig extends ProjectileConfig {
    public short projectileType = PhysicsLayer.FRIENDLY_PROJECTILE;
    public short target = PhysicsLayer.NPC;
    public int baseAttack = 8;
    public float speed = 3f;

    public String texturePath = "images/round.png";
}
