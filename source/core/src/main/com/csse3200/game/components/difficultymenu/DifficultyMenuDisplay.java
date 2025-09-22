package com.csse3200.game.components.difficultymenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.areas.difficulty.DifficultyType;;

/**
 * A ui component for displaying the Main menu.
 */
public class DifficultyMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
    private static final float Z_INDEX = 2f;
    private final Difficultytype diffType;
    private Table table;
    private NeonStyles neon;
    private final GdxGame game;

    public MainMenuDisplay(GdxGame game) {
        this.game = game;
    }

    /**
     * Initialises styles and builds the actors.
     */
    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.70f);
        addActors();

    }

    /**
     * Creates the logo and buttons, sizes them relative to the stage, and wires
     * button events.
     */
    private void addActors() {
        table = new Table();
        table.setFillParent(true);

        // Column position
        float leftPad = stage.getWidth() * 0.12f;
        table.center().left().padLeft(leftPad);

        // Logo image
        Image title =
                new Image(
                        ServiceLocator.getResourceService()
                                .getAsset("images/logo.png", Texture.class));
        logger.debug("Logo image added");

        // Button sizing relative to screen
        float btnW = stage.getWidth() * 0.34f;
        float btnH = Math.max(64f, stage.getHeight() * 0.08f);
        table.defaults().width(btnW).height(btnH);

        TextButton.TextButtonStyle style = neon.buttonRounded();

        // Create buttons
        TextButton EasyBtn = new TextButton("Easy", style);
        TextButton MediumBtn = new TextButton("Medium", style);
        TextButton HardBtn = new TextButton("Hard", style);
        TextButton InsaneBtn = new TextButton("Insane", style);

        // Label text size
        EasyBtn.getLabel().setFontScale(2.0f);
        MediumBtn.getLabel().setFontScale(2.0f);
        HardBtn.getLabel().setFontScale(2.0f);
        InsaneBtn.getLabel().setFontScale(2.0f);

        // Button actions
        EasyBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Difficulty set to easy");
                        entity.getEvents().trigger("setDiffEasy");
                    }
                });

        MediumBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Load button clicked");
                        entity.getEvents().trigger("setDiffMedium");
                    }
                });

        settingsBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Settings button clicked");
                        entity.getEvents().trigger("settings");
                    }
                });

        exitBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Exit button clicked");
                        entity.getEvents().trigger("exit");
                    }
                });

        tutorialBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                logger.debug("Tutorial button clicked");
                entity.getEvents().trigger("tutorial");
            }
        });

        // Column layout
        table.add(title).left().padBottom(40f).padLeft(-10f);
        table.row();
        table.add(startBtn).padTop(15f).left();
        table.row();
        table.add(loadBtn).padTop(15f).left();
        table.row();
        table.add(settingsBtn).padTop(15f).left();
        table.row();
        table.add(exitBtn).padTop(15f).left();
        table.row();
        table.add(tutorialBtn).padTop(15f).left();
        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }

    /**
     * Removes and clears the root table.
     */
    @Override
    public void dispose() {
        table.clear();
        super.dispose();
    }

    /**
     * setter method for the diffType
     * @param diffType sets this.diffType
     */
    public void setDiffType(DifficultyType diffType) {
        this.diffType = diffType;
    }

    /**
     * Returns this.diffType
     * @return DifficultyType this.diffType
     */
    public DifficultyType getDiffType() {
        return this.diffType;
    }
}
