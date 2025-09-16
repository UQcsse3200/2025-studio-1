package com.csse3200.game.entities.configs.consumables;

public abstract class ProjectileConsumableConfig extends ConsumableConfig {
    // Anything that would affect how the projectile is created
    public int projectileSpeed = 1;
    public int projectileSize = 1;
    public int projectileDamage = 1;

    public ProjectileConsumableConfig() {
        this.isProjectile = true;
    }
}
