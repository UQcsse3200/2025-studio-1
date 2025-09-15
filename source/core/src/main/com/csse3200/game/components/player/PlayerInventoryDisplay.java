package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The UI component of the inventory.
 * Use the triggers: "add item," "remove item," "remove all items", "focus item"
 */
public class PlayerInventoryDisplay extends UIComponent {
    private static final Logger log = LoggerFactory.getLogger(PlayerInventoryDisplay.class);

    private Table table;
    private final Array<Slot> slots = new Array<>();

    private static final int NUM_SLOTS = 5;
    private static final float SLOT_SIZE = 96f;
    private static final float SLOT_PAD  = 10f;

    private int focusedIndex = -1;

    private final InventoryComponent inventory;

    /** TODO what happens if this is gone, along with the
     * Constructs the PlayerInventory display, takes in an InventoryComponent
     * so that it can handle displaying the item textures etc.
     * @param inventory An already initialised InventoryComponent
     */
    public PlayerInventoryDisplay(InventoryComponent inventory) {
        this.inventory = inventory;
    }

    @Override
    public void create() {
        super.create();
        buildUI();
        entity.getEvents().addListener("add item", this::addItem);
        entity.getEvents().addListener("remove item", this::clearSlot);
        entity.getEvents().addListener("remove all items", this::clearAll);
        entity.getEvents().addListener("focus item", this::setFocusedIndex);
        entity.getEvents().addListener("update item count", this::updateCount);
    }

    /**
     * Function to draw the inventory boxes
     * @param r,g,b,a fill color + transparency
     * @param borderWidth width of box border
     * @param br,bg,bb,ba border color + transparency
     */
    private Drawable createSlotBg(float r, float g, float b, float a,
                                  int borderWidth, float br, float bg, float bb, float ba) {
        int size = 32;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        // Setting actual box colour
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        // Setting box border colour
        pixmap.setColor(br, bg, bb, ba);
        for (int i = 0; i < borderWidth; i++) {
            pixmap.drawRectangle(i, i, size - i * 2, size - i * 2);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    /**
     * This function will build the basic structure of the inventory on the screen of
     * the player.
     * This will happen on creation.
     */
    private void buildUI() {
        table = new Table();
        table.setFillParent(true);
        table.center().bottom();
        table.padBottom(20f);

        Drawable normalBg = createSlotBg(0.2f, 0.2f, 0.2f, 0.6f, 2, 1f, 1f, 1f, 1f);
        Drawable focusBg  = createSlotBg(1f, 1f, 0f, 0.6f, 2, 1f, 1f, 0f, 1f);

        Drawable badgeBg  = createBadgeBg(0f, 0f, 0f, 0.65f);
        BitmapFont font   = new BitmapFont();
        Label.LabelStyle countStyle = new Label.LabelStyle(font, Color.WHITE);

        for (int i = 0; i < NUM_SLOTS; i++) {
            Slot slot = new Slot(normalBg, focusBg, badgeBg, countStyle);
            slots.add(slot);
            table.add(slot).size(SLOT_SIZE).pad(SLOT_PAD);
        }

        stage.addActor(table);

        setFocusedIndex(focusedIndex);
    }

    /** Add an item to the first empty slot. */
    public void addInventoryItem(String texturePath) {
        int idx = firstEmptySlot();
        if (idx == -1) {
            log.debug("No empty slot available");
            return;
        }
        addItem(idx, texturePath);
    }

    /** Put an item in a specific slot. */
    public void addItem(int index, String texturePath) {
        if (index < 0 || index >= slots.size) return;
        Texture tex = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
        slots.get(index).setItem(tex);
    }

    /** Clear a specific slot (keeps background square, hides item). */
    public void clearSlot(int index) {
        if (index < 0 || index >= slots.size) return;
        slots.get(index).clearItem();
        if (index == focusedIndex) setFocusedIndex(-1);
    }

    /** Remove everything (backgrounds remain visible). */
    public void clearAll() {
        for (int i = 0; i < slots.size; i++) clearSlot(i);
        setFocusedIndex(-1);
    }

    /**
     * Function that finds the first available slot
     * @return index of the place of the first empty spot. If none are found
     * returns -1
     */
    private int firstEmptySlot() {
        for (int i = 0; i < slots.size; i++) {
            if (slots.get(i).isEmpty()) return i;
        }
        return -1;
    }

    /** Focus a specific slot; pass -1 to clear focus. */
    public void setFocusedIndex(int index) {
        // clear previous focus
        if (focusedIndex >= 0 && focusedIndex < slots.size) {
            slots.get(focusedIndex).setHighlighted(false);
        }

        focusedIndex = index;

        if (focusedIndex >= 0 && focusedIndex < slots.size) {
            slots.get(focusedIndex).setHighlighted(true);
        }
    }

    public void updateCount(int index, int count) {
        if (index < 0 || index >= slots.size) return;
        System.out.println(index + ": " + count);
        slots.get(index).setCount(count);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // Stage draws everything
    }

    @Override
    public void dispose() {
        super.dispose();
        if (table != null) table.remove();
    }

    private Drawable createBadgeBg(float r, float g, float b, float a) {
        int w = 28, h = 20; // small pill
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // transparent
        pixmap.fill();

        // pill background
        pixmap.setColor(r, g, b, a);
        int radius = h / 2;
        // left & right semicircles
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(w - radius - 1, radius, radius);
        // center rect
        pixmap.fillRectangle(radius, 0, w - 2 * radius, h);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    /** A single inventory slot (background square + item image layered). */
    private static class Slot extends Stack {
        private final Image bg;
        private final Drawable normalBg;
        private final Drawable focusBg;
        private final Image item;

        private final Container<Label> badgeContainer;
        private final Label countLabel;
        private int lastCount = 0;

        Slot(Drawable normalBg, Drawable focusBg, Drawable badgeBg, Label.LabelStyle countStyle) {
            this.normalBg = normalBg;
            this.focusBg  = focusBg;

            bg = new Image(normalBg);

            item = new Image();
            item.setScaling(Scaling.fit);
            item.setVisible(false);

            countLabel = new Label("", countStyle);
            countLabel.setAlignment(Align.center);

            badgeContainer = new Container<>(countLabel);
            badgeContainer.background(badgeBg);
            badgeContainer.pad(2f, 6f, 2f, 6f); // top,right,bottom,left padding
            badgeContainer.setVisible(false);

            // position badge bottom-right inside the slot
            Table overlay = new Table();
            overlay.add().expand().fill();
            overlay.row();
            Table bottomRow = new Table();
            bottomRow.add().expandX().fillX();
            bottomRow.add(badgeContainer).bottom().right().pad(4f);
            overlay.add(bottomRow).expandX().fillX().bottom();

            add(bg);
            add(item);
            add(overlay);
        }

        void setItem(Texture tex) {
            item.setDrawable(new Image(tex).getDrawable());
            item.setVisible(true);

            if (lastCount <= 0) {
                setCount(1);
            }

        }

        void clearItem() {
            item.setDrawable(null);
            item.setVisible(false);
            setCount(0);
        }

        boolean isEmpty() {
            return item.getDrawable() == null || !item.isVisible();
        }

        void setHighlighted(boolean highlighted) {
            // swap the background drawable
            bg.setDrawable(highlighted ? focusBg : normalBg);
        }

        void setCount(int count) {
            lastCount = count;
            if (count > 1 && item.isVisible()) {
                countLabel.setText("x" + count);
                badgeContainer.setVisible(true);
            } else {
                badgeContainer.setVisible(false);
            }
        }
    }
}