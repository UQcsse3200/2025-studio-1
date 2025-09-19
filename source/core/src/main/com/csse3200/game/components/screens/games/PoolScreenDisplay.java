package com.csse3200.game.components.screens.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.ui.UIComponent;

public class PoolScreenDisplay extends UIComponent {
    private Window window;
    private Texture greenTex; // dispose this!

    public PoolScreenDisplay() {
    }

    @Override
    public void create() {
        super.create();

        // build a simple green 1x1 texture to stretch as a rectangle
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0.5f, 0f, 1f); // green cloth vibe
        pm.fill();
        greenTex = new Texture(pm);
        pm.dispose();

        // modal window
        window = new Window("Pool", skin);
        window.setModal(true);
        window.setMovable(true);
        window.setVisible(false);

        // the “pool” rectangle
        Image poolRect = new Image(new TextureRegionDrawable(new TextureRegion(greenTex)));
        poolRect.setSize(600f, 350f);
        window.add(poolRect).size(poolRect.getWidth(), poolRect.getHeight()).pad(10f).row();

        // close button
        TextButton closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                window.setVisible(false);
            }
        });
        window.add(closeBtn).right().pad(10f);

        window.pack();
        window.setPosition((stage.getWidth() - window.getWidth()) / 2f,
                (stage.getHeight() - window.getHeight()) / 2f);
        stage.addActor(window);

        // open on "interact"
        entity.getEvents().addListener("interact", this::open);
    }

    private void open() {
        window.setVisible(true);
        window.toFront();
        window.setPosition((stage.getWidth() - window.getWidth()) / 2f,
                (stage.getHeight() - window.getHeight()) / 2f);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // nothing; stage draws the window
    }

    @Override
    public void dispose() {
        super.dispose();
        if (greenTex != null) greenTex.dispose();
    }

    @Override
    public void update() {
        super.update();
        // if player presses 9, open the window
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {
            open();
        }
    }
}