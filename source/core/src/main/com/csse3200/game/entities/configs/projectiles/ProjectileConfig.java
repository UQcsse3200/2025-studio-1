package com.csse3200.game.entities.configs.projectiles;

import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.physics.PhysicsLayer;

/**
 * Defines all projectile configs to be loaded by the projectile Factory.
 */

public class ProjectileConfig {

    public enum ProjectileTarget {
        PLAYER,
        ENEMY
    }

    public ItemTypes itemType = ItemTypes.PROJECTILE;

    public short projectileType;
    public short target;
//    public int baseAttack;
//    public float speed;

    public String texturePath;

    public ProjectileConfig(ProjectileTarget target, String texturePath) {
        switch (target) {
            case PLAYER -> {
                    this.projectileType = PhysicsLayer.FRIENDLY_PROJECTILE;
                    this.target = PhysicsLayer.PLAYER;
            }
            case ENEMY -> {
                    this.projectileType = PhysicsLayer.ENEMY_PROJECTILE;
                    this.target = PhysicsLayer.NPC;
            }
        }
        this.texturePath = texturePath;
    }
}
