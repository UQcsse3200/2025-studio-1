package com.csse3200.game.components.cards;
import com.csse3200.game.components.Component;

import java.util.List;
import java.util.ArrayList;

public class BlackJackGame extends Component {
    private List<Card> dealerHand;
    private List<Card> playerHand;
    private Deck deck;
    private boolean winner;

    public void create() {
        deck = new Deck();
        dealerHand = new ArrayList<>();
        playerHand = new ArrayList<>();
        entity.getEvents().addListener("start", this::startGame);
        entity.getEvents().addListener("drawCard", this::drawCard);
        entity.getEvents().addListener("stand", this::dealerTurn);
    }

    public int dealerHandValue() {
        return getHandValue(dealerHand);
    }

    public int playerHandValue() {
        return getHandValue(playerHand);
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getDealerHand() {
        return dealerHand;
    }

    private void dealerTurn() {
        if (!winner) {
            while (getHandValue(dealerHand) < 17) {
                dealerHand.add(deck.drawCard());
            }
            if (getHandValue(dealerHand) > 21) {
                entity.getEvents().trigger("dealerbust");
            } else if (getHandValue(playerHand) > getHandValue(dealerHand)) {
                entity.getEvents().trigger("playerwin");
            } else if (getHandValue(playerHand) < getHandValue(dealerHand)) {
                entity.getEvents().trigger("dealerwin");
            } else {
                entity.getEvents().trigger("tie");
            }
            winner = true;
        }


    }

    private void startGame() {
        winner = false;
        deck.resetDeck();
        dealerHand.clear();
        playerHand.clear();
        playerHand.add(deck.drawCard());
        playerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
    }

    private void drawCard() {
        if(!winner) {
            playerHand.add(deck.drawCard());
            if (getHandValue(playerHand) > 21) {
                winner = true;
                entity.getEvents().trigger("playerbust");
            }
        }
    }

    private int getHandValue(List<Card> hand) {
        int value = 0;
        int aces = 0;
        for(Card card : hand) {
            value += card.getValue();
            if(card.getRank() == Rank.ACE) {
                aces++;
            }
        }
        for(int i = 0; i < aces; i++) {
            if((value + 10) <= 21) {
                value += 10;
            }
        }

        return value;
    }
}
