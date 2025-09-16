package com.csse3200.game.components.shop;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages updating a player's inventory after an event (e.g., item purchase).
 */
public class InventoryOperations {

    private InventoryOperations() {}

    public static int addOrStack(InventoryComponent inventory, Entity item,
                                 int amount, int maxStack) {
        if (inventory == null || item == null || amount <= 0 || maxStack <= 0) {
            return PurchaseError.UNEXPECTED.getCode();
        }

        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            return PurchaseError.INVALID_ITEM.getCode();
        }

        int index = findIndexForItem(inventory, item);
        if (index >= 0) {
            // Found existing slot
            Entity existing = inventory.getInventory().get(index);
            ItemComponent existingItemComponent = existing.getComponent(ItemComponent.class);
            if (existingItemComponent == null) {
                return PurchaseError.INVALID_ITEM.getCode();
            }

            int currentQuantity = existingItemComponent.getCount();
            if  (currentQuantity + amount > maxStack) {
                return PurchaseError.LIMIT_REACHED.getCode();
            }
            existingItemComponent.setCount(currentQuantity + amount);
            inventory.getEntity().getEvents()
                    .trigger("update item count", index, existingItemComponent.getCount());
            return index;
        } else {  // Insert as a new slot
            if (inventory.isFull()) {
                return PurchaseError.INVENTORY_FULL.getCode();
            }
            if (amount > maxStack) {
                return PurchaseError.LIMIT_REACHED.getCode();
            }
            itemComponent.setCount(amount);
            boolean ok = inventory.addItem(item);
            if (!ok) {
                return PurchaseError.UNEXPECTED.getCode();
            }
            return inventory.getInventory().indexOf(item);
        }
    }

    /**
     /**
     * Finds the index of the slot that contains the same {@link Entity} reference.
     *
     * @param inventory the player's inventory
     * @param item the entity to locate
     * @return the index if found; otherwise {@code -1}
     */
    private static int findIndexForItem(InventoryComponent inventory, Entity item) {
        final List<Entity> items = inventory.getInventory();
        for (int i = 0, n = items.size(); i < n; i++) {
            final Entity e = items.get(i);
            if (e != null && e.equals(item)) {
                return i;
            }
        }
        return -1;
    }


}
