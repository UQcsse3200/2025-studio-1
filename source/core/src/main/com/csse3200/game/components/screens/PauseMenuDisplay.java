package com.csse3200.game.components.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Visual pause menu overlay shown on top of the main game.
 */
public class PauseMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PauseMenuDisplay.class);
    private static final float Z_INDEX = 100f;
    private Table root;
    private NeonStyles neon;
    private Image dimmer;
    private Texture dimTex;
    private final GdxGame game;

    public PauseMenuDisplay(GdxGame game) {
        super();
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
     * Builds and adds all Scene2D actors for the overlay:
     * a semi-transparent fullscreen dimmer, a centered panel with title and buttons,
     * and input listeners.
     */
    private void addActors() {
        // Fullscreen dimmer
        dimTex = makeSolidTexture( new Color(0, 0, 0, 0.6f));
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTex)));
        dimmer.setFillParent(true);
        stage.addActor(dimmer);
        logger.debug("Semi-transparent dimmer added");

        root = new Table();
        root.setFillParent(true);
        root.center();

        TextButton.TextButtonStyle style = neon.buttonRounded();

        // Title
        Label pausedLabel = new Label("Game Paused", skin, "title");
        pausedLabel.setFontScale(2.0f);
        pausedLabel.getStyle().fontColor = Color.WHITE;
        logger.debug("Title label created");

        // Buttons
        TextButton resumeBtn   = new TextButton("Resume", style);
        TextButton restartBtm = new TextButton("Restart", style);
        TextButton mainBtn     = new TextButton("Main Menu", style);
        TextButton saveBtn     = new TextButton("Save", style);

        // Label text size
        resumeBtn.getLabel().setFontScale(1.8f);
        restartBtm.getLabel().setFontScale(1.8f);
        mainBtn.getLabel().setFontScale(1.8f);
        saveBtn.getLabel().setFontScale(1.8f);
        logger.debug("Buttons created");

        Table panel = new Table();
        panel.defaults().pad(10f);

        // Stack title and buttons vertically with padding
        panel.add(pausedLabel).center().padBottom(24f).row();
        panel.add(resumeBtn).row();
        panel.add(restartBtm).row();
        panel.add(mainBtn).row();
        panel.add(saveBtn).row();

        root.add(panel);

        // Button handlers
        resumeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                logger.debug("Resume button clicked");
                entity.getEvents().trigger("resume");
            }
        });

        restartBtm.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                logger.debug("Restart button clicked");
                game.setScreen(ScreenType.MAIN_GAME);
            }
        });

        mainBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                logger.debug("Main Menu button clicked");
                game.setScreen(ScreenType.MAIN_MENU);
            }
        });

        saveBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                logger.debug("save button clicked");
                entity.getEvents().trigger("save");
                game.setScreen(ScreenType.MAIN_MENU);
            }
        });

        stage.setKeyboardFocus(root);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    logger.debug("ESC pressed");
                    entity.getEvents().trigger("resume");
                    return true;
                }
                return false;
            }
        });
        stage.addActor(root);
    }

    /**
     * Creates a solid RGBA texture.
     */
    private static Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }

    /**
     * Returns the draw order for the overlay.
     */
    @Override
    public float getZIndex() {
        return Z_INDEX;
    }

    /**
     * Removes overlay actors from the stage and disposes generated textures.
     */
    @Override
    public void dispose() {
        if (root != null) { root.remove(); root = null; }
        if (dimmer != null) { dimmer.remove(); dimmer = null; }
        if (dimTex != null) { dimTex.dispose(); dimTex = null; }
        logger.debug("Pause menu disposed");
        super.dispose();
    }
}
