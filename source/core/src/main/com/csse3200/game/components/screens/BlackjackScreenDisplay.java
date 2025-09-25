package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.cards.BlackJackGame;
import com.csse3200.game.components.cards.Card;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

public class BlackjackScreenDisplay extends UIComponent {
    private static final float PANEL_W = 800f;
    private static final float PANEL_H = 800f;

    private Image background;
    private Image dimmer;
    private Table root;
    private Label resultLabel;
    private TextButton restartBtn;
    private Table dealerCardsTable;
    private Table playerCardsTable;
    private Label dealerValueLabel;
    private Label playerValueLabel;
    private TextureAtlas atlas;

    private BlackJackGame gameLogic;
    private Texture pixelTex;
    private boolean dealerTurn;



    @Override
    public void create() {
        super.create();
        atlas = ServiceLocator.getResourceService().getAsset("images/cards.atlas", TextureAtlas.class);
        gameLogic = entity.getComponent(BlackJackGame.class);

        pixelTex = makeSolidTexture(Color.WHITE);

       // buildBackground();
        buildRootTable();
        addHeader();
        addDealerSection();
        addPlayerSection();
        addButtons();
        dealerTurn = false;

        // Listen for game events
        entity.getEvents().addListener("playerbust", () -> showRestart("Player Busts! Dealer Wins"));
        entity.getEvents().addListener("dealerbust", () -> showRestart("Dealer Busts! Player Wins"));
        entity.getEvents().addListener("playerwin", () -> showRestart("Player Wins!"));
        entity.getEvents().addListener("dealerwin", () -> showRestart("Dealer Wins!"));
        entity.getEvents().addListener("tie", () -> showRestart("It's a Tie!"));
        entity.getEvents().addListener("interact", this::show);
        entity.getEvents().addListener("hide", this::hide);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage does the drawing
    }

    @Override
    public void dispose() {
        if (background != null) {
            background.remove();
            background = null;
        }
        if (pixelTex != null) {
            pixelTex.dispose();
            pixelTex = null;
        }
        if (root != null) {
            root.remove();
            root = null;
        }
        if (dimmer != null) {
            dimmer.remove();
            dimmer = null;
        }
        super.dispose();
    }
/*
    private void buildBackground() {
        background = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        background.setSize(PANEL_W, PANEL_H);
        background.setPosition(
                (stage.getWidth() - PANEL_W) / 2f,
                (stage.getHeight() - PANEL_H) / 2f
        );
        background.setColor(Color.GREEN);
        stage.addActor(background);
    } */

    private void buildRootTable() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(
                (stage.getWidth() - PANEL_W) / 2f,
                (stage.getHeight() - PANEL_H) / 2f
        );
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        dimmer.setFillParent(true);
        dimmer.setColor(0f, 0f, 0f, 0.6f);
        stage.addActor(dimmer);

        // Set background directly on the table
        root.setBackground(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        root.getBackground().setMinWidth(PANEL_W);
        root.getBackground().setMinHeight(PANEL_H);
        // Set background color
        root.setColor(Color.OLIVE);

        root.top().pad(20);
        root.defaults().pad(10);
        stage.addActor(root);
    }

    private void addHeader() {
        // Title style
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = Color.WHITE;

        // Title label
        Label title = new Label("Blackjack", titleStyle);
        title.setFontScale(1.8f);
        root.add(title).padBottom(4).row(); // smaller padding so underline is closer

        // Underline as a solid dark gray line
        Image underline = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        underline.setColor(Color.DARK_GRAY); // dark gray for subtle contrast
        root.add(underline)
                .width(PANEL_W - 40f)
                .height(2f)   // 2px height
                .padBottom(12f) // space below underline
                .row();
    }


    private void addDealerSection() {
        root.add(new Label("Dealer:", skin)).left().row();

        dealerCardsTable = new Table();
        root.add(dealerCardsTable).left().row();

        dealerValueLabel = new Label("", skin);
        root.add(dealerValueLabel).left().padTop(5f).row();
    }

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


    private void addButtons() {
        TextButton hitBtn = new TextButton("Hit", skin);
        TextButton standBtn = new TextButton("Stand", skin);
        TextButton closeBtn = new TextButton("Close", skin);

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

        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                // Reset game when closing
                entity.getEvents().trigger("start");
            }
        });

        // Restart button (hidden initially)
        restartBtn = new TextButton("Restart", skin);
        restartBtn.setVisible(false);
        restartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dealerTurn = false;
                restartBtn.setVisible(false);  // hide button again
                resultLabel.setText("");        // clear previous result
                entity.getEvents().trigger("start"); // start new game
                updateHands();
            }
        });

// Add restart button below the result label
        root.add(restartBtn).padTop(10f).center().row();


        // Put Hit + Stand side by side
        Table buttonRow = new Table();
        buttonRow.add(hitBtn).padRight(10f);
        buttonRow.add(standBtn);

        root.add(buttonRow).padTop(15f).row();

        // Close button centered below
        root.add(closeBtn).padTop(10f).center().row();
    }

    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        if (background != null) background.setVisible(true);
        if (root != null) root.setVisible(true);
        if (dimmer != null) dimmer.setVisible(true);

        // Start new round
        entity.getEvents().trigger("start");
        updateHands();
    }


    public void hide() {
        ServiceLocator.getTimeSource().setPaused(false);
        if (background != null) background.setVisible(false);
        if (root != null) root.setVisible(false);
        if (dimmer != null) dimmer.setVisible(false);
        resultLabel.setText("");
    }


    private void updateHands() {
        dealerCardsTable.clearChildren();
        playerCardsTable.clearChildren();

        // Dealer’s cards
        if (!dealerTurn) {
            // Only show dealer's first card
            if (!gameLogic.getDealerHand().isEmpty()) {
                TextureRegion firstCard = gameLogic.getDealerHand().getFirst().getTexture();
                Image cardImage = new Image(new TextureRegionDrawable(firstCard));
                cardImage.setSize(72, 96);
                dealerCardsTable.add(cardImage).size(72,96).pad(3f);
                TextureRegion hiddenCard = atlas.findRegion("facedown");
                cardImage = new Image(new TextureRegionDrawable(hiddenCard));
                cardImage.setSize(72, 96);
                dealerCardsTable.add(cardImage).size(72, 96).pad(3f);
            }
            dealerValueLabel.setText("Value: " + gameLogic.getDealerHand().getFirst().getValue());
        } else {
            for (Card card : gameLogic.getDealerHand()) {
                TextureRegion texture = card.getTexture();
                Image cardImage = new Image(new TextureRegionDrawable(texture));
                cardImage.setSize(72, 96);
                dealerCardsTable.add(cardImage).size(72,96).pad(3f);
            }
            dealerValueLabel.setText("Value: " + gameLogic.dealerHandValue());
        }

        // Player’s cards
        for (Card card : gameLogic.getPlayerHand()) {
            TextureRegion texture = card.getTexture();
            Image cardImage = new Image(new TextureRegionDrawable(texture));
            cardImage.setSize(72, 96);
            playerCardsTable.add(cardImage).size(72,96).pad(3f);
        }
        playerValueLabel.setText("Value: " + gameLogic.playerHandValue());
    }


    private void setResult(String msg) {
        resultLabel.setText(msg);
    }

    private static Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    public Stage getStage() {
        return stage;
    }

    private void showRestart(String msg) {
        dealerTurn = true;
        updateHands();
        setResult(msg);         // show result
        restartBtn.setVisible(true); // show restart button
    }



}
