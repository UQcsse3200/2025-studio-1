package com.csse3200.game.components.cards;

/**
 * Represents the suit of a playing card.
 * <p>
 * Each {@code Suit} has a symbolic string used for display and identification
 * of cards in combination with {@link Rank}.
 * </p>
 */
public enum Suit {
    /**
     * Clubs suit, symbol "C".
     */
    CLUBS("C"),

    /**
     * Diamonds suit, symbol "D".
     */
    DIAMONDS("D"),

    /**
     * Hearts suit, symbol "H".
     */
    HEARTS("H"),

    /**
     * Spades suit, symbol "S".
     */
    SPADES("S");

    /**
     * Symbolic representation of the suit (e.g., "C", "D", "H", "S").
     */
    private final String symbol;

    /**
     * Constructs a suit with a symbolic representation.
     *
     * @param symbol the symbol for this suit
     */
    Suit(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the symbol representing this suit.
     *
     * @return the suit's symbol
     */
    public String getSymbol() {
        return symbol;
    }
}
