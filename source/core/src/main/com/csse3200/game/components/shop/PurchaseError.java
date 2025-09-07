package com.csse3200.game.components.shop;

/**
 * Errors that could occur during a purchase event
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

