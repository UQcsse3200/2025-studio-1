package com.csse3200.game.components.minigames;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.cards.Card;
import com.csse3200.game.components.cards.Deck;
import com.csse3200.game.components.cards.Rank;

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
    /**
     * The dealer’s current hand of cards.
     */
    private List<Card> dealerHand;
    /**
     * The player’s current hand of cards.
     */
    private List<Card> playerHand;
    /**
     * The deck used for drawing cards.
     */
    private Deck deck;
    /**
     * Indicates whether a winner has been determined this round.
     */
    private boolean winner;

    /**
     * Initializes the game by creating a new deck and empty hands,
     * and registers listeners for player actions (hit and stand).
     */
    public void create() {
        deck = new Deck();
        dealerHand = new ArrayList<>();
        playerHand = new ArrayList<>();
        entity.getEvents().addListener("drawCard", this::drawCard);
        entity.getEvents().addListener("stand", this::dealerTurn);
    }

    /**
     * Returns the total value of the dealer's hand, accounting for ace adjustments.
     *
     * @return the total numeric value of the dealer’s hand
     */
    public int dealerHandValue() {
        return getHandValue(dealerHand);
    }

    /**
     * Returns the total value of the player’s hand, accounting for ace adjustments.
     *
     * @return the total numeric value of the player’s hand
     */
    public int playerHandValue() {
        return getHandValue(playerHand);
    }

    /**
     * Returns the list of cards currently in the player’s hand.
     *
     * @return the player’s hand as a list of {@link Card} objects
     */
    public List<Card> getPlayerHand() {
        return playerHand;
    }

    /**
     * Returns the list of cards currently in the dealer’s hand.
     *
     * @return the dealer’s hand as a list of {@link Card} objects
     */
    public List<Card> getDealerHand() {
        return dealerHand;
    }

    /**
     * Executes the dealer’s turn logic. The dealer continues drawing cards until their
     * hand value reaches at least 17. Once done, the game outcome is determined based
     * on hand values and the appropriate game event is triggered.
     * <ul>
     *     <li>Triggers {@code dealerbust} and {@code win} if the dealer exceeds 21.</li>
     *     <li>Triggers {@code playerWin} and {@code win} if the player’s hand is higher.</li>
     *     <li>Triggers {@code dealerWin} and {@code lose} if the dealer’s hand is higher.</li>
     *     <li>Triggers {@code tie} if both hands are equal.</li>
     * </ul>
     */
    void dealerTurn() {
        if (!winner) {
            while (getHandValue(dealerHand) < 17) {
                dealerHand.add(deck.drawCard());
            }
            if (getHandValue(dealerHand) > 21) {
                entity.getEvents().trigger("dealerbust");
                entity.getEvents().trigger("win");
            } else if (getHandValue(playerHand) > getHandValue(dealerHand)) {
                entity.getEvents().trigger("playerWin");
                entity.getEvents().trigger("win");
            } else if (getHandValue(playerHand) < getHandValue(dealerHand)) {
                entity.getEvents().trigger("dealerWin");
                entity.getEvents().trigger("lose");
            } else {
                entity.getEvents().trigger("tie");
            }
            winner = true;
        }
    }

    /**
     * Starts a new game by resetting the deck and dealing two cards each to the
     * player and dealer. Clears previous hands and sets {@code winner} to false.
     */
    public void startGame() {
        winner = false;
        deck.resetDeck();
        dealerHand.clear();
        playerHand.clear();
        playerHand.add(deck.drawCard());
        playerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
    }

    /**
     * Draws a card for the player (on "Hit"). If the player’s hand exceeds 21,
     * the player busts and the appropriate lose events are triggered.
     */
    void drawCard() {
        if (!winner) {
            playerHand.add(deck.drawCard());
            if (getHandValue(playerHand) > 21) {
                winner = true;
                entity.getEvents().trigger("playerbust");
                entity.getEvents().trigger("lose");
            }
        }
    }

    /**
     * Calculates the numeric value of a given hand, with special handling for Aces.
     * Aces count as 1 or 11, whichever provides the highest value without exceeding 21.
     *
     * @param hand the list of cards to evaluate
     * @return the total hand value as an integer
     */
    private int getHandValue(List<Card> hand) {
        int value = 0;
        int aces = 0;
        for (Card card : hand) {
            value += card.getValue();
            if (card.getRank() == Rank.ACE) {
                aces++;
            }
        }
        for (int i = 0; i < aces; i++) {
            if ((value + 10) <= 21) {
                value += 10;
            }
        }
        return value;
    }
}
