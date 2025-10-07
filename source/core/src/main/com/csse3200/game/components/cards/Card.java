package com.csse3200.game.components.cards;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.w3c.dom.Text;

public class Card {
    Suit suit;
    Rank rank;
    TextureRegion texture;

    public Card(Suit suit, Rank rank, TextureRegion texture) {
        this.suit = suit;
        this.rank = rank;
        this.texture = texture;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return rank.getSymbol() + suit.getSymbol();
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getValue() {
        return rank.getValue();
    }

    public TextureRegion getTexture() {
        return texture;
    }
}
