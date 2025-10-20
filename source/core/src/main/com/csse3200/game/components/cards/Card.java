package com.csse3200.game.components.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Represents a single playing card in the game of Blackjack.
 * <p>
 * Each {@code Card} has a {@link Suit}, a {@link Rank}, and a
 * {@link TextureRegion} used for rendering its visual appearance.
 * Cards provide convenient access to their symbolic name and
 * Blackjack-specific numeric value.
 * </p>
 */
public class Card {
    /**
     * The suit of this card (e.g., Hearts, Clubs).
     */
    private Suit suit;

    /**
     * The rank of this card (e.g., Two, King, Ace).
     */
    private Rank rank;

    /**
     * The texture used to render this card on screen.
     */
    private TextureRegion texture;

    /**
     * Constructs a card with the specified suit, rank, and texture.
     *
     * @param suit    the {@link Suit} of the card
     * @param rank    the {@link Rank} of the card
     * @param texture the {@link TextureRegion} image representing the card
     */
    public Card(Suit suit, Rank rank, TextureRegion texture) {
        this.suit = suit;
        this.rank = rank;
        this.texture = texture;
    }

    /**
     * Returns a string representation of this card, consisting of its rank and suit symbols.
     * <p>
     * For example, the Ace of Spades might be represented as {@code "AS"}.
     * </p>
     *
     * @return the symbolic name of this card
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns the symbolic name of this card, combining rank and suit.
     *
     * @return the card's symbol, such as {@code "KH"} or {@code "7D"}
     */
    public String getName() {
        return rank.getSymbol() + suit.getSymbol();
    }

    /**
     * Returns the suit of this card.
     *
     * @return the {@link Suit} of this card
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Returns the rank of this card.
     *
     * @return the {@link Rank} of this card
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Returns the Blackjack value of this card.
     * <p>
     * For example, face cards are worth 10 and an Ace is worth 1 (or 11 depending on the hand).
     * </p>
     *
     * @return the numeric value of this card
     */
    public int getValue() {
        return rank.getValue();
    }

    /**
     * Returns the texture image used to render this card.
     *
     * @return this card's {@link TextureRegion}
     */
    public TextureRegion getTexture() {
        return texture;
    }
}
