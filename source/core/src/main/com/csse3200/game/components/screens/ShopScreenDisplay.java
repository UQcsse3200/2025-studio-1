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
import com.csse3200.game.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.scenes.scene2d.ui.Container;


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
    private Image frame;
    private Image dimmer;
    private Texture dimTex;
    private Texture frameTex;
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

        // Dimmer
        dimTex = makeSolidTexture(new Color(0, 0, 0, 0.6f));
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTex)));
        dimmer.setFillParent(true);
        stage.addActor(dimmer);

        // Frame (outline)
        float panelW = 720f, panelH = 660f;
        frameTex = makeSolidTexture(Color.BLACK);
        frame = new Image(new TextureRegionDrawable(new TextureRegion(frameTex)));
        frame.setSize(panelW + 8, panelH + 8);
        frame.setPosition(
                (stage.getWidth()  - frame.getWidth())  / 2f,
                (stage.getHeight() - frame.getHeight()) / 2f
        );
        stage.addActor(frame);

        // Panel background (centered card)
        Texture panelTex = makeSolidTexture(Color.valueOf("0B132B")); // deep navy
        background = new Image(new TextureRegionDrawable(new TextureRegion(panelTex)));
        background.setSize(panelW, panelH);
        background.setPosition(
                (stage.getWidth() - background.getWidth()) / 2f,
                (stage.getHeight() - background.getHeight()) / 2f);
        stage.addActor(background);

        // Root table sits on top of the panel
        root = new Table();
        root.setSize(panelW, panelH);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(20); // top-align & inner padding
        root.defaults().pad(10);
        stage.addActor(root);

        // Title
        Label.LabelStyle titleStyle =
                new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = Color.valueOf("00E5FF"); // aqua
        Label title = new Label("Shop", titleStyle);
        title.setFontScale(1.8f);
        root.add(title).padBottom(10).row();

        // Divider
        Image divider = new Image(new TextureRegionDrawable(new TextureRegion(
                makeSolidTexture(new Color(1f, 1f, 1f, 0.08f))
        )));
        divider.setHeight(2);
        divider.setFillParent(false);
        divider.setWidth(panelW - 40);
        root.add(divider).padBottom(8).row();

        // Grid (uniform cells)
        grid = new Table();
        grid.defaults().pad(12).size(150, 180).uniform(true); // keeps every cell the same size

        int columns = 4;
        int count = 0;

        for (CatalogEntry entry : catalog.list()) {
            makeButton(entry);
            count++;
            if (count % columns == 0) grid.row();
        }

        root.add(grid).row();

        // Balance footer (bottom-right inside panel)
        Label.LabelStyle balStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        balStyle.fontColor = Color.valueOf("FFD54F"); // gold
        currencyLabel = new Label("", balStyle);
        currencyLabel.setFontScale(1.2f);
        updateCurrencyLabel();

        Table footer = new Table();
        footer.add().expandX();                 // push next cell to the right edge
        footer.add(currencyLabel).right();
        root.add(footer).growX().padTop(8f).row();

        //  Close Shop button
        TextButton.TextButtonStyle style = skin.get("default", TextButton.TextButtonStyle.class);
        TextButton closeBtn = new TextButton("Close Shop", style);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        root.add(closeBtn).padTop(14).center().row();

        // keep the footer label in sync with InventoryComponent#setProcessor()
        game.getPlayer().getEvents().addListener("updateProcessor", (Integer p) -> {
            if (currencyLabel != null) {
                currencyLabel.setText("Balance: $" + p);
            }
        });

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
        currencyLabel.setText("Balance: $" + amount);
    }

    @Override
    public void dispose() {
        if (root != null) { root.remove(); root = null; }
        if (dimmer != null) { dimmer.remove(); dimmer = null; }
        if (dimTex != null) { dimTex.dispose(); dimTex = null; }
        if (frame != null) { frame.remove(); frame = null; }
        if (frameTex != null) { frameTex.dispose(); frameTex = null; }
        if (itemPopup != null) { itemPopup.dispose(); itemPopup = null; }
        if (background != null) {background.remove(); background = null; }
        super.dispose();
    }

    private void makeButton(CatalogEntry entry) {
        ImageButton iconButton = (ImageButton) entry.getIconActor(skin);
        // Ensure the icon scales to fit the cell (prevents tall sprites from overlapping)
        iconButton.getImage().setScaling(com.badlogic.gdx.utils.Scaling.fit);

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

        // Wrap the icon in a fixed-size box
        Container<Actor> iconBox = new Container<>(iconButton);
        iconBox.prefSize(96, 96);
        iconBox.minSize(96, 96);
        iconBox.maxSize(96, 96);
        iconBox.fill();

        // Add name & price below icon
        Table itemTable = new Table();
        itemTable.add(iconBox).width(96).height(96).padTop(6).padBottom(8).row();
        itemTable.add(new Label(entry.getItemName(), skin)).padBottom(4).row();

        // Price label in gold
        Label.LabelStyle priceStyle =
                new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        priceStyle.fontColor = Color.valueOf("FFD54F"); // gold
        Label price = new Label("$" + entry.price(), priceStyle);
        itemTable.add(price).padBottom(8).row();

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

        grid.add(itemTable);

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
        refreshCatalog();
        updateCurrencyLabel();
        if (frame != null) {
            frame.setVisible(true);
        }
        if (background != null) {
            background.setVisible(true);
        }
        if (root != null) {
            root.setVisible(true);
        }
        if (dimmer != null) {
            dimmer.setVisible(true);
        }
    }

    public void hide() {
        if (frame != null) {
            frame.setVisible(false);
        }
        if (background != null) {
            background.setVisible(false);
        }
        if (root != null) {
            root.setVisible(false);
        }
        if (dimmer != null) {
            dimmer.setVisible(false);
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