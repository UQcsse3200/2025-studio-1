package com.csse3200.game.components;


import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;

public class ConsumableUseComponent extends ItemActionsComponent {

    public void use(Entity player) {
        consumeConsumable(player);
    }

    private boolean consumeConsumable(Entity player) {
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        boolean toBeConsumed = entity.getComponent(ItemComponent.class).getCount() <= 1;

        int itemIdx = -1;
        for (int i = 0; i < inventory.getSize(); i++) {
            // Finds index of current item
            if (inventory.get(i).getId() == entity.getId()) {
                itemIdx = i;
                break;
            }
        }

        if (itemIdx == -1) {
            return false;
        }

        if (toBeConsumed) {
            inventory.remove(itemIdx);
        } else {
            ItemComponent itemComponent = entity.getComponent(ItemComponent.class);
            itemComponent.setCount(itemComponent.getCount() - 1);
        }
        return true;
    }
}
