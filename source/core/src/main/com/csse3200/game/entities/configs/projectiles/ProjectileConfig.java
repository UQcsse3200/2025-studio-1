package com.csse3200.game.entities.configs.projectiles;

import com.csse3200.game.entities.configs.ItemTypes;

/**
 * Defines all projectile configs to be loaded by the projectile Factory.
 */

public class ProjectileConfig {
    public ItemTypes itemType = ItemTypes.PROJECTILE;
    public short projectileType;

    public short target;
    public int baseAttack;
    public float speed;

    public String texturePath;
}
