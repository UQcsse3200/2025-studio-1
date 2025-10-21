package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PlayerInventoryDisplay.
 * These tests avoid libGDX GL usage by:
 *  - constructing Slot via reflection with mocked Drawables
 *  - mocking ResourceService to return a mocked Texture
 *  - never invoking buildUI() or Pixmap/Texture creation paths
 */
class PlayerInventoryDisplayTest {

    /* --------------------------- reflection helpers --------------------------- */

    private static Field field(Class<?> owner, String name) {
        try {
            Field f = owner.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            fail("Missing field " + owner.getSimpleName() + "." + name + ": " + e);
            return null;
        }
    }

    private static Object get(Object target, Field f) {
        try { return f.get(target); }
        catch (Exception e) { fail("get failed: " + e); return null; }
    }

    private static void set(Object target, Field f, Object value) {
        try { f.set(target, value); }
        catch (Exception e) { fail("set failed: " + e); }
    }

    private static Method method(Class<?> owner, String name, Class<?>... params) {
        try {
            Method m = owner.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            fail("Missing method " + owner.getSimpleName() + "." + name + ": " + e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Object target, Method m, Object... args) {
        try { return (T) m.invoke(target, args); }
        catch (Exception e) { fail("invoke failed: " + e); return null; }
    }

    /* --------------------------- Slot construction --------------------------- */

    /** Create one Slot instance using the private inner class constructor. */
    private static Object newSlot() {
        try {
            Class<?> slotKlass = Class.forName("com.csse3200.game.components.player.PlayerInventoryDisplay$Slot");
            Constructor<?> ctor = slotKlass.getDeclaredConstructor(
                    Drawable.class, Drawable.class, Drawable.class, Label.LabelStyle.class);
            ctor.setAccessible(true);

            Drawable normal = mock(Drawable.class);
            Drawable focus  = mock(Drawable.class);
            Drawable badge  = mock(Drawable.class);

            Label.LabelStyle style = new Label.LabelStyle();
            style.font = new BitmapFont();

            return ctor.newInstance(normal, focus, badge, style);
        } catch (Exception e) {
            fail("Failed to construct Slot: " + e);
            return null;
        }
    }

    /** Reflect helpers for Slot internals */
    private static Image slotBg(Object slot) {
        Field f = field(slot.getClass(), "bg");
        return (Image) get(slot, f);
    }

    private static Image slotItem(Object slot) {
        Field f = field(slot.getClass(), "item");
        return (Image) get(slot, f);
    }

    private static Container<?> slotBadge(Object slot) {
        Field f = field(slot.getClass(), "badgeContainer");
        return (Container<?>) get(slot, f);
    }

    private static void slotSetItem(Object slot, Object texture) {
        Method m = method(slot.getClass(), "setItem", com.badlogic.gdx.graphics.Texture.class);
        invoke(slot, m, texture);
    }

    private static void slotClearItem(Object slot) {
        Method m = method(slot.getClass(), "clearItem");
        invoke(slot, m);
    }

    private static void slotSetCount(Object slot, int count) {
        Method m = method(slot.getClass(), "setCount", int.class);
        invoke(slot, m, count);
    }

    private static void slotSetHighlighted(Object slot, boolean h) {
        Method m = method(slot.getClass(), "setHighlighted", boolean.class);
        invoke(slot, m, h);
    }


    /** Create a PlayerInventoryDisplay with a pre-filled 'slots' array. */
    private static PlayerInventoryDisplay newDisplayWithSlots(int n) {
        InventoryComponent inv = mock(InventoryComponent.class);
        PlayerInventoryDisplay d = new PlayerInventoryDisplay(inv);

        // Inject slots Array<Slot>
        Field slotsF = field(PlayerInventoryDisplay.class, "slots");
        Array<Object> slots = new Array<>();
        for (int i = 0; i < n; i++) slots.add(newSlot());
        set(d, slotsF, slots);

        // Table needed for show/hide/toggleVisibility tests
        Field tableF = field(PlayerInventoryDisplay.class, "table");
        set(d, tableF, new Table());

        return d;
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void addInventoryItem_fillsFirstEmpty_andHidesBadgeByDefault() {
        // Arrange: two empty slots
        PlayerInventoryDisplay d = newDisplayWithSlots(2);

        // Mock resource service -> getAsset returns a mock Texture
        ResourceService rs = mock(ResourceService.class);
        com.badlogic.gdx.graphics.Texture tex = mock(com.badlogic.gdx.graphics.Texture.class);
        when(rs.getAsset("item.png", com.badlogic.gdx.graphics.Texture.class)).thenReturn(tex);
        ServiceLocator.registerResourceService(rs);

        // Act
        d.addInventoryItem("item.png");

        // Assert: slot[0] now has a drawable and is visible; badge hidden (count==1)
        Field slotsF = field(PlayerInventoryDisplay.class, "slots");
        Array<?> slots = (Array<?>) get(d, slotsF);
        Object slot0 = slots.get(0);

        assertNotNull(slotItem(slot0).getDrawable(), "item drawable should be set");
        assertTrue(slotItem(slot0).isVisible(), "item should be visible after add");
        assertFalse(slotBadge(slot0).isVisible(), "badge should be hidden for count==1");

        // Also verify resource service used
        verify(rs, times(1)).getAsset("item.png", com.badlogic.gdx.graphics.Texture.class);
    }

    @Test
    void addItem_outOfBounds_doesNothing() {
        PlayerInventoryDisplay d = newDisplayWithSlots(1);

        ResourceService rs = mock(ResourceService.class);
        when(rs.getAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class)))
                .thenReturn(mock(com.badlogic.gdx.graphics.Texture.class));
        ServiceLocator.registerResourceService(rs);

        // Act: -1 and 5 are OOB
        d.addItem(-1, "x.png");
        d.addItem(5, "x.png");

        // Assert: slot unchanged (still empty)
        Field slotsF = field(PlayerInventoryDisplay.class, "slots");
        Array<?> slots = (Array<?>) get(d, slotsF);
        Object slot0 = slots.get(0);

        assertNull(slotItem(slot0).getDrawable(), "OOB add should not touch slots");
        verify(rs, never()).getAsset(eq("x.png"), any());
    }

    @Test
    void clearSlot_emptiesItem_hidesBadge_andClearsFocusIfSameIndex() {
        PlayerInventoryDisplay d = newDisplayWithSlots(2);

        // Put item in slot 0
        ResourceService rs = mock(ResourceService.class);
        com.badlogic.gdx.graphics.Texture tex = mock(com.badlogic.gdx.graphics.Texture.class);
        when(rs.getAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(tex);
        ServiceLocator.registerResourceService(rs);
        d.addItem(0, "a.png");

        // Focus slot 0
        d.setFocusedIndex(0);

        // Clear slot 0
        d.clearSlot(0);

        Field slotsF = field(PlayerInventoryDisplay.class, "slots");
        Array<?> slots = (Array<?>) get(d, slotsF);
        Object slot0 = slots.get(0);

        // Item cleared
        assertNull(slotItem(slot0).getDrawable(), "clearSlot should remove drawable");
        assertFalse(slotItem(slot0).isVisible(), "clearSlot should hide item");
        assertFalse(slotBadge(slot0).isVisible(), "badge hidden when cleared");

        // Focus cleared
        Field focusedF = field(PlayerInventoryDisplay.class, "focusedIndex");
        assertEquals(-1, (int) get(d, focusedF));
    }

    @Test
    void clearAll_clearsEverything_andResetsFocus() {
        PlayerInventoryDisplay d = newDisplayWithSlots(3);

        // Populate slots 0 and 1 with visible items and counts
        ResourceService rs = mock(ResourceService.class);
        com.badlogic.gdx.graphics.Texture tex = mock(com.badlogic.gdx.graphics.Texture.class);
        when(rs.getAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(tex);
        ServiceLocator.registerResourceService(rs);

        d.addItem(0, "a.png");
        d.addItem(1, "b.png");

        // badge becomes visible only if count>1
        Field slotsF = field(PlayerInventoryDisplay.class, "slots");
        Array<?> slots = (Array<?>) get(d, slotsF);
        slotSetCount(slots.get(1), 3); // badge visible on slot1

        d.setFocusedIndex(1);

        // Act
        d.clearAll();

        // Assert: all empty & hidden
        for (int i = 0; i < slots.size; i++) {
            Object s = slots.get(i);
            assertFalse(slotItem(s).isVisible(), "slot " + i + " item should be hidden");
            assertNull(slotItem(s).getDrawable(), "slot " + i + " drawable should be null");
            assertFalse(slotBadge(s).isVisible(), "slot " + i + " badge hidden");
        }

        Field focusedF = field(PlayerInventoryDisplay.class, "focusedIndex");
        assertEquals(-1, (int) get(d, focusedF), "focus reset");
    }

    @Test
    void setFocusedIndex_switchesHighlightDrawable() {
        PlayerInventoryDisplay d = newDisplayWithSlots(2);

        // Capture initial bg drawables for comparison by flipping highlight
        Field slotsF = field(PlayerInventoryDisplay.class, "slots");
        Array<?> slots = (Array<?>) get(d, slotsF);

        Object s0 = slots.get(0);
        Object s1 = slots.get(1);

        // Initial (unhighlighted) states
        Drawable s0Normal = slotBg(s0).getDrawable();
        Drawable s1Normal = slotBg(s1).getDrawable();

        // Highlight slot 1
        d.setFocusedIndex(1);
        Drawable s1After = slotBg(s1).getDrawable();
        Drawable s0After = slotBg(s0).getDrawable();

        // Slot1 changed to focus drawable; slot0 stayed/returned to normal
        assertNotSame(s1Normal, s1After, "slot1 should switch to focus drawable");
        assertSame(s0Normal, s0After, "slot0 should not be focused");

        // Switch focus to slot0; verify swap again
        d.setFocusedIndex(0);
        assertNotSame(s0Normal, slotBg(s0).getDrawable(), "slot0 should now be focused");
        assertSame(s1Normal, slotBg(s1).getDrawable(), "slot1 should revert to normal");
    }

    @Test
    void updateCount_badgeVisibleOnlyWhenCountGt1_andItemVisible() {
        PlayerInventoryDisplay d = newDisplayWithSlots(1);

        // Put a visible item in slot0
        ResourceService rs = mock(ResourceService.class);
        com.badlogic.gdx.graphics.Texture tex = mock(com.badlogic.gdx.graphics.Texture.class);
        when(rs.getAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(tex);
        ServiceLocator.registerResourceService(rs);

        d.addItem(0, "thing.png");

        // Count 2 -> badge visible
        d.updateCount(0, 2);
        Field slotsF = field(PlayerInventoryDisplay.class, "slots");
        Array<?> slots = (Array<?>) get(d, slotsF);
        Object s0 = slots.get(0);
        assertTrue(slotBadge(s0).isVisible(), "badge visible for count>1");

        // Count 1 -> badge hidden
        d.updateCount(0, 1);
        assertFalse(slotBadge(s0).isVisible(), "badge hidden for count<=1");

        // If no item visible, even big count should hide badge
        slotClearItem(s0);
        d.updateCount(0, 99);
        assertFalse(slotBadge(s0).isVisible(), "badge stays hidden when no item");
    }

    @Test
    void showAndHide_toggleTableVisibilityFlags() {
        PlayerInventoryDisplay d = newDisplayWithSlots(1);
        Field tableF = field(PlayerInventoryDisplay.class, "table");
        Table table = (Table) get(d, tableF);

        d.hide();
        assertFalse(table.isVisible());

        d.show();
        assertTrue(table.isVisible());

        d.hide();
        assertFalse(table.isVisible());
    }

    @Test
    void addInventoryItem_skipsWhenFull_noExceptions() {
        PlayerInventoryDisplay d = newDisplayWithSlots(1);

        ResourceService rs = mock(ResourceService.class);
        com.badlogic.gdx.graphics.Texture tex = mock(com.badlogic.gdx.graphics.Texture.class);
        when(rs.getAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(tex);
        ServiceLocator.registerResourceService(rs);

        // Fill the only slot
        d.addItem(0, "a.png");

        // Now adding another should just log & return
        d.addInventoryItem("b.png");

        // Slot remains occupied by first item
        Field slotsF = field(PlayerInventoryDisplay.class, "slots");
        Array<?> slots = (Array<?>) get(d, slotsF);
        assertNotNull(slotItem(slots.get(0)).getDrawable());
    }

    @Test
    void updateCount_outOfBoundsSafe() {
        PlayerInventoryDisplay d = newDisplayWithSlots(2);

        // Exercise bounds; should not throw
        d.updateCount(-1, 5);
        d.updateCount(99, 5);
    }

    @Test
    void clearSlot_outOfBoundsSafe() {
        PlayerInventoryDisplay d = newDisplayWithSlots(2);
        d.clearSlot(-1);
        d.clearSlot(2);
        // no exception expected
    }

    @Test
    void addItem_usesResourceService_getAsset() {
        PlayerInventoryDisplay d = newDisplayWithSlots(1);

        ResourceService rs = mock(ResourceService.class);
        com.badlogic.gdx.graphics.Texture tex = mock(com.badlogic.gdx.graphics.Texture.class);
        when(rs.getAsset("weapon.png", com.badlogic.gdx.graphics.Texture.class)).thenReturn(tex);
        ServiceLocator.registerResourceService(rs);

        d.addItem(0, "weapon.png");
        verify(rs, times(1)).getAsset("weapon.png", com.badlogic.gdx.graphics.Texture.class);
    }

}