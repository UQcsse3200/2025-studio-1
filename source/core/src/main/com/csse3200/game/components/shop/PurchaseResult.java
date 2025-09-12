package com.csse3200.game.components.shop;

import com.csse3200.game.entities.Entity;

/**
 * Stores the result of a purchase event.
 * @param ok
 * @param error
 * @param entry
 * @param qty
 */
public record PurchaseResult(boolean ok, PurchaseError error, CatalogEntry entry, int qty) {
    public static PurchaseResult ok(CatalogEntry entry, int qty) {
        return new PurchaseResult(true, PurchaseError.NONE, entry, qty);
    }

    public static PurchaseResult fail(PurchaseError e) {
        return new PurchaseResult(false, e, null, 0);
    }
}