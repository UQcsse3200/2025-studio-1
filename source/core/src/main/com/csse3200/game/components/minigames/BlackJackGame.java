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
    /**
     * The dealer’s current hand of cards.
     */
    private Hand dealerHand;

    /**
     * The player’s current hands of cards. Multiple hands allowed due to splits.
     */
    private List<Hand> playerHands;

    /**
     * The deck used for drawing cards.
     */
    private Deck deck;

    /**
     * The currently active player hand.
     */
    private Hand currentHand;

    /**
     * Index of the current active hand in {@code playerHands}.
     */
    int handIndex;

    /**
     * True if it is the dealer's turn to draw cards.
     */
    boolean dealerTurn;
    private static final String HAND_LABEL = "Hand ";

    /**
     * Initializes the game component, sets up deck and hands,
     * and registers listeners for player actions (hit and stand).
     */
    public void create() {
        deck = new Deck();
        dealerHand = new Hand();
        playerHands = new ArrayList<>();
        playerHands.add(new Hand());
        currentHand = playerHands.get(0);

        // Listen for player actions
        entity.getEvents().addListener("drawCard", this::drawCard);
        entity.getEvents().addListener("stand", this::nextHand);

        handIndex = 0;
        dealerTurn = false;
    }

    /**
     * Returns the total value of the dealer's hand, accounting for Ace adjustments.
     */
    public int dealerHandValue() {
        return dealerHand.getValue();
    }

    /**
     * Returns the total value of the currently active player's hand.
     */
    public int playerHandValue() {
        return currentHand.getValue();
    }

    /**
     * Returns all of the player's hands.
     */
    public List<Hand> getPlayerHands() {
        return playerHands;
    }

    /**
     * Returns the dealer's hand.
     */
    public Hand getDealerHand() {
        return dealerHand;
    }

    /**
     * Returns the currently active player hand.
     */
    public Hand getCurrentHand() {
        return currentHand;
    }

    /**
     * Executes the dealer's turn logic. Dealer draws cards until hand value ≥ 17,
     * then checks the outcomes against all player hands.
     */
    void dealerPlay() {
        dealerTurn = true;
        while (dealerHand.getValue() < 17) {
            dealerHand.addCard(deck.drawCard());
        }
        checkWinners();
    }

    /**
     * Starts a new game by resetting the deck and dealing two cards each to
     * the player and dealer. Automatically moves to next hand if the player
     * has a blackjack.
     */
    public void startGame() {
        dealerTurn = false;
        handIndex = 0;
        dealerHand.resetHand();
        playerHands.clear();
        playerHands.add(currentHand);
        currentHand.resetHand();

        // Deal initial cards
        currentHand.addCard(deck.drawCard());
        currentHand.addCard(deck.drawCard());
        dealerHand.addCard(deck.drawCard());
        dealerHand.addCard(deck.drawCard());

        if (currentHand.isBlackjack()) {
            nextHand();
        }
    }

    /**
     * Draws a card for the current player hand. If the hand exceeds 21, moves to the next hand.
     */
    void drawCard() {
        currentHand.addCard(deck.drawCard());
        if (currentHand.isBust()) {
            nextHand();
        }
    }

    /**
     * Returns the deck being used in the game.
     */
    public Deck getDeck() {
        return deck;
    }

    /**
     * Moves to the next hand if multiple hands exist. If all hands played,
     * triggers the dealer's turn.
     */
    public void nextHand() {
        if (handIndex < playerHands.size() - 1) {
            handIndex++;
            currentHand = playerHands.get(handIndex);
            currentHand.addCard(deck.drawCard());
        } else {
            dealerPlay();
        }
    }

    /**
     * Splits the current hand into two hands if allowed (same value cards),
     * and deals one new card to each hand.
     */
    public void splitHand() {
        if (currentHand.canSplit() && playerHands.size() < 4) {
            Card second = currentHand.getCards().get(1);

            currentHand.remove(second);
            currentHand.addCard(deck.drawCard());

            Hand hand2 = new Hand();
            hand2.addCard(second);

            playerHands.add(hand2);

            if (currentHand.isBlackjack()) {
                nextHand();
            }
        }
    }

    /**
     * Doubles the current bet (sets doubled flag) and draws exactly one more card,
     * then moves to the next hand.
     */
    public void doubleDown() {
        currentHand.setDoubled(true);
        currentHand.addCard(deck.drawCard());
        nextHand();
    }

    /**
     * Returns the index of the currently active hand.
     */
    public int getActiveHandIndex() {
        return handIndex;
    }

    /**
     * Returns true if it is the dealer's turn.
     */
    public boolean isDealerTurn() {
        return dealerTurn;
    }

    /**
     * Compares each player hand against the dealer hand to determine outcomes.
     * Triggers events for win, lose, tie, and doubled conditions.
     */
    private void checkWinners() {
        List<String> results = new ArrayList<>();
        int i = 1;

        for (Hand hand : playerHands) {
            String outcome;
            if (hand.isBlackjack()) {
                outcome = HAND_LABEL + i + ": Blackjack! Player Wins!";
                winner(hand);
            } else if (hand.isBust()) {
                outcome = HAND_LABEL + i + ": Bust! Dealer Wins!";
                lose(hand);
            } else if (dealerHand.isBust()) {
                outcome = HAND_LABEL + i + ": Dealer Busts! Player Wins!";
                winner(hand);
            } else if (hand.getValue() < dealerHand.getValue()) {
                outcome = HAND_LABEL + i + ": Dealer Wins!";
                lose(hand);
            } else if (hand.getValue() > dealerHand.getValue()) {
                outcome = HAND_LABEL + i + ": Player Wins!";
                winner(hand);
            } else {
                outcome = HAND_LABEL + i + ": Tie!";
                if (hand.isDoubled()) {
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

    /**
     * Triggers a win event for the given hand, handling doubled bets.
     */
    private void winner(Hand hand) {
        if (hand.isDoubled()) {
            entity.getEvents().trigger("doubleWin");
            hand.setDoubled(false);
        } else {
            entity.getEvents().trigger("win");
        }
    }

    /**
     * Triggers a lose event for the given hand, handling doubled bets.
     */
    private void lose(Hand hand) {
        if (hand.isDoubled()) {
            entity.getEvents().trigger("doubleLose");
            hand.setDoubled(false);
        } else {
            entity.getEvents().trigger("lose");
        }
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }
}
