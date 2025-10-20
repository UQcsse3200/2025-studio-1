package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.cards.Card;
import com.csse3200.game.components.cards.Hand;
import com.csse3200.game.components.minigames.BlackJackGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

import java.util.List;

/**
 * UI component that displays the Blackjack game screen.
 * <p>
 * Handles all visual elements for the Blackjack mini-game including:
 * dealer/player hands, results, and action buttons.
 * Game logic/state is delegated to {@link BlackJackGame}.
 * </p>
 */
public class BlackjackScreenDisplay extends UIComponent {

    private static final float PANEL_W = 800f;
    private static final float PANEL_H = 1000f;
    private final int TOTAL_CARDS = 104;
    private final String valueLabel = "Value: ";

    /**
     * Background panel of the game screen.
     */
    private Image background;

    /**
     * Semi-transparent overlay behind the panel.
     */
    private Image dimmer;

    /**
     * Root table containing all UI elements.
     */
    private Table root;

    /**
     * Displays round results or messages.
     */
    private Label resultLabel;

    /**
     * Buttons for player actions.
     */
    private TextButton exitBtn;
    private TextButton hitBtn;
    private TextButton standBtn;
    private TextButton splitBtn;
    private TextButton doubleBtn;

    /**
     * Tables displaying dealer and player cards.
     */
    private Table dealerCardsTable;
    private Table playerCardsTable;
    private Table resultsTable;

    /**
     * Label showing dealer hand value.
     */
    private Label dealerValueLabel;

    /**
     * Label showing remaining cards in the deck.
     */
    private Label cardsRemainingLabel;

    /**
     * Texture atlas containing card images.
     */
    private TextureAtlas atlas;

    /**
     * Handles Blackjack game logic and rules.
     */
    private BlackJackGame gameLogic;

    /**
     * Solid-color texture used for backgrounds and highlights.
     */
    private Texture pixelTex;

    /**
     * Utility method to create a 1x1 solid color texture.
     *
     * @param color the color to fill the texture with
     * @return a new {@link Texture} of the specified color
     */
    private static Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Creates and initializes all visual elements of the Blackjack screen.
     * Registers listeners for game events such as hide, displayResults, split, and double.
     */
    @Override
    public void create() {
        super.create();
        atlas = ServiceLocator.getResourceService().getAsset("images/cards.atlas", TextureAtlas.class);
        gameLogic = entity.getComponent(BlackJackGame.class);
        pixelTex = makeSolidTexture(Color.WHITE);

        buildRootTable();
        addHeader();
        addDealerSection();
        addPlayerSection();
        addButtons();

        // Event listeners for game interactions
        entity.getEvents().addListener("hide", this::hide);
        entity.getEvents().addListener("betPlaced", this::show);
        entity.getEvents().addListener("displayResults", this::showResults);
        entity.getEvents().addListener("splitSuccess", this::split);
        entity.getEvents().addListener("doubleSuccess", this::doubleDown);

        hide();
    }

    /**
     * Stage handles rendering, so no custom draw code is required here.
     */
    @Override
    public void draw(SpriteBatch batch) {
        // Stage handles rendering
    }

    /**
     * Cleans up all UI elements and textures.
     */
    @Override
    public void dispose() {
        if (background != null) background.remove();
        if (root != null) root.remove();
        if (dimmer != null) dimmer.remove();
        if (pixelTex != null) pixelTex.dispose();

        background = null;
        root = null;
        dimmer = null;
        pixelTex = null;

        super.dispose();
    }

    /**
     * Builds the root container table and dimmer overlay.
     * Prepares the main layout and top row for deck info.
     */
    private void buildRootTable() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition((stage.getWidth() - PANEL_W) / 2f, (stage.getHeight() - PANEL_H) / 2f);

        // Dimmer overlay
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        dimmer.setFillParent(true);
        dimmer.setColor(0f, 0f, 0f, 0.6f);
        stage.addActor(dimmer);

        // Table background
        root.setBackground(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        root.setColor(Color.DARK_GRAY);
        root.top().pad(20);
        root.defaults().pad(10);
        stage.addActor(root);

        root.top();

        // Deck info label
        cardsRemainingLabel = new Label("Cards: " + TOTAL_CARDS, skin);
        cardsRemainingLabel.setFontScale(1.1f);
        root.add(cardsRemainingLabel).expandX().right().pad(10).row();
    }

    /**
     * Adds the title and underline for the screen.
     */
    private void addHeader() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = Color.WHITE;

        Label title = new Label("Blackjack", titleStyle);
        title.setFontScale(1.8f);
        root.add(title).padBottom(4).row();

