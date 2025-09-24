package com.csse3200.game.components.minigames.robotFighting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.screens.ItemScreenDisplay;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

public class RobotFightingDisplay extends UIComponent {

    // UI constants
    private static final float PANEL_W = 720f;
    private static final float PANEL_H = 600f;
    private static final int GRID_COLUMNS = 4;
    private static final float CELL_W = 150f;
    private static final float CELL_H = 180f;
    private static final float ICON_SIZE = 96f;

    private static final Color PANEL_COLOR = Color.valueOf("0B132B");
    private static final Color TITLE_COLOR = Color.valueOf("00E5FF");
    private static final Color GOLD = Color.valueOf("FFD54F");

    // Scene2D Widgets
    private Table root;
    private Table grid;
    private Image frame;
    private Image dimmer;
    private Texture pixelTex;
    private Label currencyLabel;
    private ItemScreenDisplay itemPopup;
    Image background;

    private final GameArea game = ServiceLocator.getGameArea();

    public RobotFightingDisplay() {
    }

    /**
     * Creates a new ShopScreen Display and sets up UI components
     **/
    @Override
    public void create() {
        super.create();

        itemPopup = new ItemScreenDisplay();
        entity.addComponent(itemPopup);

        buildBackdrop();
        buildRootTable();
        addHeader();
        buildGrid();
        addFooter();
        subscribeCurrencyUpdates();

        hide();
    }

    /**
     * Draw method overridden
     *
     * @param batch Batch to render to.
     */
    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }

    /**
     * Returns the Stage associated with the shop screen
     *
     * @return the stage object
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Cleans up resources and disposes of UI elements.
     */
    @Override
    public void dispose() {
        if (root != null) {
            root.remove();
            root = null;
        }
        if (dimmer != null) {
            dimmer.remove();
            dimmer = null;
        }
        if (frame != null) {
            frame.remove();
            frame = null;
        }
        if (pixelTex != null) {
            pixelTex.dispose();
            pixelTex = null;
        }
        if (itemPopup != null) {
            itemPopup.dispose();
            itemPopup = null;
        }
        if (background != null) {
            background.remove();
            background = null;
        }
        super.dispose();
    }

    /**
     * Shows the shop screen.
     * Pauses the game, refreshes the catalog, and updates balance display
     */
    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
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

    /**
     * Hides the shop screen and resumes game time.
     */
    public void hide() {
        ServiceLocator.getTimeSource().setPaused(false);
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
        pixelTex = makeSolidTexture(Color.WHITE);

        // Dimmer
        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        dimmer.setFillParent(true);
        dimmer.setColor(0f, 0f, 0f, 0.6f);
        stage.addActor(dimmer);

        // Black frame (outline)
        frame = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        frame.setSize(PANEL_W + 8, PANEL_H + 8);
        frame.setPosition((stage.getWidth() - frame.getWidth()) / 2f,
                (stage.getHeight() - frame.getHeight()) / 2f);
        frame.setColor(Color.BLACK);
        stage.addActor(frame);

        // Navy panel
        background = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        background.setSize(PANEL_W, PANEL_H);
        background.setPosition((stage.getWidth() - background.getWidth()) / 2f,
                (stage.getHeight() - background.getHeight()) / 2f);
        background.setColor(PANEL_COLOR);
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

        Image divider = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        divider.setColor(1f, 1f, 1f, 0.08f);
        root.add(divider)
                .width(PANEL_W - 40f)
                .height(2f)
                .padBottom(8f)
                .row();
    }

    // Create grid table and add to root
    private void buildGrid() {
        grid = new Table();
        grid.defaults().pad(12).size(CELL_W, CELL_H).uniform(true);
        root.add(grid).row();
    }

    // One footer row (Button and Balance)
    private void addFooter() {
        Label.LabelStyle balStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        balStyle.fontColor = GOLD;
        currencyLabel = new Label("", balStyle);
        currencyLabel.setFontScale(1.2f);
        updateCurrencyLabel();

        // Close button
        TextButton closeBtn = new TextButton(
                "Close Shop",
                skin.get("default", TextButton.TextButtonStyle.class)
        );
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        // Two overlayed rows: one centers the button, one right-aligns balance
        Stack footerStack = new Stack();

        Table centerRow = new Table();
        centerRow.center();
        centerRow.add(closeBtn).expandX().center();

        Table rightRow = new Table();
        rightRow.add(currencyLabel).expandX().right().padRight(12f);

        footerStack.add(centerRow);
        footerStack.add(rightRow);

        root.add(footerStack).growX().padTop(8f).padBottom(10f).row();
    }

    // Keep balance in sync
    private void subscribeCurrencyUpdates() {

        game.getPlayer().getEvents().addListener("updateProcessor", (Integer p) -> {
            if (currencyLabel != null) {
                currencyLabel.setText("Balance: $" + p);
            }
        });
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
}
