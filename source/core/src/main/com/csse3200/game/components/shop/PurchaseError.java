package com.csse3200.game.components.shop;

/**
 * Represents possible errors that can occur during a purchase event.
 * <p>
 * These values allow shop systems to communicate the exact reason
 * why a purchase request failed, or {@link #NONE} if successful.
 * </p>
 */
public enum PurchaseError {
    NONE,
    NOT_FOUND,
    DISABLED,
    INSUFFICIENT_FUNDS,
    INVENTORY_FULL,
    LIMIT_REACHED,
    INVALID_ITEM,
    UNEXPECTED,
}

