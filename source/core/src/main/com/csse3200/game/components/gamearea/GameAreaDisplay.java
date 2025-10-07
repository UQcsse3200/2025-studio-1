package com.csse3200.game.components.gamearea;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.ui.UIComponent;

public class GameAreaDisplay extends UIComponent {
    private final String gameAreaName;
    private Table overlay;
    private Texture bgTex;

    public GameAreaDisplay(String gameAreaName) {
        this.gameAreaName = gameAreaName;
    }

    @Override
    public void create() {
        super.create();

        Label title = new Label(gameAreaName, skin, "white");

        // Full-screen layout for positioning
        overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().left();
        overlay.defaults().left();
        overlay.padTop(10f).padLeft(10f);

        // Create panel
        Table panel = new Table();
        panel.left();
        panel.setBackground(makeSolidBg(new Color(0f, 0f, 0f, 0.3f)));
        panel.pad(4f, 4f, 0f, 8f);
        panel.add(title).left();

        overlay.add(panel).left().width(320f);
        stage.addActor(overlay);
    }

    @Override public void draw(SpriteBatch batch) { /* Scene2D lays it out */ }

    @Override
    public void dispose() {
        super.dispose();
        if (overlay != null) {
            overlay.remove();
            overlay = null;
        }
        if (bgTex != null) {
            bgTex.dispose();
            bgTex = null;
        }
    }

    /** Create a solid-coloured 1x1 drawable */
    private Drawable makeSolidBg(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        bgTex = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(bgTex));
    }
}