package com.csse3200.game.components.minigames;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.cards.Card;
import com.csse3200.game.components.cards.Deck;
import com.csse3200.game.components.cards.Hand;

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
    private Hand currentHand;
    int handIndex;
    boolean dealerTurn;

    /** Initializes the game by creating a new deck and empty hands,
     * and registers listeners for player actions (hit and stand). */
    public void create() {
        deck = new Deck();
        dealerHand = new Hand();
        playerHands = new ArrayList<>();
        playerHands.add(new Hand());
        currentHand = playerHands.getFirst();
        entity.getEvents().addListener("drawCard", this::drawCard);
        entity.getEvents().addListener("stand", this::nextHand);
        handIndex = 0;
        dealerTurn = false;
    }

    /** Returns the total value of the dealer's hand, accounting for ace adjustments. */
    public int dealerHandValue() {
        return dealerHand.getValue();
    }

    /** Returns the total value of the player’s hand, accounting for ace adjustments. */
    public int playerHandValue() {
        return currentHand.getValue();
    }

    /** Returns the player’s hand. */
    public List<Hand> getPlayerHands() {
        return playerHands;
    }

    /** Returns the dealer’s hand. */
    public Hand getDealerHand() {
        return dealerHand;
    }

    public Hand getCurrentHand() {
        return currentHand;
    }
    /**
     * Executes the dealer’s turn logic. The dealer continues drawing cards until their
     * hand value reaches at least 17. Once done, the game outcome is determined based
     * on hand values and the appropriate game event is triggered.
     */
    void dealerTurn() {
        dealerTurn = true;
            while (dealerHand.getValue() < 17) {
                dealerHand.addCard(deck.drawCard());
            }

            checkWinners();

    }

    /** Starts a new game by resetting the deck and dealing two cards each to the player and dealer. */
    public void startGame() {
        dealerTurn = false;
        handIndex = 0;
        dealerHand.resetHand();
        playerHands.clear();
        playerHands.add(currentHand);
        currentHand.resetHand();

        currentHand.addCard(deck.drawCard());
        currentHand.addCard(deck.drawCard());
        dealerHand.addCard(deck.drawCard());
        dealerHand.addCard(deck.drawCard());
        if(currentHand.isBlackjack()) {
            nextHand();
        }
    }

    /** Draws a card for the player (on "Hit"). If the player’s hand exceeds 21, triggers bust events. */
    void drawCard() {
            currentHand.addCard(deck.drawCard());
            if(currentHand.isBust()) {
                nextHand();
            }

    }

    public Deck getDeck() {
        return deck;
    }

    public void nextHand() {
        if (handIndex < playerHands.size() - 1) {
            handIndex++;
            currentHand = playerHands.get(handIndex);
            currentHand.addCard(deck.drawCard());
        } else {
            dealerTurn(); // once all hands played, dealer acts
        }
    }

    public void splitHand() {
        if (currentHand.canSplit() && playerHands.size() < 4) {
            Card second = currentHand.getCards().get(1);

            currentHand.remove(second);
            currentHand.addCard(deck.drawCard());
            Hand hand2 = new Hand();
            hand2.addCard(second);

            playerHands.add(hand2);

            if(currentHand.isBlackjack()) {
                nextHand();
            }

        }
    }

    public void doubleDown() {
        currentHand.setDoubled(true);
        currentHand.addCard(deck.drawCard());
        nextHand();
    }

    public int getActiveHandIndex() {
        return handIndex;
    }

    public boolean isDealerTurn() {
        return dealerTurn;
    }

    private void checkWinners() {
        List<String> results = new ArrayList<>();
        int i = 1;

        for(Hand hand : playerHands) {
            String outcome;
            if(hand.isBlackjack()) {
                outcome = "Hand " + i + ": Blackjack! Player Wins!";
                winner(hand);
            } else if(hand.isBust()) {
                outcome = "Hand " + i + ": Bust! Dealer Wins!";
                lose(hand);
            } else if(dealerHand.isBust()){
                outcome = "Hand " + i + ": Dealer Busts! Player Wins!";
                winner(hand);
            } else if(hand.getValue() < dealerHand.getValue()) {
                outcome = "Hand " + i + ": Dealer Wins!";
                lose(hand);
            } else if (hand.getValue() > dealerHand.getValue()) {
                outcome = "Hand " + i + ": Player Wins!";
                winner(hand);
            } else {
                outcome = "Hand " + i + ": Tie!";
                if(hand.isDoubled()) {
                    entity.getEvents().trigger("doubleTie");
                    hand.setDoubled(false);
                } else {
                    entity.getEvents().trigger("tie");
                }
            }
            results.add(outcome);
            i++;
        }
        entity.getEvents().trigger("displayResults", results);
    }

    private void winner(Hand hand) {
        if(hand.isDoubled()) {
            entity.getEvents().trigger("doubleWin");
            hand.setDoubled(false);
        } else {
            entity.getEvents().trigger("win");
        }
    }

    private void lose(Hand hand) {
        if(hand.isDoubled()) {
            entity.getEvents().trigger("doubleLose");
            hand.setDoubled(false);
        } else {
            entity.getEvents().trigger("lose");
        }
    }



}
