package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.shop.CatalogEntry;
import com.csse3200.game.components.shop.CatalogService;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.ui.*;


public class ShopScreenDisplay extends UIComponent {
    private final ForestGameArea game;
    private final CatalogService catalog;
    private final ShopManager manager;
    private Table root;
    private Image dimmer;
    private Texture dimTex;
    private Table grid;
    private Table hud;
    private Label currencyLabel;
    private ItemScreenDisplay itemPopup;


    public ShopScreenDisplay(ForestGameArea area, CatalogService catalog, ShopManager manager) {
        this.game = area;
        this.catalog = catalog;
        this.manager = manager;
    }
    @Override
    public void create() {
        super.create();
        itemPopup = new ItemScreenDisplay();
        entity.addComponent(itemPopup);


        // --- Dimmer ---
        dimTex = makeSolidTexture(new Color(0, 0, 0, 0.6f));
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTex)));
        dimmer.setFillParent(true);
        stage.addActor(dimmer);

        // --- Root table ---
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        // Title
        Label title = new Label("Shop", skin);
        title.setFontScale(2f);
        root.add(title).padBottom(20).row();

        // --- Grid ---
        grid = new Table();
        grid.defaults().pad(10);

        int columns = 4;
        int count = 0;

        for (CatalogEntry entry : catalog.list()) {
            makeButton(entry);
            count++;
            if (count % columns == 0) grid.row();
        }

        root.add(grid).row();

        // --- Close Shop button ---
        TextButton.TextButtonStyle style = skin.get("default", TextButton.TextButtonStyle.class);
        TextButton closeBtn = new TextButton("Close Shop", style);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("closeShop");
            }
        });

        root.add(closeBtn).padTop(20).row();
        // --- Currency HUD (top-right) ---
        hud = new Table();
        hud.setFillParent(true);
        hud.top().right().pad(12);
        stage.addActor(hud);

        // label + initial value
        currencyLabel = new Label("", skin);
        updateCurrencyLabel();  // set from InventoryComponent once on open

        // Optional: add a static "$" label or an icon if you have one in the skin
        hud.add(new Label("$", skin)).padRight(4f);
        hud.add(currencyLabel);

    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }

    private void updateCurrencyLabel() {
        var inv = game.getPlayer().getComponent(
                com.csse3200.game.components.player.InventoryComponent.class);
        int amount = (inv != null) ? inv.getProcessor() : 0;
        currencyLabel.setText(String.valueOf(amount));
    }

    @Override
    public void dispose() {
        if (root != null) { root.remove(); root = null; }
        if (dimmer != null) { dimmer.remove(); dimmer = null; }
        if (dimTex != null) { dimTex.dispose(); dimTex = null; }
        if (hud != null) { hud.remove(); hud = null; }
        if (itemPopup != null) { itemPopup.dispose(); itemPopup = null; }
        super.dispose();
    }

    private void makeButton(CatalogEntry entry) {
        Actor actor = entry.getIconActor(skin);

        // Add name & price below icon
        Table itemTable = new Table();
        itemTable.add(actor).size(100, 100).row();
        itemTable.add(new Label(entry.itemKey(), skin)).row();
        itemTable.add(new Label("$" + entry.price(), skin)).padBottom(6).row();

        // --- Add Info button ---
        itemTable.add(infoButton(entry)).padTop(4).row();

        // Gray out if disabled
        if (!entry.enabled()) {
            itemTable.getChildren().forEach(child -> child.setColor(Color.GRAY));
        }

        // Click to purchase
        if(entry.enabled()){
            actor.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    manager.purchase(game.getPlayer(), entry.itemKey());
                }
            });}

        grid.add(itemTable).size(120, 140);

    }

    private TextButton infoButton(CatalogEntry entry) {
        TextButton btn = new TextButton("Info", skin);
        btn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                itemPopup.open(entry); // safe: stage is already set by create()
            }
        });
        return btn;
    }





    // --- Helper: create solid texture for dimmer ---
    private static Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}