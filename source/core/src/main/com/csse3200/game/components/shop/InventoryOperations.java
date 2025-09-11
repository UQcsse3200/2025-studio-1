package com.csse3200.game.components.shop;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

/**
 * Manages updating a player's inventory after an event (e.g., item purchase).
 */
public class InventoryOperations {
    private InventoryOperations() {}

    /**
     * Returns the index of the slot containing the item, if any.
     *
     * @param inventory the player's inventory
     * @param key the item's unique key ([type]:[name])
     * @return the index or -1 if not found.
     */
    public static int findIndexByKey(InventoryComponent inventory, String key) {
        int n = inventory.getInventory().size();
        for (int i = 0; i < n; i++) {
            Entity entity = inventory.get(i);
            if (entity == null) {
                continue;
            }
            String k = keyOf(entity);
            if (key != null && key.equals(k)) {
                return i;
            }
        }
        return -1;
    }

    public static int addOrStack(InventoryComponent inventory, Entity item, int amount,
                                 boolean stackable, int maxStack) {
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            return -1;
        }

        String key = keyOf(item);
        if (key == null) {
            return -1;
        }

        // TODO: refactor this
        if (stackable) {
            int idx = findIndexByKey(inventory, key);
            if (idx >= 0) {
                Entity slot = inventory.get(idx);
                ItemComponent slotItemComponent = slot.getComponent(ItemComponent.class);
                int before = Math.max(1, slotItemComponent.getCount());
                int toAdd = Math.min(amount, Math.max(1, maxStack) - before);
                if (toAdd > 0) {
                    slotItemComponent.setCount(before + toAdd);
                    itemComponent.getEntity().getEvents().trigger(
                            "inventoryUpdated", idx, slotItemComponent.getCount());
                    return idx;
                }
                // TODO: consider case where cannot be added to current slot. Attempt to add to new slot
            }
        }
        boolean ok = inventory.addItem(item);
        return ok ? inventory.getInventory().indexOf(item) : -1;
    }

    public static boolean consume(InventoryComponent inventory,
                                  String itemName, String itemType,
                                  int amount) {
        String key = itemType + ":"  + itemName;
        int idx = findIndexByKey(inventory, key);
        if (idx < 0) {
            return false;
        }

        Entity item = inventory.get(idx);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        int before = Math.max(1, itemComponent.getCount());
        int after = Math.max(0, before - amount);
        itemComponent.setCount(after);
        inventory.getEntity().getEvents().trigger("stack_updated",
                idx, itemComponent.getCount());
        if (after == 0) {
            inventory.remove(idx);
        }
        inventory.getEntity().getEvents().trigger("consumed", itemName, amount);
        return true;
    }

    private static String keyOf(Entity entity) {
        ItemComponent item = entity.getComponent(ItemComponent.class);
        if (item == null) {
            return null;
        }

        String name = item.getName();
        ItemTypes type = item.getType();
        if (name == null || type == null) {
            return null;
        }
        return type + ":"  + name;
    }
}
