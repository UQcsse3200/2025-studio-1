package com.csse3200.game.components.minigames;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.cards.Card;
import com.csse3200.game.components.cards.Hand;
import com.csse3200.game.components.cards.Rank;
import com.csse3200.game.components.cards.Suit;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BlackJackGame using the latest component structure.
 */
class BlackJackGameTest {

    private BlackJackGame game;
    private Entity entity;
    private TextureAtlas.AtlasRegion dummyTexture;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        // Create a dummy texture to avoid loading real assets
        dummyTexture = mock(TextureAtlas.AtlasRegion.class);
        TextureAtlas fakeAtlas = new TextureAtlas() {
            @Override
            public AtlasRegion findRegion(String name) {
                return dummyTexture;
            }
        };

        ResourceService mockResources = mock(ResourceService.class);
        when(mockResources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(fakeAtlas);
        ServiceLocator.registerResourceService(mockResources);
        ServiceLocator.registerTimeSource(new GameTime());

        entity = new Entity();
        game = new BlackJackGame();
        entity.addComponent(game);
        game.create();
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void testDeckIsInitialized() {
        assertNotNull(game.getDeck(), "Deck should be created on game creation");
        assertTrue(game.getDeck().cardsRemaining() > 0, "Deck should contain cards");
    }

    @Test
    void testStartGameDealsCards() {
        game.startGame();

        Hand current = game.getCurrentHand();
        assertEquals(2, current.getCards().size(), "Player hand should have 2 cards at start");
        assertEquals(2, game.getDealerHand().getCards().size(), "Dealer hand should have 2 cards at start");
    }

    @Test
    void testDrawCardAddsCardToCurrentHand() {
        game.startGame();
        Hand current = game.getCurrentHand();
        int initialSize = current.getCards().size();

        entity.getEvents().trigger("drawCard");
        assertEquals(initialSize + 1, current.getCards().size(), "Drawing a card adds to current hand");
    }

    @Test
    void testPlayerBustTriggersLoseEvent() {
        game.startGame();

        Hand current = game.getCurrentHand();
        current.getCards().clear();
        current.addCard(new Card(Suit.HEARTS, Rank.KING, dummyTexture));
        current.addCard(new Card(Suit.HEARTS, Rank.QUEEN, dummyTexture));
        current.addCard(new Card(Suit.HEARTS, Rank.JACK, dummyTexture));

        final boolean[] loseTriggered = {false};
        entity.getEvents().addListener("lose", () -> loseTriggered[0] = true);

        game.drawCard(); // forces bust check
        assertTrue(loseTriggered[0], "Lose event should trigger when player busts");
    }

    @Test
    void testDealerTurn_PlayerWins() {
        game.startGame();

        Hand dealer = new Hand();
        dealer.addCard(new Card(Suit.CLUBS, Rank.SEVEN, dummyTexture));
        dealer.addCard(new Card(Suit.CLUBS, Rank.TEN, dummyTexture));
        game.getDealerHand().getCards().clear();
        game.getDealerHand().getCards().addAll(dealer.getCards());

        Hand player = game.getCurrentHand();
        player.getCards().clear();
        player.addCard(new Card(Suit.HEARTS, Rank.NINE, dummyTexture));
        player.addCard(new Card(Suit.HEARTS, Rank.TEN, dummyTexture));

        final boolean[] winTriggered = {false};
        entity.getEvents().addListener("win", () -> winTriggered[0] = true);

        game.dealerPlay();
        assertTrue(winTriggered[0], "Player should win if hand beats dealer");
    }

    @Test
    void testDealerTurn_DealerWins() {
        game.startGame();

        Hand dealer = game.getDealerHand();
        dealer.getCards().clear();
        dealer.addCard(new Card(Suit.HEARTS, Rank.QUEEN, dummyTexture));
        dealer.addCard(new Card(Suit.HEARTS, Rank.NINE, dummyTexture));

        Hand player = game.getCurrentHand();
        player.getCards().clear();
        player.addCard(new Card(Suit.HEARTS, Rank.TEN, dummyTexture));
        player.addCard(new Card(Suit.HEARTS, Rank.EIGHT, dummyTexture));

        final boolean[] loseTriggered = {false};
        entity.getEvents().addListener("lose", () -> loseTriggered[0] = true);

        game.dealerPlay();
        assertTrue(loseTriggered[0], "Dealer should win if hand beats player");
    }

    @Test
    void testDealerTurn_TieTriggersTieEvent() {
        game.startGame();

        Hand dealer = game.getDealerHand();
        dealer.getCards().clear();
        dealer.addCard(new Card(Suit.CLUBS, Rank.TEN, dummyTexture));
        dealer.addCard(new Card(Suit.CLUBS, Rank.EIGHT, dummyTexture));

        Hand player = game.getCurrentHand();
        player.getCards().clear();
        player.addCard(new Card(Suit.HEARTS, Rank.TEN, dummyTexture));
        player.addCard(new Card(Suit.HEARTS, Rank.EIGHT, dummyTexture));

        final boolean[] tieTriggered = {false};
        entity.getEvents().addListener("tie", () -> tieTriggered[0] = true);

        game.dealerPlay();
        assertTrue(tieTriggered[0], "Tie event should trigger when hands equal");
    }

    @Test
    void testAceValueCountsAs11Or1() {
        game.startGame();
        Hand current = game.getCurrentHand();
        current.getCards().clear();

        current.addCard(new Card(Suit.HEARTS, Rank.ACE, dummyTexture));
        current.addCard(new Card(Suit.CLUBS, Rank.NINE, dummyTexture));
        assertEquals(20, current.getValue(), "Ace should count as 11 if it doesn't bust");

        current.addCard(new Card(Suit.DIAMONDS, Rank.KING, dummyTexture));
        assertEquals(20, current.getValue(), "Ace should count as 1 if adding 11 would bust");
    }

    @Test
    void testSplitHandCreatesNewHand() {
        game.startGame();

        Hand current = game.getCurrentHand();
        current.getCards().clear();
        current.addCard(new Card(Suit.HEARTS, Rank.EIGHT, dummyTexture));
        current.addCard(new Card(Suit.SPADES, Rank.EIGHT, dummyTexture));

        int before = game.getPlayerHands().size();
        game.splitHand();

        assertEquals(before + 1, game.getPlayerHands().size(), "Split should create an additional hand");
    }

    @Test
    void testDoubleDownAddsCardAndMovesNextHand() {
        game.startGame();
        Hand current = game.getCurrentHand();
        int initialSize = current.getCards().size();

        game.doubleDown();
        assertEquals(initialSize + 1, current.getCards().size(), "Double down should add one card");
        assertTrue(game.isDealerTurn() || game.getActiveHandIndex() > 0, "After double down, next hand or dealer turn begins");
    }
}