        Image underline = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        underline.setColor(Color.DARK_GRAY);
        root.add(underline).width(PANEL_W - 40f).height(2f).padBottom(12f).row();
    }

    /**
     * Adds dealer section including cards table and hand value label.
     */
    private void addDealerSection() {
        root.add(new Label("Dealer:", skin)).left().row();
        dealerCardsTable = new Table();
        root.add(dealerCardsTable).left().row();
        dealerValueLabel = new Label("", skin);
        root.add(dealerValueLabel).left().padTop(5f).row();
    }

    /**
     * Adds player section including card table, hand values, and result messages.
     */
    private void addPlayerSection() {
        root.add(new Label("Player:", skin)).left().row();

        playerCardsTable = new Table();
        root.add(playerCardsTable).left().row();

        resultsTable = new Table();
        root.add(resultsTable).left().row();

        resultLabel = new Label("", skin);
        resultLabel.setColor(Color.YELLOW);
        root.add(resultLabel).padTop(10f).row();
    }

    /**
     * Adds action buttons (Hit, Stand, Split, Double, Continue) with listeners.
     */
    private void addButtons() {
        hitBtn = new TextButton("Hit", skin);
        standBtn = new TextButton("Stand", skin);
        splitBtn = new TextButton("Split", skin);
        doubleBtn = new TextButton("Double", skin);
        exitBtn = new TextButton("Continue", skin);
        exitBtn.setVisible(false);

        hitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("drawCard");
                updateHands();
            }
        });

        standBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("stand");
                updateHands();
            }
        });

        splitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("split");
                updateHands();
            }
        });

        doubleBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("double");
                updateHands();
            }
        });

        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                exitBtn.setVisible(false);
                resultLabel.setText("");
                entity.getEvents().trigger("interact");
                hide();
            }
        });

        Table buttonRow = new Table();
        buttonRow.add(hitBtn).padRight(10f);
        buttonRow.add(standBtn).padRight(10f);
        buttonRow.add(splitBtn).padRight(10f);
        buttonRow.add(doubleBtn).padRight(10f);
        root.add(buttonRow).padTop(15f).row();

        root.add(exitBtn).padTop(10f).center().row();
    }

    /**
     * Displays the Blackjack screen and starts a new round.
     */
    public void show() {
        if (background != null) background.setVisible(true);
        if (root != null) root.setVisible(true);
        if (dimmer != null) dimmer.setVisible(true);

        hitBtn.setVisible(true);
        standBtn.setVisible(true);
        gameLogic.startGame();
        resultsTable.clearChildren();
        updateHands();
    }

    /**
     * Hides the Blackjack screen and clears messages.
     */
    public void hide() {
        if (background != null) background.setVisible(false);
        if (root != null) root.setVisible(false);
        if (dimmer != null) dimmer.setVisible(false);
        resultLabel.setText("");
    }

    /**
     * Updates the displayed dealer and player hands, including hidden cards.
     * Also updates deck remaining count.
     */
    private void updateHands() {
        dealerCardsTable.clearChildren();
        playerCardsTable.clearChildren();

        // Dealer cards
        if (!gameLogic.isDealerTurn()) {
            splitBtn.setVisible(gameLogic.getCurrentHand().canSplit());
            doubleBtn.setVisible(gameLogic.getCurrentHand().canDouble());

            if (!gameLogic.getDealerHand().isEmpty()) {
                Image firstCard = new Image(new TextureRegionDrawable(
                        gameLogic.getDealerHand().getCards().getFirst().getTexture()));
                dealerCardsTable.add(firstCard).size(72, 96).pad(3f);

                TextureRegion hiddenCard = atlas.findRegion("facedown");
                dealerCardsTable.add(new Image(new TextureRegionDrawable(hiddenCard)))
                        .size(72, 96).pad(3f);
            }
            dealerValueLabel.setText(valueLabel + gameLogic.getDealerHand().getCards().getFirst().getValue());
        } else {
            for (Card card : gameLogic.getDealerHand().getCards()) {
                dealerCardsTable.add(new Image(new TextureRegionDrawable(card.getTexture())))
                        .size(72, 96).pad(3f);
            }
            dealerValueLabel.setText(valueLabel + gameLogic.dealerHandValue());
        }

        displayPlayerHand();
        cardsRemainingLabel.setText("Cards: " + gameLogic.getDeck().cardsRemaining());
    }

    /**
     * Displays all player hands, highlighting the active hand.
     */
    private void displayPlayerHand() {
        int activeIndex = gameLogic.getActiveHandIndex();
        int index = 0;
        playerCardsTable.clearChildren();

        for (Hand hand : gameLogic.getPlayerHands()) {
            Table handTable = new Table();

            if (index == activeIndex) {
                handTable.setBackground(new TextureRegionDrawable(new TextureRegion(pixelTex)).tint(Color.OLIVE));
                handTable.addAction(Actions.forever(Actions.sequence(
                        Actions.alpha(0.6f, 0.6f),
                        Actions.alpha(0.9f, 0.6f)
                )));
            } else {
                handTable.setBackground(new TextureRegionDrawable(new TextureRegion(pixelTex)).tint(
                        new Color(0f, 0f, 0f, 0.25f)));
            }

            Table cardsTable = new Table();
            for (Card card : hand.getCards()) {
                cardsTable.add(new Image(new TextureRegionDrawable(card.getTexture())))
                        .size(72, 96).pad(3f);
            }

            Label handValueLabel = new Label(valueLabel + hand.getValue(), skin);
            handValueLabel.setColor(Color.WHITE);

            handTable.add(cardsTable).left().padRight(10f);
            handTable.add(handValueLabel).left().padLeft(5f);

            playerCardsTable.add(handTable).left().padBottom(15f).row();
            index++;
        }
    }

    /**
     * Displays the results of a round and enables the Continue button.
     *
     * @param results list of strings describing each hand's outcome
     */
    private void showResults(List<String> results) {
        resultsTable.clearChildren();

        for (String msg : results) {
            Label msgLabel = new Label(msg, skin);
            msgLabel.setColor(Color.YELLOW);
            resultsTable.add(msgLabel).left().padBottom(5f).row();
        }

        exitBtn.setVisible(true);
        hitBtn.setVisible(false);
        standBtn.setVisible(false);
        splitBtn.setVisible(false);
        doubleBtn.setVisible(false);
    }

    /**
     * Helper to split the current hand and update the UI.
     */
    private void split() {
        gameLogic.splitHand();
        updateHands();
    }

    /**
     * Helper to double the current hand's bet and update the UI.
     */
    private void doubleDown() {
        gameLogic.doubleDown();
    }

    /**
     * Returns the stage used by this UI component.
     */
    public Stage getStage() {
        return stage;
    }
}
