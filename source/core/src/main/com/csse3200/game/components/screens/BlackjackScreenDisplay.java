package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.cards.BlackJackGame;
import com.csse3200.game.ui.UIComponent;

public class BlackjackScreenDisplay extends UIComponent {
    private static final float PANEL_W = 600f;
    private static final float PANEL_H = 400f;

    private Image background;
    private Table root;
    private Label dealerLabel;
    private Label playerLabel;
    private Label resultLabel;
    private TextButton restartBtn;

    private BlackJackGame gameLogic;
    private Texture pixelTex;
    private boolean dealerTurn;



    @Override
    public void create() {
        super.create();
        gameLogic = entity.getComponent(BlackJackGame.class);

        pixelTex = makeSolidTexture(Color.WHITE);

        buildBackground();
        buildRootTable();
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
        super.dispose();
    }

    private void buildBackground() {
        background = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        background.setSize(PANEL_W, PANEL_H);
        background.setPosition(
                (stage.getWidth() - PANEL_W) / 2f,
                (stage.getHeight() - PANEL_H) / 2f
        );
        background.setColor(Color.GREEN);
        stage.addActor(background);
    }

    private void buildRootTable() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(20);
        root.defaults().pad(10);
        stage.addActor(root);
    }

    private void addDealerSection() {
        dealerLabel = new Label("Dealer: ", skin);
        root.add(dealerLabel).row();
    }

    private void addPlayerSection() {
        playerLabel = new Label("Player: ", skin);
        root.add(playerLabel).row();

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
        if (background != null) background.setVisible(true);
        if (root != null) root.setVisible(true);

        // Start new round
        entity.getEvents().trigger("start");
        updateHands();
    }


    public void hide() {
        if (background != null) background.setVisible(false);
        if (root != null) root.setVisible(false);
        resultLabel.setText("");
    }


    private void updateHands() {
        if (!dealerTurn) {
            dealerLabel.setText("Dealer: " + gameLogic.getDealerHand().getFirst() +
                    " (" + gameLogic.getDealerHand().getFirst().getValue() + ")");
        } else {
            dealerLabel.setText("Dealer: " + gameLogic.getDealerHand() +
                    " (" + gameLogic.dealerHandValue() + ")");
        }
        playerLabel.setText("Player: " + gameLogic.getPlayerHand() +
                " (" + gameLogic.playerHandValue() + ")");
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
        setResult(msg);         // show result
        restartBtn.setVisible(true); // show restart button
    }

}
