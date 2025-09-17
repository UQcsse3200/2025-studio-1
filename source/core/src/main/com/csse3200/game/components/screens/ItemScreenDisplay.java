package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.shop.CatalogEntry;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Popup screen which displays information about a shop item
 */
public class ItemScreenDisplay extends UIComponent {
    private CatalogEntry entry;      // set on open()
    private Table root;              // centers the modal
    private Texture whiteTex;        // disposed in dispose()
    private boolean whiteTexOwned;   // guard double-dispose

    /**
     * A popup which displays information about a shop item in the game.
     */
    public ItemScreenDisplay() {}

    @Override
    public void create() {
        super.create();
    }

    /** Build and show the popup for the given entry.
     * Safe to call repeatedly. */
    public void open(CatalogEntry entry) {
        // Don’t stack UIs
        close();

        // ensure stage exists even if create() hasn’t run
        if (stage == null) {
            stage = ServiceLocator.getRenderService().getStage();
        }

        this.entry = entry;

        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Table box = new Table();
        box.pad(14f);
        box.defaults().pad(6f).center();

        // White background (create once per open)
        whiteTex = makeWhiteTexture();
        whiteTexOwned = true;
        box.setBackground(new TextureRegionDrawable(new TextureRegion(whiteTex)));

        // Title
        Label title = new Label(entry.getItemName(), skin);
        title.setColor(Color.BLACK);
        title.setFontScale(1.2f);
        box.add(title).padBottom(8f).row();

        Actor popupIcon = null;
        try {
            Actor tileIcon = entry.getIconActor(skin);
            if (tileIcon instanceof Image) {
                var d = ((Image) tileIcon).getDrawable();
                if (d != null) {
                    popupIcon = new Image(d);
                }
            }
        } catch (Exception ignored) {
            // if anything went wrong, leave popupIcon null
        }
        if (popupIcon != null) {
            box.add(popupIcon).size(120, 120).padBottom(8f).row();
        }

        // Info lines
        box.add(makeBlackLabel("Price: $" + this.entry.price())).left().row();
        box.add(makeBlackLabel("Enabled: " + this.entry.enabled())).left().row();
        box.add(makeBlackLabel("Max Stack: " + this.entry.maxStack())).left().row();
        box.add(makeBlackLabel("Bundle Qty: " + this.entry.bundleQuantity())).left().row();

        // Close button
        TextButton closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { close(); }
        });
        box.add(closeBtn).padTop(8f);

        // Attach
        root.add(box).width(420f);
    }

    /** Remove popup UI (component stays attached and reusable). */
    public void close() {
        if (root != null) { root.remove(); root = null; }
        if (whiteTexOwned && whiteTex != null) {
            whiteTex.dispose();
            whiteTex = null;
            whiteTexOwned = false;
        }
        entry = null;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // stage draws UI
    }

    @Override
    public void dispose() {
        close();
        super.dispose();
    }

    // Helpers
    private static Texture makeWhiteTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Label makeBlackLabel(String text) {
        Label lbl = new Label(text, skin);
        lbl.setColor(Color.BLACK);
        return lbl;
    }
}
