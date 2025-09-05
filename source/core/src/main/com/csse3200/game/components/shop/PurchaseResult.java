package com.csse3200.game.components.shop;

public record PurchaseResult(boolean ok, PurchaseError error, String itemId, int qty) {
    public static PurchaseResult ok(String itemId, int qty) {
        return new PurchaseResult(true, PurchaseError.NONE, itemId, qty);
    }

    public static PurchaseResult fail(PurchaseError e) {
        return new PurchaseResult(false, e, null, 0);
    }
}