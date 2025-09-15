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
import com.csse3200.game.components.shop.PurchaseError;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;


public class ShopScreenDisplay extends UIComponent {
    // error messages
    private static final String ERROR_MESSAGE = "Unable to purchase ";
    private static final String NOT_FOUND_MESSAGE = ".Item was not found.";
    private static final String DISABLED_MESSAGE = ". Item is disabled.";
    private static final String INSUFFICIENT_FUNDS_MESSAGE = ". You have insufficient funds.";
    private static final String INVENTORY_FULL_MESSAGE = ". Inventory is full.";
    private static final String LIMIT_REACHED_MESSAGE = ". Item limit has been reached.";
    private static final String INVALID_ITEM_MESSAGE = ". Invalid item.";
    private static final String UNEXPECTED_MESSAGE = ". Unexpected error.";

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
    Image background;


    public ShopScreenDisplay(ForestGameArea area, ShopManager manager) {
        this.game = area;
        this.catalog = manager.getCatalog();
        this.manager = manager;
    }
    @Override
    public void create() {
        entity.getEvents().addListener("purchaseFailed", this::showError);
        entity.getEvents().addListener("interact", this::show);
        super.create();
        itemPopup = new ItemScreenDisplay();
        entity.addComponent(itemPopup);

        // Background white colour
        Texture whiteTex = makeSolidTexture(Color.WHITE);
        background = new Image(new TextureRegionDrawable(new TextureRegion(whiteTex)));
        background.setFillParent(false); // we will size it manually
        background.setSize(500, 600);     // adjust width/height to cover your grid/buttons
        background.setPosition(
                (stage.getWidth() - background.getWidth()) / 2,
                (stage.getHeight() - background.getHeight()) / 2
        );

        //Dimmer
        dimTex = makeSolidTexture(new Color(0, 0, 0, 0.6f));
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTex)));
        dimmer.setFillParent(true);
        stage.addActor(dimmer);

        // Root table
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(background);
        stage.addActor(root);

        // Title
        Label title = new Label("Shop", skin);
        title.setFontScale(2f);
        root.add(title).padBottom(20).row();

        // Grid
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

        //  Close Shop button
        TextButton.TextButtonStyle style = skin.get("default", TextButton.TextButtonStyle.class);
        TextButton closeBtn = new TextButton("Close Shop", style);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
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
        hide();
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }

    public Stage getStage() { return stage; }


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
        if (background != null) {background.remove(); background = null; }
        super.dispose();
    }

    private void makeButton(CatalogEntry entry) {
        ImageButton iconButton = (ImageButton) entry.getIconActor(skin);

        Actor finalIcon;
        if (!entry.enabled()) {
            // Wrap in a stack to add overlay
            Stack stack = new Stack();
            stack.add(iconButton);

            // Semi-transparent grey overlay
            Image overlay = new Image(new TextureRegionDrawable(new TextureRegion(
                    makeSolidTexture(new Color(0.8f, 0f, 0f, 0.5f))
            )));
            overlay.setFillParent(true);
            stack.add(overlay);

            finalIcon = stack;
        } else {
            finalIcon = iconButton;
        }
        // Add name & price below icon
        Table itemTable = new Table();
        itemTable.add(finalIcon).size(100, 100).row();
        itemTable.add(new Label(entry.getItemName(), skin)).row();
        itemTable.add(new Label("$" + entry.price(), skin)).padBottom(6).row();

        // --- Add Info button ---
        itemTable.add(infoButton(entry)).padTop(4).row();

        // Gray out if disabled

        // Click to purchase
        int amountToPurchase = 1;
        finalIcon.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    manager.purchase(game.getPlayer(), entry, amountToPurchase);
                }
            });

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

    public void refreshCatalog(){
        grid.clearChildren();
        int columns = 4;
        int count = 0;
        for (CatalogEntry entry : catalog.list()) {
            makeButton(entry);
            count++;
            if(count % columns == 0) grid.row();
        }
    }

    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        refreshCatalog();
        updateCurrencyLabel();
        if (background != null) {
            background.setVisible(true);
        }
        if (root != null) {
            root.setVisible(true);
        }
        if (dimmer != null) {
            dimmer.setVisible(true);
        }
        if (hud != null) {
            hud.setVisible(true);
        }

    }

    public void hide() {
        ServiceLocator.getTimeSource().setPaused(false);
        if (background != null) {
            background.setVisible(false);
        }
        if (root != null) {
            root.setVisible(false);
        }
        if (dimmer != null) {
            dimmer.setVisible(false);
        }
        if (hud != null) {
            hud.setVisible(false);
        }
    }

    public void showError(String itemName, PurchaseError error) {
        String message = ERROR_MESSAGE + itemName;
        String errorMsg = switch(error) {
            case DISABLED -> DISABLED_MESSAGE;
            case NOT_FOUND -> NOT_FOUND_MESSAGE;
            case INVALID_ITEM -> INVALID_ITEM_MESSAGE;
            case LIMIT_REACHED -> LIMIT_REACHED_MESSAGE;
            case INVENTORY_FULL -> INVENTORY_FULL_MESSAGE;
            case INSUFFICIENT_FUNDS -> INSUFFICIENT_FUNDS_MESSAGE;
            default -> UNEXPECTED_MESSAGE;
        };
        Dialog dialog = new Dialog("Error", skin);
        dialog.text(message + errorMsg);
        dialog.button("OK");
        dialog.show(stage);
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