package com.csse3200.game.components.difficultymenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.areas.difficulty.DifficultyType;
import com.csse3200.game.components.mainmenu.MainMenuDisplay;;

/**
 * A ui component for displaying the Main menu.
 */
public class DifficultyMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
    private static final float Z_INDEX = 2f;
    private Table table;
    private NeonStyles neon;
    private final GdxGame game;

    public DifficultyMenuDisplay(GdxGame game) {
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
        TextButton easyBtn = new TextButton("Easy", style);
        TextButton normalBtn = new TextButton("Normal", style);
        TextButton hardBtn = new TextButton("Hard", style);
        TextButton insaneBtn = new TextButton("Insane", style);
        TextButton applyBtn = new TextButton("Apply", style);

        // Label text size
        easyBtn.getLabel().setFontScale(2.0f);
        normalBtn.getLabel().setFontScale(2.0f);
        hardBtn.getLabel().setFontScale(2.0f);
        insaneBtn.getLabel().setFontScale(2.0f);
        applyBtn.getLabel().setFontScale(2.0f);

        // Button actions
        easyBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Difficulty set to easy");
                        entity.getEvents().trigger("setDiffEasy");
                    }
                });

        normalBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Difficulty set to normal");
                        entity.getEvents().trigger("setDiffNormal");
                    }
                });

        hardBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Difficulty set to hard");
                        entity.getEvents().trigger("setDiffHard");
                    }
                });

        insaneBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Difficulty set to insane");
                        entity.getEvents().trigger("setDiffInsane");
                    }
                });

        applyBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Exited Difficulty Set Screen");
                        entity.getEvents().trigger("applyDiff");
                    }
                });

        // Column layout
        table.add(title).left().padBottom(40f).padLeft(-10f);
        table.row();
        table.add(easyBtn).padTop(15f).left();
        table.row();
        table.add(normalBtn).padTop(15f).left();
        table.row();
        table.add(hardBtn).padTop(15f).left();
        table.row();
        table.add(insaneBtn).padTop(15f).left();
        table.row();
        table.add(applyBtn).padTop(15f).left();
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
}
