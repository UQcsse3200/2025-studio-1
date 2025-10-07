package com.csse3200.game.components.minigames;

import com.csse3200.game.components.cards.Card;
import com.csse3200.game.components.cards.Rank;
import com.csse3200.game.components.cards.Suit;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.GameTime;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlackJackGameTest {

    private BlackJackGame game;
    private Entity entity;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        // Fake TextureAtlas that always returns a dummy TextureRegion
        TextureAtlas.AtlasRegion dummyTexture = mock(TextureAtlas.AtlasRegion.class);
        TextureAtlas fakeAtlas = new TextureAtlas() {
            @Override
            public AtlasRegion findRegion(String name) {
                return dummyTexture;
            }
        };

        // Mock ResourceService to return our fake atlas
        ResourceService mockResources = mock(ResourceService.class);
        when(mockResources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(fakeAtlas);
        ServiceLocator.registerResourceService(mockResources);

        ServiceLocator.registerTimeSource(new GameTime());

        // Setup entity and component
        entity = new Entity();
        game = new BlackJackGame();
        entity.addComponent(game);

        // Create the component (Deck is now safe)
        game.create();
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void testStartGameInitialHands() {
        game.startGame();

        List<Card> playerHand = game.getPlayerHand();
        List<Card> dealerHand = game.getDealerHand();

        assertEquals(2, playerHand.size(), "Player should start with 2 cards");
        assertEquals(2, dealerHand.size(), "Dealer should start with 2 cards");
    }

    @Test
    void testDrawCardAddsToPlayerHand() {
        game.startGame();
        int initialSize = game.getPlayerHand().size();
        entity.getEvents().trigger("drawCard");
        assertEquals(initialSize + 1, game.getPlayerHand().size());
    }

    @Test
    void testPlayerBustTriggersLose() {
        game.startGame();

        // Force player bust: 10 + 10 + 10
        game.getPlayerHand().clear();
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.KING, mock(TextureRegion.class)));
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.QUEEN, mock(TextureRegion.class)));
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.JACK, mock(TextureRegion.class)));

        final boolean[] loseTriggered = {false};
        entity.getEvents().addListener("lose", () -> loseTriggered[0] = true);

        game.drawCard(); // triggers bust
        assertTrue(loseTriggered[0], "Lose event should be triggered when player busts");
    }

    @Test
    void testDealerTurnLogic_PlayerWins() {
        game.startGame();

        // Player higher than dealer
        game.getDealerHand().clear();
        game.getDealerHand().add(new Card(Suit.SPADES, Rank.TEN, mock(TextureRegion.class)));
        game.getDealerHand().add(new Card(Suit.HEARTS, Rank.SEVEN, mock(TextureRegion.class)));

        game.getPlayerHand().clear();
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.TEN, mock(TextureRegion.class)));
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.NINE, mock(TextureRegion.class)));

        final boolean[] winTriggered = {false};
        entity.getEvents().addListener("win", () -> winTriggered[0] = true);

        game.dealerTurn();
        assertTrue(winTriggered[0], "Player should win if hand higher than dealer without busting");
    }

    @Test
    void testDealerTurnLogic_DealerWins() {
        game.startGame();

        game.getDealerHand().clear();
        game.getDealerHand().add(new Card(Suit.HEARTS, Rank.KING, mock(TextureRegion.class)));
        game.getDealerHand().add(new Card(Suit.HEARTS, Rank.NINE, mock(TextureRegion.class)));

        game.getPlayerHand().clear();
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.TEN, mock(TextureRegion.class)));
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.EIGHT, mock(TextureRegion.class)));

        final boolean[] loseTriggered = {false};
        entity.getEvents().addListener("lose", () -> loseTriggered[0] = true);

        game.dealerTurn();
        assertTrue(loseTriggered[0], "Dealer should win if hand higher than player");
    }

    @Test
    void testDealerTurnLogic_Tie() {
        game.startGame();

        game.getPlayerHand().clear();
        game.getDealerHand().clear();

        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.TEN, mock(TextureRegion.class)));
        game.getPlayerHand().add(new Card(Suit.SPADES, Rank.TEN, mock(TextureRegion.class)));
        game.getDealerHand().add(new Card(Suit.CLUBS, Rank.TEN, mock(TextureRegion.class)));
        game.getDealerHand().add(new Card(Suit.DIAMONDS, Rank.TEN, mock(TextureRegion.class)));

        final boolean[] tieTriggered = {false};
        entity.getEvents().addListener("tie", () -> tieTriggered[0] = true);

        game.dealerTurn();
        assertTrue(tieTriggered[0], "Tie event should trigger when hands equal");
    }

    @Test
    void testGetHandValueWithAce() {
        game.startGame();

        game.getPlayerHand().clear();
        // Ace + 9 = 20
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.ACE, mock(TextureRegion.class)));
        game.getPlayerHand().add(new Card(Suit.HEARTS, Rank.NINE, mock(TextureRegion.class)));

        assertEquals(20, game.playerHandValue(), "Ace should count as 11 if it doesn't bust");
    }
}
