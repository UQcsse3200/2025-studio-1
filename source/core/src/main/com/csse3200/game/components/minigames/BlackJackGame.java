package com.csse3200.game.components.minigames;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.cards.Card;
import com.csse3200.game.components.cards.Deck;
import com.csse3200.game.components.cards.Hand;
import com.csse3200.game.components.cards.Rank;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code BlackJackGame} component implements the core logic for a Blackjack mini-game.
 * <p>
 * It handles deck management, dealing cards, calculating hand values, and determining the
 * game outcome (win, lose, tie, or bust). This class does not handle any UI rendering and
 * can be safely unit tested independently of visual components.
 * </p>
 */
public class BlackJackGame extends Component {
    /** The dealer’s current hand of cards. */
    private Hand dealerHand;
    /** The player’s current hand of cards. */
    private List<Hand> playerHands;
    /** The deck used for drawing cards. */
    private Deck deck;
    /** Indicates whether a winner has been determined this round. */
    private boolean winner;
    Hand currentHand;

    /** Initializes the game by creating a new deck and empty hands,
     * and registers listeners for player actions (hit and stand). */
    public void create() {
        deck = new Deck();
        dealerHand = new Hand();
        playerHands = new ArrayList<>();
        playerHands.add(new Hand());
        currentHand = playerHands.getFirst();
        entity.getEvents().addListener("drawCard", this::drawCard);
        entity.getEvents().addListener("stand", this::dealerTurn);
    }

    /** Returns the total value of the dealer's hand, accounting for ace adjustments. */
    public int dealerHandValue() {
        return getHandValue(dealerHand);
    }

    /** Returns the total value of the player’s hand, accounting for ace adjustments. */
    public int playerHandValue() {
        return getHandValue(currentHand);
    }

    /** Returns the player’s hand. */
    public List<Card> getPlayerHand() {
        return currentHand.getCards();
    }

    /** Returns the dealer’s hand. */
    public List<Card> getDealerHand() {
        return dealerHand.getCards();
    }

    /**
     * Executes the dealer’s turn logic. The dealer continues drawing cards until their
     * hand value reaches at least 17. Once done, the game outcome is determined based
     * on hand values and the appropriate game event is triggered.
     */
    void dealerTurn() {
        if (!winner) {
            while (getHandValue(dealerHand) < 17) {
                dealerHand.addCard(deck.drawCard());
            }

            int dealerValue = getHandValue(dealerHand);
            int playerValue = getHandValue(currentHand);

            if (dealerValue > 21) {
                entity.getEvents().trigger("dealerbust");
                entity.getEvents().trigger("win");
            } else if (playerValue > dealerValue) {
                entity.getEvents().trigger("playerWin");
                entity.getEvents().trigger("win");
            } else if (playerValue < dealerValue) {
                entity.getEvents().trigger("dealerWin");
                entity.getEvents().trigger("lose");
            } else {
                entity.getEvents().trigger("tie");
            }

            winner = true;
        }
    }

    /** Starts a new game by resetting the deck and dealing two cards each to the player and dealer. */
    public void startGame() {
        winner = false;
        deck.resetDeck();
        dealerHand.resetHand();
        playerHands.clear();
        currentHand.resetHand();

        currentHand.addCard(deck.drawCard());
        currentHand.addCard(deck.drawCard());
        dealerHand.addCard(deck.drawCard());
        dealerHand.addCard(deck.drawCard());
    }

    /** Draws a card for the player (on "Hit"). If the player’s hand exceeds 21, triggers bust events. */
    void drawCard() {
        if (!winner) {
            currentHand.addCard(deck.drawCard());
            if (getHandValue(currentHand) > 21) {
                winner = true;
                entity.getEvents().trigger("playerbust");
                entity.getEvents().trigger("lose");
            }
        }
    }

    /**
     * Calculates the numeric value of a given hand, with special handling for Aces.
     * Aces count as 1 or 11, whichever provides the highest value without exceeding 21.
     */
    private int getHandValue(Hand hand) {
        int value = 0;
        int aces = 0;

        for (Card card : hand.getCards()) {
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

    public boolean canSplit(Hand hand) {
        List<Card> cards = hand.getCards();
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

}
