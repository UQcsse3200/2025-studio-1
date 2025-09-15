package com.csse3200.game.entities.factories.items;

import com.csse3200.game.components.items.ConsumableComponent;
import com.csse3200.game.components.items.ConsumableUseComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.RangedUseComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Consumables;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.consumables.ConsumableConfig;
import com.csse3200.game.entities.configs.consumables.ProjectileConsumableConfig;

public class ConsumableFactory {

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

    private static void setUpAsProjectileLauncher(Entity consumable, ProjectileConsumableConfig config) {
        int damage = config.projectileDamage;

        consumable.addComponent(new WeaponsStatsComponent(damage))
                .addComponent(new RangedUseComponent());

        consumable.getComponent(WeaponsStatsComponent.class).setProjectileTexturePath(config.texturePath);
    }
}
