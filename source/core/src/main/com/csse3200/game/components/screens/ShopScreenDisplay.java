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
    // Error messages
    private static final String ERROR_MESSAGE = "Unable to purchase ";
    private static final String NOT_FOUND_MESSAGE = ".Item was not found.";
    private static final String DISABLED_MESSAGE = ". Item is disabled.";
    private static final String INSUFFICIENT_FUNDS_MESSAGE = ". You have insufficient funds.";
    private static final String INVENTORY_FULL_MESSAGE = ". Inventory is full.";
    private static final String LIMIT_REACHED_MESSAGE = ". Item limit has been reached.";
    private static final String INVALID_ITEM_MESSAGE = ". Invalid item.";
    private static final String UNEXPECTED_MESSAGE = ". Unexpected error.";

    // UI constants
    private static final float PANEL_W = 720f;
    private static final float PANEL_H = 660f;
    private static final int   GRID_COLUMNS = 4;
    private static final float CELL_W = 150f;
    private static final float CELL_H = 180f;
    private static final float ICON_SIZE = 96f;

    private static final Color PANEL_COLOR = Color.valueOf("0B132B");
    private static final Color TITLE_COLOR = Color.valueOf("00E5FF");
    private static final Color GOLD        = Color.valueOf("FFD54F");

    // Dependencies
    private final ForestGameArea game;
    private final CatalogService catalog;
    private final ShopManager manager;

    // Scene2D Widgets
    private Table root;
    private Image frame;
    private Image dimmer;
    private Texture dimTex;
    private Texture frameTex;
    private Table grid;
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

        buildBackdrop();
        buildRootTable();
        addHeader();
        buildGrid();
        populateGrid();

        addBalanceFooter();
        addCloseButton();
        subscribeCurrencyUpdates();

        hide();
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }

    public Stage getStage() { return stage; }

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

    // Dim the world, draw a black frame outline, and a navy panel behind content.
    private void buildBackdrop() {
        // Dimmer
        dimTex = makeSolidTexture(new Color(0, 0, 0, 0.6f));
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTex)));
        dimmer.setFillParent(true);
        stage.addActor(dimmer);

        // Black frame (outline)
        frameTex = makeSolidTexture(Color.BLACK);
        frame = new Image(new TextureRegionDrawable(new TextureRegion(frameTex)));
        frame.setSize(PANEL_W + 8, PANEL_H + 8);
        frame.setPosition((stage.getWidth() - frame.getWidth()) / 2f,
                (stage.getHeight() - frame.getHeight()) / 2f);
        stage.addActor(frame);

        // Navy panel
        Texture panelTex = makeSolidTexture(PANEL_COLOR);
        background = new Image(new TextureRegionDrawable(new TextureRegion(panelTex)));
        background.setSize(PANEL_W, PANEL_H);
        background.setPosition((stage.getWidth() - background.getWidth()) / 2f,
                (stage.getHeight() - background.getHeight()) / 2f);
        stage.addActor(background);
    }

    // Root table, sized/positioned on top of the panel.
    private void buildRootTable() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(20);
        root.defaults().pad(10);
        stage.addActor(root);
    }

    // Adds title and Divider
    private void addHeader() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = TITLE_COLOR;

        Label title = new Label("Shop", titleStyle);
        title.setFontScale(1.8f);
        root.add(title).padBottom(10).row();

        Image divider = new Image(new TextureRegionDrawable(
                new TextureRegion(makeSolidTexture(new Color(1f, 1f, 1f, 0.08f)))
        ));
        divider.setHeight(2);
        divider.setFillParent(false);
        divider.setWidth(PANEL_W - 40);
        root.add(divider).padBottom(8).row();
    }

    // Create grid table and add to root
    private void buildGrid() {
        grid = new Table();
        grid.defaults().pad(12).size(CELL_W, CELL_H).uniform(true);
        root.add(grid).row();
    }

    // Populate grid with the catalog with consistent rows/columns
    private void populateGrid() {
        int count = 0;
        for (CatalogEntry entry : catalog.list()) {
            grid.add(buildItemCell(entry));
            count++;
            if (count % GRID_COLUMNS == 0) grid.row();
        }
    }

    // Rebuild the item grid from the current catalog
    private void refreshCatalog() {
        if (grid == null) return;
        grid.clearChildren();
        populateGrid(); // re-adds all cells and rows
    }

    // Balance label aligned bottom right
    private void addBalanceFooter() {
        Label.LabelStyle balStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        balStyle.fontColor = GOLD;

        currencyLabel = new Label("", balStyle);
        currencyLabel.setFontScale(1.2f);
        updateCurrencyLabel();

        Table footer = new Table();
        footer.add().expandX();
        footer.add(currencyLabel).right();
        root.add(footer).growX().padTop(8f).row();
    }

    // Close shop button
    private void addCloseButton() {
        TextButton.TextButtonStyle style = skin.get("default", TextButton.TextButtonStyle.class);
        TextButton closeBtn = new TextButton("Close Shop", style);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        root.add(closeBtn).padTop(14).center().row();
    }

    // Keep balance in sync with InventoryComponent#setProcessor().
    private void subscribeCurrencyUpdates() {
        game.getPlayer().getEvents().addListener("updateProcessor", (Integer p) -> {
            if (currencyLabel != null) {
                currencyLabel.setText("Balance: $" + p);
            }
        });
    }

    // Build cells.
    private Table buildItemCell(CatalogEntry entry) {
        ImageButton iconButton = (ImageButton) entry.getIconActor(skin);
        iconButton.getImage().setScaling(Scaling.fit); // keep aspect within box

        Actor iconActor;
        if (!entry.enabled()) {
            // red-tinted overlay if disabled
            Stack stack = new Stack();
            stack.add(iconButton);
            Image overlay = new Image(new TextureRegionDrawable(new TextureRegion(
                    makeSolidTexture(new Color(0.8f, 0f, 0f, 0.5f))
            )));
            overlay.setFillParent(true);
            stack.add(overlay);
            iconActor = stack;
        } else {
            iconActor = iconButton;
        }

        // Lock icon into a fixed square
        Container<Actor> iconBox = new Container<>(iconActor);
        iconBox.prefSize(ICON_SIZE, ICON_SIZE).minSize(ICON_SIZE, ICON_SIZE).maxSize(ICON_SIZE, ICON_SIZE).fill();

        // Gold price labels
        Label.LabelStyle priceStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        priceStyle.fontColor = GOLD;

        // Compose one cell
        Table cell = new Table();
        cell.add(iconBox).width(ICON_SIZE).height(ICON_SIZE).padTop(6).padBottom(8).row();
        cell.add(new Label(entry.getItemName(), skin)).padBottom(4).row();
        cell.add(new Label("$" + entry.price(), priceStyle)).padBottom(8).row();
        cell.add(infoButton(entry)).padTop(4).row();

        // Click purchases
        final int amountToPurchase = 1;
        iconActor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                manager.purchase(game.getPlayer(), entry, amountToPurchase);
            }
        });

        return cell;
    }

    private TextButton infoButton(CatalogEntry entry) {
        TextButton btn = new TextButton("Info", skin);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                itemPopup.open(entry);
            }
        });
        return btn;
    }

    // Helpers
    private void updateCurrencyLabel() {
        var inv = game.getPlayer().getComponent(
                com.csse3200.game.components.player.InventoryComponent.class);
        int amount = (inv != null) ? inv.getProcessor() : 0;
        currencyLabel.setText("Balance: $" + amount);
    }

    private static Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // Errors
    private void showError(String itemName, PurchaseError error) {
        String message = ERROR_MESSAGE + itemName;
        String errorMsg = switch (error) {
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
}