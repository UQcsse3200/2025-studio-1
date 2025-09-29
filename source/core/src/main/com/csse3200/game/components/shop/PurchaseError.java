package com.csse3200.game.components.shop;

/**
 * Represents possible errors that can occur during a purchase event.
 * <p>
 * These values allow shop systems to communicate the exact reason
 * why a purchase request failed, or {@link #NONE} if successful.
 * </p>
 */
public enum PurchaseError {
    NONE(0),
    NOT_FOUND(-1),
    DISABLED(-2),
    INSUFFICIENT_FUNDS(-3),
    INVENTORY_FULL(-4),
    LIMIT_REACHED(-5),
    INVALID_ITEM(-6),
    UNEXPECTED(-7),
    INVALID_WEAPON(-8)
    ;

    private final int code;

    PurchaseError(int i) {
        this.code = i;
    }

    /**
     * Returns the code associated with a PurchaseError
     *
     * @return an integer representing the PurchaseError
     */
    public int getCode() {
        return code;
    }

    /**
     * Creates PurchaseError from a integer error code
     *
     * @param code The code representing the error.
     * @return The PurchaseError.
     */
    public static PurchaseError fromCode(int code) {
        for (PurchaseError e : PurchaseError.values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}

