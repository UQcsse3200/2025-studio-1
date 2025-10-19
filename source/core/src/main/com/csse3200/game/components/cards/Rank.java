package com.csse3200.game.components.cards;

/**
 * Represents the rank of a playing card in Blackjack.
 * <p>
 * Each {@code Rank} has a symbolic string (used for display and identification)
 * and a numeric value (used for calculating hand totals in Blackjack).
 * </p>
 */
public enum Rank {
    /** Numeric card 2 with value 2. */
    TWO("2", 2),

    /** Numeric card 3 with value 3. */
    THREE("3", 3),

    /** Numeric card 4 with value 4. */
    FOUR("4", 4),

    /** Numeric card 5 with value 5. */
    FIVE("5", 5),

    /** Numeric card 6 with value 6. */
    SIX("6", 6),

    /** Numeric card 7 with value 7. */
    SEVEN("7", 7),

    /** Numeric card 8 with value 8. */
    EIGHT("8", 8),

    /** Numeric card 9 with value 9. */
    NINE("9", 9),

    /** Numeric card 10 with value 10. */
    TEN("10", 10),

    /** Jack face card with value 10. */
    JACK("J", 10),

    /** Queen face card with value 10. */
    QUEEN("Q", 10),

    /** King face card with value 10. */
    KING("K", 10),

    /** Ace card with value 1 (or 11 depending on hand). */
    ACE("A", 1);

    /** Symbolic representation of the rank (e.g., "A", "10", "J"). */
    private final String symbol;

    /** Numeric value of the rank for Blackjack calculations. */
    private final int value;

    /**
     * Constructs a rank with a symbol and numeric value.
     *
     * @param symbol the symbol representing this rank
     * @param value  the numeric value of the rank in Blackjack
     */
    Rank(String symbol, int value) {
        this.symbol = symbol;
        this.value = value;
    }

    /**
     * Returns the symbol of this rank.
     *
     * @return the symbolic representation, e.g., "A", "10", "J"
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the numeric value of this rank for Blackjack.
     *
     * @return the rank's numeric value
     */
    public int getValue() {
        return value;
    }
}
