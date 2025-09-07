package com.csse3200.game.entities.configs.projectiles;

import com.csse3200.game.physics.PhysicsLayer;

/**
 * Defines all Pistol Bullet configs to be loaded by the projectile Factory.
 */

public class PistolBulletConfig extends ProjectileConfig {

    public PistolBulletConfig() {
        this.projectileType = PhysicsLayer.FRIENDLY_PROJECTILE;
        this.target = PhysicsLayer.NPC;
        this.baseAttack = 8;
        this.speed = 3f;
        this.texturePath = "images/round.png";
    }
}
