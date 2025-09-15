package com.csse3200.game.components.shop;

/**
 * Represents the outcome of a purchase attempt in the shop system.
 * <p>
 * A {@code PurchaseResult} indicates whether the purchase succeeded,
 * the type of error (if any), the catalog entry involved, and the
 * quantity purchased.
 * </p>
 *
 * @param ok {@code true} if the purchase was successful; {@code false} otherwise.
 * @param error The error type if the purchase failed, or {@link PurchaseError#NONE} if successful.
 * @param entry The purchased catalog entry, or {@code null} if the purchase failed.
 * @param qty The quantity purchased. {@code 0} if the purchase failed.
 */
public record PurchaseResult(boolean ok, PurchaseError error, CatalogEntry entry, int qty) {

    /**
     * Creates a successful purchase result.
     *
     * @param entry The catalog entry that was purchased (must not be {@code null}).
     * @param qty The number of units purchased (must be {@code > 0}).
     * @return A {@code PurchaseResult} indicating success with the specified entry and quantity.
     */
    public static PurchaseResult ok(CatalogEntry entry, int qty) {
        return new PurchaseResult(true, PurchaseError.NONE, entry, qty);
    }

    /**
     * Creates a failed purchase result.
     *
     * @param e The error indicating why the purchase failed.
     * @return A {@code PurchaseResult} indicating failure with the specified error.
     *         The {@code entry} will be {@code null} and {@code qty} will be {@code 0}.
     */
    public static PurchaseResult fail(PurchaseError e) {
        return new PurchaseResult(false, e, null, 0);
    }
}