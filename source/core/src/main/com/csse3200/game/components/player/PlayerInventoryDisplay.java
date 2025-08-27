package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Big, simple inventory bar (single row).
 * Each slot is a Stack: [background square (always visible)] + [item image (optional)].
 * Supports highlighted vs non-highlighted boxes.
 */
public class PlayerInventoryDisplay extends UIComponent {
    private static final Logger log = LoggerFactory.getLogger(PlayerInventoryDisplay.class);

    private Table table;
    private final Array<Slot> slots = new Array<>();

    private static final int SLOTS = 5;
    private static final float SLOT_SIZE = 96f;
    private static final float SLOT_PAD  = 10f;

    // Box textures (make sure both are loaded by ResourceService)
    private static final String BG_TEX          = "images/ghost_1.png";        // normal
    private static final String BG_TEX_FOCUSED  = "images/ghost_king.png";  // highlighted

    private int focusedIndex = -1;

    @Override
    public void create() {
        super.create();
        buildUI();

        entity.getEvents().addListener("add item", this::addInventoryItem);
        entity.getEvents().addListener("remove item", this::clearSlot);
        entity.getEvents().addListener("remove all items", this::clearAll);
        entity.getEvents().addListener("focus item", this::setFocusedIndex);
    }

    private void buildUI() {
        table = new Table();
        table.setFillParent(true);
        table.center().bottom();
        table.padBottom(20f);

        // Preload background drawables
        Drawable normalBg  = new Image(ServiceLocator.getResourceService().getAsset(BG_TEX, Texture.class)).getDrawable();
        Drawable focusBg   = new Image(ServiceLocator.getResourceService().getAsset(BG_TEX_FOCUSED, Texture.class)).getDrawable();

        for (int i = 0; i < SLOTS; i++) {
            Slot slot = new Slot(normalBg, focusBg);
            slots.add(slot);
            table.add(slot).size(SLOT_SIZE).pad(SLOT_PAD);
        }

        stage.addActor(table);
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

    private int firstEmptySlot() {
        for (int i = 0; i < slots.size; i++) {
            if (slots.get(i).isEmpty()) return i;
        }
        return -1;
    }

    /** Focus a specific slot; pass -1 to clear focus. */
    public void setFocusedIndex(int index) {
        if (index == focusedIndex) return;

        // clear previous focus
        if (focusedIndex >= 0 && focusedIndex < slots.size) {
            slots.get(focusedIndex).setHighlighted(false);
        }

        focusedIndex = index;

        if (focusedIndex >= 0 && focusedIndex < slots.size) {
            slots.get(focusedIndex).setHighlighted(true);
        }
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

    /** A single inventory slot (background square + item image layered). */
    private static class Slot extends Stack {
        private final Image bg;
        private final Drawable normalBg;
        private final Drawable focusBg;
        private final Image item;

        Slot(Drawable normalBg, Drawable focusBg) {
            this.normalBg = normalBg;
            this.focusBg  = focusBg;

            bg = new Image(normalBg); // start normal

            item = new Image();
            item.setScaling(Scaling.fit);
            item.setVisible(false);

            add(bg);
            add(item);
        }

        void setItem(Texture tex) {
            item.setDrawable(new Image(tex).getDrawable());
            item.setVisible(true);
        }

        void clearItem() {
            item.setDrawable(null);
            item.setVisible(false);
        }

        boolean isEmpty() {
            return item.getDrawable() == null || !item.isVisible();
        }

        void setHighlighted(boolean highlighted) {
            // swap the background drawable
            bg.setDrawable(highlighted ? focusBg : normalBg);
        }
    }
}