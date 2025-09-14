package com.csse3200.game.components.items;


import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.effects.Effect;
import com.csse3200.game.entities.Entity;

public class ConsumableUseComponent extends ItemActionsComponent {

    public void use(Entity player) {
        ConsumableComponent consumable = entity.getComponent(ConsumableComponent.class);
        if (!consumable.firesProjectile()) {
            for (Effect effect : consumable.getEffects()) {
                effect.apply(player);
            }
        }
        consumeConsumable(player);
    }

    private void consumeConsumable(Entity player) {
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        boolean toBeConsumed = entity.getComponent(ItemComponent.class).getCount() <= 1;

        // Current method of accessing current item from inventory hopefully should be changed to getter method
        // in inventory component following changes suggested to Team 1
        int itemIdx = -1;
        for (int i = 0; i < inventory.getSize(); i++) {
            // Finds index of current item
            if (inventory.get(i).getId() == entity.getId()) {
                itemIdx = i;
                break;
            }
        }

        if (itemIdx == -1) {
            return;
        }

        if (toBeConsumed) {
            inventory.remove(itemIdx);
        } else {
            ItemComponent itemComponent = entity.getComponent(ItemComponent.class);
            itemComponent.setCount(itemComponent.getCount() - 1);
        }
    }
}
