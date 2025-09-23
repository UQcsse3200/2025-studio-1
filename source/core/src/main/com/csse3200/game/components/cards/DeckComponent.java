package com.csse3200.game.components.cards;

import com.csse3200.game.components.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckComponent extends Component {
    List<Card> deck;
    int position;

    public DeckComponent() {
        deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for(Rank rank : Rank.values()) {
                deck.add(new Card(suit, rank));
            }

        }

        Collections.shuffle(deck);
        position = 0;
    }

    public Card drawCard() {
        return deck.get(position++);
    }

    public void resetDeck() {
        position = 0;
        Collections.shuffle(deck);
    }

}
