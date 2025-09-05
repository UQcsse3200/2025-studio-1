package com.csse3200.game.components.shop;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;

public class ShopManager {
    private final CatalogService catalog;

    public ShopManager(CatalogService catalog) {
        this.catalog = catalog;
    }


    public PurchaseResult purchase(Entity player, String itemId) {
        // InventoryComponent inventory = player.getComponent(InventoryComponent.class);

        return PurchaseResult.ok(itemId, 1);
    }
}
