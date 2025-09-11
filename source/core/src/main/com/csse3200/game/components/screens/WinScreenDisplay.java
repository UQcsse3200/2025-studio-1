package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI component for displaying the Win Screen.
 * Shows a "Victory" message, the current round, elapsed time, and provides
 * buttons to retry the game or return to the main menu.
 **/
public class WinScreenDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(WinScreenDisplay.class);
    private final GdxGame game;
    private static final float Z_INDEX = 2f;
    private Table table;
    private NeonStyles neon;

    /**
     * Creates a new WinScreenDisplay
     * @param game the {@link GdxGame} instance
     */
    public WinScreenDisplay(GdxGame game) {
        super();
        this.game = game;
    }

    /**
     * Called when the component is created.
     * Initialise styles and sets up UI elements
     */
    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.70f);
        addActors();
    }

    /**
     * Builds and adds all UI actors
     * Includes "VICTORY" title, round and time label and
     * Continue/ Main Menu buttons
     */
    private void addActors() {
        table = new Table();
        table.setFillParent(true);


        table.center();
        float btnW = stage.getWidth() * 0.34f;
        float btnH = Math.max(64f, stage.getHeight() * 0.08f);
        table.defaults().width(btnW).height(btnH);

        //Victory title
        Label victoryLabel = new Label("Victory", skin, "title");
        victoryLabel.setFontScale(3.0f);
        victoryLabel.setColor(0f, 1f, 0f, 1f);
        table.add(victoryLabel).colspan(2).center().padBottom(50f);
        table.row();

        Label.LabelStyle smallStyle = skin.get("small", Label.LabelStyle.class);
        smallStyle.fontColor = skin.getColor("white");

        //Round label
        Label roundLabel = new Label("Round: 1", skin, "small");
        roundLabel.setFontScale(3.0f);
        table.add(roundLabel).colspan(2).center().padBottom(50f);
        table.row();

        //Time label
        Label timeLabel = new Label("Time: 00:00", skin, "small");
        timeLabel.setFontScale(3.0f);
        table.add(timeLabel).colspan(2).center().padBottom(50f);
        table.row();

        //Buttons
        TextButton.TextButtonStyle style = neon.buttonRounded();
        TextButton tryAgainBtn = new TextButton("Continue", style);
        TextButton mainMenuBtn = new TextButton("Main Menu", style);

        tryAgainBtn.getLabel().setFontScale(2.0f);
        mainMenuBtn.getLabel().setFontScale(2.0f);


        // Triggers an event when the button is pressed
        tryAgainBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Continue Button clicked");

                    }
                });

        mainMenuBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Main Menu button clicked");
                        backMainMenu();
                    }
                });


        table.row();
        table.add(tryAgainBtn).left().padRight(30f);
        table.add(mainMenuBtn).left();

        stage.addActor(table);
    }

    /**
     * Switch the game screen to Main Menu
     */
    private void backMainMenu() {
        logger.debug("Switching to Main Menu screen");
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing WinScreenDisplay");
        table.clear();
        super.dispose();
    }
}