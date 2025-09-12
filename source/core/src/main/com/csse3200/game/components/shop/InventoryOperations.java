package com.csse3200.game.components.shop;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

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
     * @param key the item's unique key ([type]:[name])
     * @return the index or -1 if not found.
     */
    public static int findIndexByKey(InventoryComponent inventory, Entity item) {
        ArrayList<Entity> inventoryItems = inventory.getInventory();
        for (Entity e : inventoryItems) {
            if (e.equals(item)) {
                return inventoryItems.indexOf(e);
            }
        }
        return -1;
    }

    public static int addOrStack(InventoryComponent inventory, Entity item, int amount,
                                 boolean stackable, int maxStack) {
        boolean ok = inventory.addItem(item);
        return ok ? inventory.getInventory().indexOf(item) : -1;
    }

    public static boolean consume(InventoryComponent inventory,
                                  String itemName, String itemType,
                                  int amount) {
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
