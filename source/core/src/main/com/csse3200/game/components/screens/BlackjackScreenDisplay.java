package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.cards.Card;
import com.csse3200.game.components.minigames.BlackJackGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * A UI component that displays the Blackjack game screen, including
 * the dealer and player hands, game results, and action buttons.
 * <p>
 * This class is responsible for rendering and updating all visual
 * elements of the Blackjack mini-game, while delegating logic and
 * state handling to {@link BlackJackGame}.
 * </p>
 */
public class BlackjackScreenDisplay extends UIComponent {

    private static final float PANEL_W = 800f;
    private static final float PANEL_H = 800f;

    private Image background;
    private Image dimmer;
    private Table root;
    private Label resultLabel;
    private TextButton exitBtn;
    private TextButton hitBtn;
    private TextButton standBtn;
    private Table dealerCardsTable;
    private Table playerCardsTable;
    private Label dealerValueLabel;
    private Label playerValueLabel;
    private TextureAtlas atlas;

    private BlackJackGame gameLogic;
    private Texture pixelTex;
    private boolean dealerTurn;

    /**
     * Utility to create a 1x1 solid color texture.
     *
     * @param color the color to fill the texture with
     * @return a new {@link Texture} of the given color
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
     * Sets up the game area, button listeners, and event triggers.
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
        dealerTurn = false;

        // Game event listeners
        entity.getEvents().addListener("playerbust", () -> showRestart("Player Busts! Dealer Wins"));
        entity.getEvents().addListener("dealerbust", () -> showRestart("Dealer Busts! Player Wins"));
        entity.getEvents().addListener("playerWin", () -> showRestart("Player Wins!"));
        entity.getEvents().addListener("dealerWin", () -> showRestart("Dealer Wins!"));
        entity.getEvents().addListener("tie", () -> showRestart("It's a Tie!"));
        entity.getEvents().addListener("hide", this::hide);
        entity.getEvents().addListener("betPlaced", this::show);

        hide();
    }

    /**
     * The stage handles rendering for this component.
     */
    @Override
    public void draw(SpriteBatch batch) {
        // Stage handles rendering
    }

    /**
     * Cleans up and disposes of all UI elements and textures.
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
     * Builds the root container for all UI components.
     * Adds a dimmed overlay background and prepares the main layout.
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
        root.setColor(Color.OLIVE);
        root.top().pad(20);
        root.defaults().pad(10);
        stage.addActor(root);
    }

    /**
     * Adds the title and underline header for the Blackjack screen.
     */
    private void addHeader() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = Color.WHITE;

        Label title = new Label("Blackjack", titleStyle);
        title.setFontScale(1.8f);
        root.add(title).padBottom(4).row();

        // Underline
        Image underline = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        underline.setColor(Color.DARK_GRAY);
        root.add(underline)
                .width(PANEL_W - 40f)
                .height(2f)
                .padBottom(12f)
                .row();
    }

    /**
     * Adds the dealer section including their cards and hand value.
     */
    private void addDealerSection() {
        root.add(new Label("Dealer:", skin)).left().row();
        dealerCardsTable = new Table();
        root.add(dealerCardsTable).left().row();
        dealerValueLabel = new Label("", skin);
        root.add(dealerValueLabel).left().padTop(5f).row();
    }

    /**
     * Adds the player section including their cards, hand value,
     * and result messages.
     */
    private void addPlayerSection() {
        root.add(new Label("Player:", skin)).left().row();
        playerCardsTable = new Table();
        root.add(playerCardsTable).left().row();
        playerValueLabel = new Label("", skin);
        root.add(playerValueLabel).left().padTop(5f).row();

        resultLabel = new Label("", skin);
        resultLabel.setColor(Color.YELLOW);
        root.add(resultLabel).padTop(10f).row();
    }

    /**
     * Adds Hit, Stand, and Continue buttons to control game actions.
     * Buttons trigger events handled by {@link BlackJackGame}.
     */
    private void addButtons() {
        hitBtn = new TextButton("Hit", skin);
        standBtn = new TextButton("Stand", skin);

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
                dealerTurn = true;
                entity.getEvents().trigger("stand");
                updateHands();
            }
        });

        // Continue button shown after round ends
        exitBtn = new TextButton("Continue", skin);
        exitBtn.setVisible(false);
        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dealerTurn = false;
                exitBtn.setVisible(false);
                resultLabel.setText("");
                entity.getEvents().trigger("interact");
                hide();
            }
        });

        root.add(exitBtn).padTop(10f).center().row();

        // Arrange Hit/Stand side by side
        Table buttonRow = new Table();
        buttonRow.add(hitBtn).padRight(10f);
        buttonRow.add(standBtn);
        root.add(buttonRow).padTop(15f).row();
    }

    /**
     * Displays the Blackjack screen, initializes a new round,
     * and pauses game time.
     */
    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        if (background != null) background.setVisible(true);
        if (root != null) root.setVisible(true);
        if (dimmer != null) dimmer.setVisible(true);

        gameLogic.startGame();
        updateHands();
        hitBtn.setVisible(true);
        standBtn.setVisible(true);
    }

    /**
     * Hides the Blackjack screen and resumes game time.
     */
    public void hide() {
        ServiceLocator.getTimeSource().setPaused(false);
        if (background != null) background.setVisible(false);
        if (root != null) root.setVisible(false);
        if (dimmer != null) dimmer.setVisible(false);
        resultLabel.setText("");
    }

    /**
     * Updates the card images and hand values for both dealer and player.
     * Handles logic for hiding the dealer’s second card until the player's turn ends.
     */
    private void updateHands() {
        dealerCardsTable.clearChildren();
        playerCardsTable.clearChildren();

        // Dealer’s cards
        if (!dealerTurn) {
            if (!gameLogic.getDealerHand().isEmpty()) {
                TextureRegion firstCard = gameLogic.getDealerHand().getFirst().getTexture();
                Image cardImage = new Image(new TextureRegionDrawable(firstCard));
                dealerCardsTable.add(cardImage).size(72, 96).pad(3f);

                TextureRegion hiddenCard = atlas.findRegion("facedown");
                dealerCardsTable.add(new Image(new TextureRegionDrawable(hiddenCard))).size(72, 96).pad(3f);
            }
            dealerValueLabel.setText("Value: " + gameLogic.getDealerHand().getFirst().getValue());
        } else {
            for (Card card : gameLogic.getDealerHand()) {
                TextureRegion texture = card.getTexture();
                dealerCardsTable.add(new Image(new TextureRegionDrawable(texture))).size(72, 96).pad(3f);
            }
            dealerValueLabel.setText("Value: " + gameLogic.dealerHandValue());
        }

        // Player’s cards
        for (Card card : gameLogic.getPlayerHand()) {
            TextureRegion texture = card.getTexture();
            playerCardsTable.add(new Image(new TextureRegionDrawable(texture))).size(72, 96).pad(3f);
        }
        playerValueLabel.setText("Value: " + gameLogic.playerHandValue());
    }

    /**
     * Displays a round result message and enables the "Continue" button.
     *
     * @param msg the message to display to the player
     */
    private void showRestart(String msg) {
        dealerTurn = true;
        updateHands();
        resultLabel.setText(msg);
        exitBtn.setVisible(true);
        hitBtn.setVisible(false);
        standBtn.setVisible(false);
    }

    /**
     * Returns the stage used by this UI component.
     *
     * @return the {@link Stage} instance
     */
    public Stage getStage() {
        return stage;
    }
}
