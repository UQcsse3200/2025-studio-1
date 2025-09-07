package com.csse3200.game.components.shop;

import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopManager {
    private static final Logger logger = LoggerFactory.getLogger(ShopManager.class);

    private final CatalogService catalog;

    public ShopManager(CatalogService catalog) {
        this.catalog = catalog;
    }


    public PurchaseResult purchase(Entity player, String itemKey) {
        // TODO: refactor this
         InventoryComponent inventory = player.getComponent(InventoryComponent.class);
         if (inventory == null) {
             return fail(player, itemKey, PurchaseError.UNEXPECTED);
         }

         CatalogEntry entry = catalog.get(itemKey);
         if (entry == null) {
             return fail(player, itemKey, PurchaseError.NOT_FOUND);
         }
         if (!entry.enabled()) {
             return fail(player, itemKey, PurchaseError.DISABLED);
         }

         // TODO: make a way for user to input how many to purchase
         final int amount = entry.bundleQuantity();
         final int cost = entry.price();

         // Check user has sufficient funds
        if (!hasSufficientFunds(inventory, amount, cost)) {
            return fail(player, itemKey, PurchaseError.INSUFFICIENT_FUNDS);
        }

        // Spawn item
        Entity item = spawnItem(inventory, itemKey);
        if (item == null) {
            return fail(player, itemKey, PurchaseError.INVALID_ITEM);
        }

        // Add item to Inventory
        int idx = InventoryOperations.addOrStack(inventory, item, amount,
                entry.stackable(), entry.maxStack());
        if (idx < 0) {
            return fail(player, itemKey, PurchaseError.INVENTORY_FULL);
        }

        chargePlayer(inventory, amount, cost);

        // TODO: Success hooks

        return PurchaseResult.ok(itemKey, 1);
    }

    private PurchaseResult fail(Entity player, String itemKey, PurchaseError error) {
        logger.error("Failed to purchase item {}, error: {}", itemKey, error);
        player.getEvents().trigger("purchaseFailed", itemKey, error);
        return PurchaseResult.fail(error);
    }

    private boolean hasSufficientFunds(InventoryComponent inventory, int amount, int cost) {
        return inventory.hasProcessor(amount * cost);
    }

    private void chargePlayer(InventoryComponent inventory, int amount, int cost) {
        int total = amount * cost;
        inventory.addProcessor(-1 * total);
    }

    private Entity spawnItem(InventoryComponent inventory, String itemKey) {
        Entity item = catalog.spawnEntity(itemKey);
        if (item == null) {
            return null;
        }
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            return null;
        }
        if (itemComponent.getCount() <= 0) {
            itemComponent.setCount(1);
        }
        return item;
    }
}
