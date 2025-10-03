package com.csse3200.game.components.shop;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;


/**
 * Handles shop-related operations such as validating purchases,
 * charging players, and updating their inventories.
 * <p>
 * A {@code ShopManager} depends on a {@link CatalogService} to
 * resolve purchasable items, and coordinates with the player's
 * {@link InventoryComponent} to ensure space, funds, and item
 * limits are respected.
 * </p>
 */
public class ShopManager extends Component {

    private final CatalogService catalog;


    /**
     * Creates a {@code ShopManager} that uses the given catalog service.
     *
     * @param catalog the catalog of items available for purchase (must not be {@code null})
     */
    public ShopManager(CatalogService catalog) {
        this.catalog = catalog;
    }

    /**
     * Gets the underlying catalog service.
     *
     * @return the catalog service storing available items
     */
    public CatalogService getCatalog() {
        return catalog;
    }


    /**
     * Attempts to purchase a given catalog entry for a player.
     * <p>
     * The purchase process checks that:
     * </p>
     * <ul>
     *   <li>The player has an {@link InventoryComponent}.</li>
     *   <li>The item exists in the catalog and is enabled.</li>
     *   <li>The player has sufficient funds (processors).</li>
     *   <li>The item can be added/stacked in the player's inventory.</li>
     * </ul>
     * <p>
     * On success, the player is charged and their inventory updated.
     * On failure, a {@code "purchaseFailed"} event is triggered on this entity.
     * </p>
     *
     * @param player the entity attempting the purchase (must have an {@link InventoryComponent})
     * @param item   the catalog entry being purchased
     * @param amount the quantity to purchase
     * @return a {@link PurchaseResult} indicating success or failure and the reason
     */
    public PurchaseResult purchase(Entity player, CatalogEntry item, int amount) {
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        if (inventory == null) {
            return fail(item, PurchaseError.UNEXPECTED);
        }

        // Get item being purchased
        CatalogEntry entry = catalog.get(item);
        if (entry == null) {
            return fail(item, PurchaseError.NOT_FOUND);
        }
        if (!entry.enabled()) {
            return fail(item, PurchaseError.DISABLED);
        }

        final int cost = entry.price();

        // Check user has sufficient funds
        if (!hasSufficientFunds(inventory, amount, cost)) {
            return fail(item, PurchaseError.INSUFFICIENT_FUNDS);
        }

        // Add item to Inventory
        int idx = InventoryOperations.addOrStack(inventory, item.getItem(), amount,
                entry.maxStack());
        if (idx < 0) {
            return fail(item, PurchaseError.fromCode(idx));
        }

        chargePlayer(inventory, amount, cost);
        return PurchaseResult.ok(item, 1);
    }

    private PurchaseResult fail(CatalogEntry item, PurchaseError error) {
        entity.getEvents().trigger("purchaseFailed", item.getItemName(), error);
        return PurchaseResult.fail(error);
    }

    private boolean hasSufficientFunds(InventoryComponent inventory, int amount, int cost) {
        return inventory.hasProcessor(amount * cost);
    }

    private void chargePlayer(InventoryComponent inventory, int amount, int cost) {
        int total = amount * cost;
        inventory.addProcessor(-1 * total);
    }
}
