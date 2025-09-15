package com.csse3200.game.components.shop;

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

    public CatalogService getCatalog() {
        return catalog;
    }


    public PurchaseResult purchase(Entity player, CatalogEntry item, int amount) {
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        if (inventory == null) {
            return fail(player, item, PurchaseError.UNEXPECTED);
        }

        CatalogEntry entry = catalog.get(item);
        if (entry == null) {
            return fail(player, item, PurchaseError.NOT_FOUND);
        }
        if (!entry.enabled()) {
            return fail(player, item, PurchaseError.DISABLED);
        }

        final int cost = entry.price();

         // Check user has sufficient funds
        if (!hasSufficientFunds(inventory, amount, cost)) {
            return fail(player, item, PurchaseError.INSUFFICIENT_FUNDS);
        }

        // Add item to Inventory
        int idx = InventoryOperations.addOrStack(inventory, item.getItem(), amount,
                entry.maxStack());
        if (idx < 0) {
            return fail(player, item, PurchaseError.INVENTORY_FULL);
        }

        chargePlayer(inventory, amount, cost);

        // TODO: Success hooks

        return PurchaseResult.ok(item, 1);
    }

    private PurchaseResult fail(Entity player, CatalogEntry item, PurchaseError error) {
//        logger.error("Failed to purchase item {}, error: {}", getItemName(item), error);
        player.getEvents().trigger("purchaseFailed", getItemName(item), error);
        return PurchaseResult.fail(error);
    }

    private boolean hasSufficientFunds(InventoryComponent inventory, int amount, int cost) {
        return inventory.hasProcessor(amount * cost);
    }

    private void chargePlayer(InventoryComponent inventory, int amount, int cost) {
        int total = amount * cost;
        inventory.addProcessor(-1 * total);
    }

    private String getItemName(CatalogEntry entry) {
        return entry.getItemName();
    }

}
