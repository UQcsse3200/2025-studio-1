package com.csse3200.game.components.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single hand of cards in a Blackjack game.
 * <p>
 * A {@code Hand} holds a list of {@link Card} objects and provides methods
 * for calculating its total value, determining if it can be split or doubled,
 * and checking for special conditions such as blackjack or bust.
 * </p>
 */
public class Hand {
    /** The list of cards currently in this hand. */
    private List<Card> cards;

    /** Whether this hand has been doubled down. */
    private boolean doubled;

    /**
     * Constructs a new, empty {@code Hand}.
     * The hand starts with no cards and is not doubled.
     */
    public Hand() {
        cards = new ArrayList<>();
        doubled = false;
    }

    /**
     * Adds a card to this hand.
     *
     * @param card the {@link Card} to add
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Removes all cards from this hand, resetting it to empty.
     */
    public void resetHand() {
        cards.clear();
    }

    /**
     * Returns the list of cards in this hand.
     *
     * @return the list of {@link Card} objects in the hand
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Checks if this hand currently contains no cards.
     *
     * @return {@code true} if the hand is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Marks this hand as doubled or not doubled.
     *
     * @param bool {@code true} to mark the hand as doubled, {@code false} otherwise
     */
    public void setDoubled(boolean bool) {
        doubled = bool;
    }

    /**
     * Checks whether this hand has been doubled.
     *
     * @return {@code true} if doubled, {@code false} otherwise
     */
    public boolean isDoubled() {
        return doubled;
    }

    /**
     * Calculates the total numeric value of this hand, handling Aces appropriately.
     * <p>
     * Aces count as either 1 or 11, whichever results in the highest possible value
     * that does not exceed 21.
     * </p>
     *
     * @return the total hand value according to Blackjack rules
     */
    public int getValue() {
        int value = 0;
        int aces = 0;

        for (Card card : cards) {
            value += card.getValue();
            if (card.getRank() == Rank.ACE) {
                aces++;
            }
        }

        // Adjust Ace values (count some as 11 if possible)
        for (int i = 0; i < aces; i++) {
            if ((value + 10) <= 21) {
                value += 10;
            }
        }

        return value;
    }

    /**
     * Determines whether this hand is bust (value exceeds 21).
     *
     * @return {@code true} if the hand's value exceeds 21, {@code false} otherwise
     */
    public boolean isBust() {
        return this.getValue() > 21;
    }

    /**
     * Checks if this hand is a blackjack.
     * <p>
     * A blackjack occurs when the hand contains exactly two cards
     * and their total value equals 21.
     * </p>
     *
     * @return {@code true} if the hand is a blackjack, {@code false} otherwise
     */
    public boolean isBlackjack() {
        return cards.size() == 2 && getValue() == 21;
    }

    /**
     * Determines whether this hand can be split into two hands.
     * <p>
     * A hand can be split if it contains exactly two cards of equal value.
     * </p>
     *
     * @return {@code true} if the hand can be split, {@code false} otherwise
     */
    public boolean canSplit() {
        if (cards.size() != 2) {
            return false;
        }
        Card card1 = cards.getFirst();
        Card card2 = cards.getLast();
        return card1.getValue() == card2.getValue();
    }

    /**
     * Checks whether this hand is eligible for a double down action.
     * <p>
     * A hand can be doubled if it contains exactly two cards.
     * </p>
     *
     * @return {@code true} if the hand can be doubled, {@code false} otherwise
     */
    public boolean canDouble() {
        return cards.size() == 2;
    }

    /**
     * Removes a specific card from the hand.
     *
     * @param card the {@link Card} to remove
     */
    public void remove(Card card) {
        cards.remove(card);
    }
}
