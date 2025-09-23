package com.csse3200.game.components.difficultymenu;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
import com.csse3200.game.areas.difficulty.Difficulty;
import com.csse3200.game.areas.difficulty.DifficultyType;
import com.csse3200.game.components.mainmenu.MainMenuDisplay;

/**
 * A ui component for displaying the Main menu.
 */
public class DifficultyMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
    private static final float Z_INDEX = 2f;
    private Table table;
    private NeonStyles neon;
    private final GdxGame game;
    private DifficultyType diffType;

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
                        diffEasy();
                    }
                });

        normalBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        diffNormal();
                    }
                });

        hardBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        diffHard();
                    }
                });

        insaneBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        diffInsane();
                    }
                });

        applyBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        applyDiff();
                    }
                });

        // Column layout
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
     * Swaps difficulty to Easy.
     */
    private void diffEasy() {
        logger.info("Difficulty set to Easy");
        this.diffType = DifficultyType.EASY;
    }

    /**
     * Swaps difficulty to Normal.
     */
    private void diffNormal() {
        logger.info("Difficulty set to Normal");
        this.diffType = DifficultyType.NORMAL;
    }

    /**
     * Swaps difficulty to Hard.
     */
    private void diffHard() {
        logger.info("Difficulty set to Hard");
        this.diffType = DifficultyType.HARD;
    }

    /**
     * Swaps difficulty to Insane.
     */
    private void diffInsane() {
        logger.info("Difficulty set to Insane");
        this.diffType = DifficultyType.INSANE;
    }

    /**
     * Applies the difficulty to service locator
     * and then switches back to main menu
     */
    private void applyDiff() {
        logger.info("Difficulty Set and Applied");
        ServiceLocator.registerDifficulty(new Difficulty(this.diffType));
        game.setScreen(ScreenType.MAIN_MENU);
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
