package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all "end-of-run" screens in the game, such as
 * {@link DeathScreenDisplay} and {@link WinScreenDisplay}.
 * <p>
 * This component is responsible for creating and managing common UI
 * elements shared by end screens:
 * <ul>
 *   <li>A title label (e.g., "Victory", "Defeated")</li>
 *   <li>Round and elapsed time labels</li>
 *   <li>Primary and secondary action buttons</li>
 * </ul>
 * Subclasses must provide specific values (title text, title color,
 * button labels, and button actions).
 */
abstract class BaseEndScreenDisplay extends UIComponent {
    private final Logger logger = LoggerFactory.getLogger(BaseEndScreenDisplay.class);
    private static final float Z_INDEX = 2f;
    // protected members
    protected final GdxGame game;
    private final String titleText;
    private final Color titleColor;
    private final String primaryText;
    private final String secondaryText;
    private final Runnable primaryAction;
    private final Runnable secondaryAction;
    private NeonStyles neon;
    private Table table;

    /**
     * Constructs a new BaseEndScreenDisplay.
     *
     * @param game the {@link GdxGame} instance, used for screen navigation
     */
    protected BaseEndScreenDisplay(
            GdxGame game,
            String titleText,
            Color titleColor,
            String primaryText,
            Runnable primaryAction,
            Runnable secondaryAction
    ) {
        this.game = game;
        this.titleText = titleText;
        this.titleColor = titleColor;
        this.primaryText = primaryText;
        this.primaryAction = primaryAction;
        this.secondaryText = "Main Menu";
        this.secondaryAction = secondaryAction != null ? secondaryAction : this::backToMainMenu;
    }

    /**
     * Called when the component is created. Initialises styles and
     * constructs all UI elements.
     */
    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.70f);
        logger.info("{} created", getClass().getSimpleName());
        addActors();
    }

    /**
     * Builds and adds all UI actors (title, labels, buttons) to the stage.
     * This method is private as it should only be invoked during creation.
     */
    private void addActors() {
        table = new Table();
        table.setFillParent(true);
        table.center();

        float btnWidth = stage.getWidth() * 0.34f;
        float btnHeight = Math.max(64f, stage.getHeight() * 0.08f);
        table.defaults().width(btnWidth).height(btnHeight);

        // title
        Label title = new Label(this.titleText, skin, "title");
        title.setFontScale(3.0f);
        title.setColor(this.titleColor);
        table.add(title).colspan(2).center().padBottom(50f);
        table.row();

        // Ensure small style uses white font colour (clone to avoid mutating shared style)
        Label.LabelStyle baseSmall = skin.get("small", Label.LabelStyle.class);
        Label.LabelStyle small = new Label.LabelStyle(baseSmall);
        small.fontColor = skin.getColor("white");

        // Round / Time
        Label roundLabel = new Label("Round: 1", small);
        roundLabel.setFontScale(3.0f);
        table.add(roundLabel).colspan(2).center().padBottom(50f);
        table.row();

        Label timeLabel = new Label("Time: 00:00", small);
        timeLabel.setFontScale(3.0f);
        table.add(timeLabel).colspan(2).center().padBottom(50f);
        table.row();

        // Buttons
        TextButton primaryBtn = createButton(primaryText, primaryAction);
        TextButton secondaryBtn = createButton(secondaryText, secondaryAction);

        table.add(primaryBtn).left().padRight(30f);
        table.add(secondaryBtn).left();

        stage.addActor(table);
        logger.info("Actors added to {}", getClass().getSimpleName());
    }

    private TextButton createButton(String text, Runnable action) {
        TextButton btn = new TextButton(text, neon.buttonRounded());
        btn.getLabel().setFontScale(2.0f);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                logger.debug("{} clicked on {}", text, getClass().getSimpleName());
                if (action != null) {
                    action.run();
                }
            }
        });
        return btn;
    }

    /**
     * Defines the behaviour executed when the secondary button is clicked.
     * By default, switches back to the Main Menu screen, but can be overridden.
     */
    protected void backToMainMenu() {
        logger.info("Switching to MAIN_MENU from {}", getClass().getSimpleName());
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    /**
     * This method is intentionally left empty. Rendering is handled
     * by the stage rather than by this component.
     */
    @Override public void draw(SpriteBatch batch) { /* The Stage draws itself */ }

    /**
     * Returns the z-index (rendering order) of this UI component.
     *
     * @return z-index of this component
     */
    @Override public float getZIndex() { return Z_INDEX; }

    /**
     * Disposes of this component and clears its UI table.
     * Called when the component is being destroyed.
     */
    @Override
    public void dispose() {
        logger.debug("Disposing {}", getClass().getSimpleName());
        if (table != null) table.clear();
        super.dispose();
    }
}
