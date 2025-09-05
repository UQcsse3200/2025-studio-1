package com.csse3200.game.components.shop;

public final class CatalogEntry {
    public final String itemId;
    public final int price;  // in processors
    public final boolean enabled;
    public final boolean stackable;  // for consumable items
    public final int maxStack;
    public final int bundleQty;

    public CatalogEntry(String itemId, int price, boolean enabled,
                        boolean stackable, int maxStack, int bundleQty) {
        this.itemId = itemId;
        this.price = Math.max(0, price);
        this.enabled = enabled;
        this.stackable = stackable;
        this.maxStack = Math.max(1, stackable ? maxStack : 1);
        this.bundleQty = Math.max(1, bundleQty);
    }
}