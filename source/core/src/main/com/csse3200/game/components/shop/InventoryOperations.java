package com.csse3200.game.components.shop;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;

import java.util.ArrayList;

/**
 * Manages updating a player's inventory after an event (e.g., item purchase).
 */
public class InventoryOperations {
    private InventoryOperations() {}

    /**
     * Returns the index of the slot containing the item, if any.
     *
     * @param inventory the player's inventory
     * @param item the item's unique key ([type]:[name])
     * @return the index or -1 if not found.
     */
    public static int findIndexForItem(InventoryComponent inventory, Entity item) {
        ArrayList<Entity> inventoryItems = inventory.getInventory();
        System.out.println(inventoryItems);
        for (Entity e : inventoryItems) {
            if (e != null && e.equals(item)) {
                return inventoryItems.indexOf(e);
            }
        }
        return -1;
    }

    public static int addOrStack(InventoryComponent inventory, Entity item,
                                 int amount, int maxStack) {
        if (amount <= 0 || maxStack <= 0) {
            return -1;
        }

        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            return -1;
        }

        int index = findIndexForItem(inventory, item);
        if (index >= 0) {
            Entity existing = inventory.getInventory().get(index);
            ItemComponent existingItemComponent = existing.getComponent(ItemComponent.class);
            if (existingItemComponent == null) {
                return -1;
            }

            int currentQuantity = existingItemComponent.getCount();
            if  (currentQuantity + amount > maxStack) {
                return -1;
            }
            existingItemComponent.setCount(currentQuantity + amount);
            inventory.getEntity().getEvents()
                    .trigger("update item count", index, existingItemComponent.getCount());
            return index;
        }

        if (inventory.isFull()) {
            return -1;
        }
        if (amount > maxStack) {
            return -1;
        }
        itemComponent.setCount(amount);
        boolean ok = inventory.addItem(item);
        if (!ok) {
            return -1;
        }
        return inventory.getInventory().indexOf(item);
    }
}
