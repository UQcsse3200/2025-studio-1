package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.effects.TextEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ui component for displaying the Main menu.
 */
public class MainMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
    private static final float Z_INDEX = 2f;
    private TextEffects splashFx;     // to cancel on dispose
    private Table table;

    public MainMenuDisplay(GdxGame game) {
    }

    /**
     * Initialises styles and builds the actors.
     */
    @Override
    public void create() {
        super.create();
        addActors();
    }

    /**
     * Creates the logo and buttons, sizes them relative to the stage, and wires
     * button events.
     */
    private void addActors() {
        table = new Table();
        table.setFillParent(true);

        float leftPad = stage.getWidth() * 0.12f;
        table.center().left().padLeft(leftPad);

        // --- Logo image (taller, aspect-correct) ---
        Texture logoTex = ServiceLocator.getResourceService().getAsset("images/logo.png", Texture.class);
        Image title = new Image(logoTex);
        title.setScaling(Scaling.fit);       // keep aspect ratio when parent is larger
        title.setAlign(Align.center);

        // make it ~15% taller than the source, but also cap it relative to screen height
        float desiredH = logoTex.getHeight() * 1.15f;
        float maxH = stage.getHeight() * 0.345f;            // tune this % if you want bigger/smaller
        float targetH = Math.min(desiredH, maxH);
        float aspect = logoTex.getWidth() / (float) logoTex.getHeight();
        float targetW = targetH * aspect;

        // --- Splash text (Minecraft-style) ---
        splashFx = new TextEffects();
        Label.LabelStyle splashStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        splashStyle.fontColor = Color.YELLOW;
        String splashText = TextEffects.readRandomLine("text/mainmenu.txt", "Hello, Pilot!");
        Label splashLabel = new Label(splashText, splashStyle);
        TextEffects.enableMarkup(splashLabel);
        splashLabel.setColor(1f, 1f, 1f, 1f);
        splashLabel.setFontScale(1.15f);

        Group splashGroup = new Group();
        splashGroup.setTransform(true);
        splashGroup.setOrigin(Align.center);
        splashGroup.addActor(splashLabel);
        float prefW = splashLabel.getPrefWidth();
        float prefH = splashLabel.getPrefHeight();
        splashLabel.setPosition(0f, 0f);
        splashGroup.setSize(prefW, prefH);
        splashGroup.setRotation(10f);
        splashFx.pulseBetween(splashLabel, "ffe066", "ffffff", 0.8f);

        // Stack the splash over the (now larger) logo
        Stack titleStack = new Stack();
        titleStack.add(title);

        Table splashOverlay = new Table();
        splashOverlay.add(splashGroup)
                .expand().bottom().right()
                .padRight(-prefW * 0.5f)
                .padBottom(-prefH * 0.5f);
        titleStack.add(splashOverlay);

        // --- Buttons unchanged above/below ---
        float btnW = stage.getWidth() * 0.34f;
        float btnH = Math.max(64f, stage.getHeight() * 0.08f);
        table.defaults().width(btnW).height(btnH);

        TextButton startBtn = new TextButton("Start", skin);
        TextButton loadBtn = new TextButton("Load", skin);
        TextButton settingsBtn = new TextButton("Settings", skin);
        TextButton tutorialBtn = new TextButton("Tutorial", skin);
        TextButton difficultyBtn = new TextButton("Difficulty", skin);
        TextButton exitBtn = new TextButton("Exit", skin);
        startBtn.getLabel().setFontScale(2f);
        loadBtn.getLabel().setFontScale(2f);
        settingsBtn.getLabel().setFontScale(2f);
        tutorialBtn.getLabel().setFontScale(2f);
        difficultyBtn.getLabel().setFontScale(2f);
        exitBtn.getLabel().setFontScale(2f);

        // Button actions
        startBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ServiceLocator.getButtonSoundService().playClick();
                logger.debug("Start button clicked");
                entity.getEvents().trigger("start");
            }
        });
        loadBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ServiceLocator.getButtonSoundService().playClick();
                logger.debug("Load button clicked");
                entity.getEvents().trigger("load");
            }
        });
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ServiceLocator.getButtonSoundService().playClick();
                logger.debug("Settings button clicked");
                entity.getEvents().trigger("settings");
            }
        });
        tutorialBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ServiceLocator.getButtonSoundService().playClick();
                logger.debug("Tutorial button clicked");
                entity.getEvents().trigger("tutorial");
            }
        });
        difficultyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ServiceLocator.getButtonSoundService().playClick();
                logger.debug("Difficulty button clicked");
                entity.getEvents().trigger("difficulty");
            }
        });
        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ServiceLocator.getButtonSoundService().playClick();
                logger.debug("Exit button clicked");
                entity.getEvents().trigger("exit");
            }
        });

        // Column layout: use the stacked title (logo + splash)
        table.add(titleStack)
                .size(targetW, targetH)     // <-- this makes it taller on screen
                .left().padBottom(1.5f).padLeft(-10f);
        table.add(startBtn).padTop(1.5f).left();
        table.row();
        table.add(startBtn).padTop(15f).left();
        table.row();
        table.add(loadBtn).padTop(15f).left();
        table.row();
        table.add(settingsBtn).padTop(15f).left();
        table.row();
        table.add(tutorialBtn).padTop(15f).left();
        table.row();
        table.add(difficultyBtn).padTop(15f).left();
        table.row();
        table.add(exitBtn).padTop(15f).left();

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
        if (splashFx != null) {
            splashFx.cancel();
            splashFx = null;
        }
        if (table != null) {
            table.clear();
        }
        super.dispose();
    }
}
