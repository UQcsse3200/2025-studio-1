package com.csse3200.game.components.cards;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    List<Card> deck;
    int position;

    public Deck() {
        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset("images/cards.atlas", TextureAtlas.class);
        deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for(Rank rank : Rank.values()) {
                TextureRegion texture = atlas.findRegion(rank.getSymbol() + suit.getSymbol());
                deck.add(new Card(suit, rank, texture));
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
