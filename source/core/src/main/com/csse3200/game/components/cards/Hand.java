package com.csse3200.game.components.cards;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    List<Card> cards;
    boolean doubled;

    public Hand() {
        cards = new ArrayList<>();
        doubled = false;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void resetHand() {
        cards.clear();
    }

    public List<Card> getCards() {
        return cards;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void setDoubled(boolean bool) {
        doubled = bool;
    }

    public boolean isDoubled() {
        return doubled;
    }

    /**
     * Calculates the numeric value of a given hand, with special handling for Aces.
     * Aces count as 1 or 11, whichever provides the highest value without exceeding 21.
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

    public boolean isBust() {
        return this.getValue() > 21;
    }

    public boolean isBlackjack() {
        if(cards.size() == 2 && getValue() == 21) {
                return true;
        }
        return false;
    }

    public boolean canSplit() {
        if(cards.size() != 2) {
            return false;
        }
        Card card1 = cards.getFirst();
        Card card2 = cards.getLast();
        if(card1.getValue() == card2.getValue()) {
            return true;
        }
        return false;
    }

    public boolean canDouble() {
        return cards.size() == 2;
    }

    public void remove(Card card) {
        cards.remove(card);
    }

}