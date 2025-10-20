package com.csse3200.game.components.cards;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a standard double deck of playing cards used in Blackjack.
 * <p>
 * The {@code Deck} class manages the creation, shuffling, and drawing of cards.
 * It loads all card textures from a LibGDX {@link TextureAtlas} and provides
 * methods to retrieve cards, count remaining cards, and reset the deck when
 * necessary. Two full 52-card decks (104 cards total) are included.
 * </p>
 */
public class Deck {
    /**
     * The list of all cards currently in the deck.
     */
    private final List<Card> cards;
    private static final int totalCards = 104;
    /**
     * The index of the next card to be drawn.
     */
    private int position;

    /**
     * Constructs a new shuffled double deck (104 cards) using assets
     * from {@code images/cards.atlas}.
     * <p>
     * Each deck contains one of each {@link Rank} and {@link Suit} combination,
     * and two full decks are generated for use in Blackjack.
     * </p>
     */
    public Deck() {
        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset("images/cards.atlas", TextureAtlas.class);
        cards = new ArrayList<>();

        // Create two standard 52-card decks
        for (int i = 0; i < 2; i++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    TextureRegion texture = atlas.findRegion(rank.getSymbol() + suit.getSymbol());
                    cards.add(new Card(suit, rank, texture));
                }
            }
        }

        Collections.shuffle(cards);
        position = 0;
    }

    /**
     * Returns the number of cards remaining in the deck.
     *
     * @return the number of cards left to draw
     */
    public int cardsRemaining() {
        return totalCards - position;
    }

    /**
     * Draws the next card from the deck.
     * <p>
     * If the deck is nearly empty (less than 4 cards left),
     * it is automatically reset and reshuffled.
     * </p>
     *
     * @return the next {@link Card} from the deck
     */
    public Card drawCard() {
        if (position > 100) {
            resetDeck();
        }
        return cards.get(position++);
    }

    /**
     * Resets the deck to its initial state by shuffling
     * all 104 cards and resetting the draw position.
     */
    public void resetDeck() {
        position = 0;
        Collections.shuffle(cards);
    }
}
