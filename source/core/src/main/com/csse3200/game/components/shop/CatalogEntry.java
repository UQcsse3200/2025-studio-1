package com.csse3200.game.components.shop;


/**
 * Models one purchasable catalog item that can be found in the shop.
 */
public record CatalogEntry(
        String itemKey,
        int price,
        boolean enabled,
        boolean stackable,
        int maxStack,
        int bundleQuantity
) {

    /**
     * Constructs a CatalogEntry, storing the details around an item that can
     * be purchased from the shop
     *
     * @param itemKey The unique identifiable itemKey
     * @param price The price (per unit) of the item
     * @param enabled Whether this item can currently be purchased by the player.
     *                (E.g., disabled due to max stack, not enough processors, etc.)
     * @param stackable Whether the use may have multiple of this item in an
     *                  inventory slot.
     * @param maxStack The max number of this item that can be stored in one inventory
     *                 slot (1 if stackable is false)
     * @param bundleQuantity How many units of the item sold per purchase.
     */
    public CatalogEntry {
        checkValidEntry(itemKey, price, stackable, maxStack, bundleQuantity);
    }

    /**
     * Calculates the cost for the quantity of this CatalogEntry.
     *
     * @param quantity the number the player wants to purchase
     * @return the total cost
     */
    public int costFor(int quantity) {
        int qty = Math.max(1, quantity);
        return (price * qty);
    }

    private void checkValidEntry(String itemKey, int price,
                                 boolean stackable, int maxStack,
                                 int bundleQuantity) {
        if (itemKey == null || itemKey.isBlank()) {
            throw new IllegalArgumentException("itemKey cannot be null or blank");
        }

        if (price <= 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }

        if (bundleQuantity <= 0) {
            throw new IllegalArgumentException("bundleQuantity cannot be negative");
        }

        if (!stackable && maxStack < 1) {
            throw new IllegalArgumentException("maxStack cannot be negative for stackables");
        }
    }
}