package com.csse3200.game.entities.factories.items;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ConsumableComponent;
import com.csse3200.game.components.items.ConsumableUseComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.items.RangedUseComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Consumables;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.consumables.ConsumableConfig;
import com.csse3200.game.entities.configs.consumables.ProjectileConsumableConfig;

/**
 * Factory class responsible for creating consumable items in the game.
 * <p>
 * Consumables can either be standard items that apply effects directly
 * or projectile-based items (e.g., bombs or throwable items) that
 * use {@link RangedUseComponent} and {@link WeaponsStatsComponent}.
 */
public class ConsumableFactory {

    /**
     * Creates a consumable entity of the specified type.
     * <p>
     * The returned entity will have:
     * <ul>
     *   <li>A {@link ConsumableComponent} containing its effects and duration</li>
     *   <li>A {@link ConsumableUseComponent} to handle usage</li>
     *   <li>Optional projectile behavior if the configuration specifies a projectile</li>
     *   <li>Its item type set to {@link ItemTypes#CONSUMABLE}</li>
     * </ul>
     *
     * @param consumableType the type of consumable to create
     * @return the created consumable entity
     */
    public static Entity createConsumable(Consumables consumableType) {
        ConsumableConfig config = consumableType.getConfig();
        Entity consumable = ItemFactory.createItem(config.texturePath)
                .addComponent(new ConsumableComponent(config.effects, config.duration))
                .addComponent(new ConsumableUseComponent());

        if (config.isProjectile) {
            setUpAsProjectileLauncher(consumable, (ProjectileConsumableConfig) config);
        }

        consumable.getComponent(ItemComponent.class).setType(ItemTypes.CONSUMABLE);
        consumable.create();
        return consumable;
    }

    /**
     * Configures a consumable entity to behave as a projectile launcher.
     * <p>
     * Adds a {@link WeaponsStatsComponent} and {@link RangedUseComponent}
     * to the entity and sets the projectile texture.
     *
     * @param consumable the consumable entity to configure
     * @param config     the projectile-specific configuration
     */
    private static void setUpAsProjectileLauncher(Entity consumable, ProjectileConsumableConfig config) {
        int damage = config.projectileDamage;

        consumable.addComponent(new WeaponsStatsComponent(damage))
                .addComponent(new RangedUseComponent());

        consumable.getComponent(WeaponsStatsComponent.class).setProjectileTexturePath(config.texturePath);
    }
}
