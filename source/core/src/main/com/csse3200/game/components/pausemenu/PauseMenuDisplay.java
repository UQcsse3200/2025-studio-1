package com.csse3200.game.components.pausemenu;

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

    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.70f);
        addActors();
    }

    private void addActors() {
        // Fullscreen dimmer
        dimTex = makeSolidTexture(1, 1, new Color(0, 0, 0, 0.6f));
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTex)));
        dimmer.setFillParent(true);
        stage.addActor(dimmer);

        root = new Table();
        root.setFillParent(true);
        root.center();

        float btnW = stage.getWidth() * 0.28f;
        float btnH = Math.max(56f, stage.getHeight() * 0.07f);

        NeonStyles neon = new NeonStyles(0.70f);
        TextButton.TextButtonStyle style = neon.buttonRounded();

        // Title
        Label pausedLabel = new Label("Game Paused", skin, "title");
        pausedLabel.setFontScale(2.0f);
        pausedLabel.getStyle().fontColor = Color.WHITE;

        // Buttons
        TextButton resumeBtn   = new TextButton("Resume", style);
        TextButton restartBtm = new TextButton("Restart", style);
        TextButton mainBtn     = new TextButton("Main Menu", style);

        resumeBtn.getLabel().setFontScale(1.8f);
        restartBtm.getLabel().setFontScale(1.8f);
        mainBtn.getLabel().setFontScale(1.8f);

        Table panel = new Table();
        panel.defaults().pad(10f);

        panel.add(pausedLabel).center().padBottom(24f).row();
        panel.add(resumeBtn).row();
        panel.add(restartBtm).row();
        panel.add(mainBtn).row();

        root.add(panel);

        // Button handlers
        resumeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("resume");
            }
        });

        restartBtm.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(ScreenType.MAIN_GAME);
            }
        });

        mainBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(ScreenType.MAIN_MENU);
            }
        });

        // Quick keyboard UX: ESC = resume
        stage.setKeyboardFocus(root);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    entity.getEvents().trigger("resume");
                    return true;
                }
                return false;
            }
        });
        stage.addActor(root);
    }

    private static Texture makeSolidTexture(int w, int h, Color color) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
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

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }

    @Override
    public void dispose() {
        if (root != null) { root.remove(); root = null; }
        if (dimmer != null) { dimmer.remove(); dimmer = null; }
        if (dimTex != null) { dimTex.dispose(); dimTex = null; }
        super.dispose();
    }
}
