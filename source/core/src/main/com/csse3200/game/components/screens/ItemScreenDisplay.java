package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.shop.CatalogEntry;

public class ItemScreenDisplay {
    private final CatalogEntry entry;

    // Keep only what must live beyond show() because close() needs it
    private Table root;          // centers the modal
    private Texture whiteTex;    // dispose in close()

    public ItemScreenDisplay(CatalogEntry entry) {
        this.entry = entry;
    }

    /** Show the mini screen on the given stage using the provided skin. */
    public void show(Stage stage, Skin skin) {
        // --- Root table ---
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        // --- Modal box (local, no need to keep as field) ---
        Table box = new Table();
        box.pad(14f);
        box.defaults().pad(6f).center();

        // Solid white background (no param since it's always WHITE)
        whiteTex = makeWhiteTexture();
        box.setBackground(new TextureRegionDrawable(new TextureRegion(whiteTex)));

        // Title
        Label title = new Label(entry.itemKey(), skin);
        title.setColor(Color.BLACK);
        title.setFontScale(1.2f);
        box.add(title).padBottom(8f).row();

        // Icon (static analysis says non-null; remove the check to silence warning)
        Actor icon = entry.getIconActor(skin);
        box.add(icon).size(120, 120).padBottom(8f).row();

        // Info labels
        box.add(makeBlackLabel("Price: $" + entry.price(), skin)).left().row();
        box.add(makeBlackLabel("Enabled: " + entry.enabled(), skin)).left().row();
        box.add(makeBlackLabel("Stackable: " + entry.stackable(), skin)).left().row();
        box.add(makeBlackLabel("Max Stack: " + entry.maxStack(), skin)).left().row();
        box.add(makeBlackLabel("Bundle Qty: " + entry.bundleQuantity(), skin)).left().row();

        // Close
        TextButton closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { close(); }
        });
        box.add(closeBtn).padTop(8f);

        // Attach
        root.add(box).width(420f);
    }

    /** Remove the mini screen. */
    public void close() {
        if (root != null) { root.remove(); root = null; }
        if (whiteTex != null) { whiteTex.dispose(); whiteTex = null; }
    }

    // Helper: always WHITE
    private static Texture makeWhiteTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static Label makeBlackLabel(String text, Skin skin) {
        Label lbl = new Label(text, skin);
        lbl.setColor(Color.BLACK);
        return lbl;
    }
}
