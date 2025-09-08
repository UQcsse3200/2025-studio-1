package com.csse3200.game.components.shop;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public class ShopAssets {
    private final Map<String, TextureRegionDrawable> icons = new HashMap<>();

    public ShopAssets() {
        loadIcons();
    }

    private void loadIcons() {
        //TODO: add images to assets and add to icons
        // icons.put(name, makeDrawable(path));
    }

    private TextureRegionDrawable makeDrawable(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    public Actor getIconActor(String itemKey, Skin skin) {
        TextureRegionDrawable icon = icons.get(itemKey);
        if (icon != null) {
            return new ImageButton(icon);
        } else {
            // Create a Table with a dark background and a label showing the key
            Table table = new Table();
            table.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
            Label label = new Label(itemKey, skin);
            label.setColor(Color.WHITE);
            table.add(label).pad(5);
            return table;
        }
    }
}
