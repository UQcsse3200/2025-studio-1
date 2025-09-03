package com.csse3200.game.components.player;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PlayerInventoryDisplayTest {

    /** Test double that skips UI wiring in create() */
    static class TestInventoryDisplay extends PlayerInventoryDisplay {
        TestInventoryDisplay(InventoryComponent inventory) {
            super(inventory);
        }
        @Override public void create() {
        }
    }

    private TestInventoryDisplay display;
    private static Field fSlots;
    private static Field fFocusedIndex;
    private static Constructor<?> ctorSlot;
    private static Field fSlotBg;
    private static Field fSlotNormalBg;
    private static Field fSlotFocusBg;
    private static Field fSlotItem;
    private static Method mItemSetVisible;

    @BeforeEach
    void setUp() throws Exception {
        display = new TestInventoryDisplay(mock(InventoryComponent.class));

        if (fSlots == null) {
            fSlots = PlayerInventoryDisplay.class.getDeclaredField("slots");
            fSlots.setAccessible(true);

            fFocusedIndex = PlayerInventoryDisplay.class.getDeclaredField("focusedIndex");
            fFocusedIndex.setAccessible(true);

            Class<?> cSlot = Class.forName(PlayerInventoryDisplay.class.getName() + "$Slot");
            ctorSlot = cSlot.getDeclaredConstructor(Drawable.class, Drawable.class);
            ctorSlot.setAccessible(true);

            fSlotBg = cSlot.getDeclaredField("bg");
            fSlotBg.setAccessible(true);

            fSlotNormalBg = cSlot.getDeclaredField("normalBg");
            fSlotNormalBg.setAccessible(true);

            fSlotFocusBg = cSlot.getDeclaredField("focusBg");
            fSlotFocusBg.setAccessible(true);

            fSlotItem = cSlot.getDeclaredField("item");
            fSlotItem.setAccessible(true);

            mItemSetVisible = Image.class.getMethod("setVisible", boolean.class);
        }

        Array<Object> slots = new Array<>();
        Drawable normal = mock(Drawable.class);
        Drawable focused = mock(Drawable.class);

        for (int i = 0; i < 5; i++) {
            Object slot = ctorSlot.newInstance(normal, focused);
            slots.add(slot);
        }
        // Inject slots into the component
        fSlots.set(display, slots);
        // Ensure no focus initially
        fFocusedIndex.setInt(display, -1);
    }

    /** Utility: set an "item" into a slot by directly setting its Image drawable/visibility */
    private void putItemInSlot(int index) throws Exception {
        @SuppressWarnings("unchecked")
        Array<Object> slots = (Array<Object>) fSlots.get(display);
        Object slot = slots.get(index);
        Image itemImg = (Image) fSlotItem.get(slot);
        itemImg.setDrawable(mock(Drawable.class));
        mItemSetVisible.invoke(itemImg, true);
    }

    /** Utility: read which background drawable is currently set for a slot */
    private Drawable currentBgDrawable(Object slot) throws Exception {
        Image bg = (Image) fSlotBg.get(slot);
        return bg.getDrawable();
    }

    /** Utility: get the "normal" and "focus" drawables saved inside a slot */
    private Drawable normalBg(Object slot) throws Exception {
        return (Drawable) fSlotNormalBg.get(slot);
    }

    private Drawable focusBg(Object slot) throws Exception {
        return (Drawable) fSlotFocusBg.get(slot);
    }

    /** Utility: check if a slot is empty by inspecting its Image state */
    private boolean isSlotEmpty(int index) throws Exception {
        @SuppressWarnings("unchecked")
        Array<Object> slots = (Array<Object>) fSlots.get(display);
        Object slot = slots.get(index);
        Image itemImg = (Image) fSlotItem.get(slot);
        return itemImg.getDrawable() == null || !itemImg.isVisible();
    }

    @Test
    void setFocusedIndex_togglesBackgroundAndClearsPrevious() throws Exception {
        @SuppressWarnings("unchecked")
        Array<Object> slots = (Array<Object>) fSlots.get(display);

        Object slot0 = slots.get(0);
        Object slot3 = slots.get(3);

        // Initially no focus
        assertEquals(-1, fFocusedIndex.getInt(display));
        assertEquals(normalBg(slot0), currentBgDrawable(slot0));
        assertEquals(normalBg(slot3), currentBgDrawable(slot3));

        // Focus index 3
        display.setFocusedIndex(3);
        assertEquals(3, fFocusedIndex.getInt(display));
        assertEquals(focusBg(slot3), currentBgDrawable(slot3));
        assertEquals(normalBg(slot0), currentBgDrawable(slot0)); // others unchanged

        // Move focus to 0; 3 should un-highlight
        display.setFocusedIndex(0);
        assertEquals(0, fFocusedIndex.getInt(display));
        assertEquals(focusBg(slot0), currentBgDrawable(slot0));
        assertEquals(normalBg(slot3), currentBgDrawable(slot3));

        // Clear focus with -1
        display.setFocusedIndex(-1);
        assertEquals(-1, fFocusedIndex.getInt(display));
        assertEquals(normalBg(slot0), currentBgDrawable(slot0));
        assertEquals(normalBg(slot3), currentBgDrawable(slot3));
    }

    @Test
    void clearSlot_removesItemAndClearsFocusIfFocused() throws Exception {
        putItemInSlot(2);
        display.setFocusedIndex(2);
        assertEquals(2, fFocusedIndex.getInt(display));

        assertFalse(isSlotEmpty(2));

        display.clearSlot(2);

        assertTrue(isSlotEmpty(2));
        assertEquals(-1, fFocusedIndex.getInt(display));
    }

    @Test
    void clearAll_removesEveryItemAndResetsFocus() throws Exception {
        putItemInSlot(0);
        putItemInSlot(1);
        putItemInSlot(3);
        display.setFocusedIndex(1);
        assertEquals(1, fFocusedIndex.getInt(display));

        // Sanity
        assertFalse(isSlotEmpty(0));
        assertFalse(isSlotEmpty(1));
        assertFalse(isSlotEmpty(3));

        // Clear everything
        display.clearAll();

        // All empty and focus reset
        for (int i = 0; i < 5; i++) {
            assertTrue(isSlotEmpty(i), "slot " + i + " should be empty");
        }
        assertEquals(-1, fFocusedIndex.getInt(display));
    }

    @Test
    void clearSlot_outOfRangeIsNoop() throws Exception {
        putItemInSlot(0);
        assertFalse(isSlotEmpty(0));
        display.clearSlot(-5);
        display.clearSlot(999);

        assertFalse(isSlotEmpty(0));
    }

    @Test
    void setFocusedIndex_outOfRangeIsGracefulAndClearsPrevious() throws Exception {
        display.setFocusedIndex(4);
        assertEquals(4, fFocusedIndex.getInt(display));
        display.setFocusedIndex(42);

        @SuppressWarnings("unchecked")
        Array<Object> slots = (Array<Object>) fSlots.get(display);
        Object slot4 = slots.get(4);
        assertEquals(normalBg(slot4), currentBgDrawable(slot4));
    }
}