package com.csse3200.game.entities.configs.projectiles;

import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.physics.PhysicsLayer;

/**
 * Defines all projectile configs to be loaded by the projectile Factory.
 */

public class ProjectileConfig {

    public static final ItemTypes itemType = ItemTypes.PROJECTILE;

    public final short projectileType;
    public final short target;

    public final String texturePath;

    /**
     * Creates a ProjectileConfig for a projectile.
     *
     * @param target      The target of the projectile
     * @param texturePath The path to the projectile's texture
     */
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
            default -> throw new IllegalArgumentException("Unknown target: " + target);
        }
        this.texturePath = texturePath;
    }
}
