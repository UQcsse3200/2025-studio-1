package com.csse3200.game.components.player;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link KeyboardPlayerInputComponent}.
 * <p>
 * These tests verify basic functionality such as selecting inventory slots,
 * equipping items, and unequipping items. No mocking is used; only simple
 * components are attached to the entity for testing purposes.
 */
class KeyboardPlayerInputComponentTest {
    private Entity player;
    private KeyboardPlayerInputComponent inputComponent;

    /**
     * Sets up a new player entity with a KeyboardPlayerInputComponent,
     * InventoryComponent, and PlayerActions component before each test.
     */
    @BeforeEach
    void setup() {
        player = new Entity();
        inputComponent = new KeyboardPlayerInputComponent();
        player.addComponent(inputComponent);

        InventoryComponent inventory = new InventoryComponent(5);
        player.addComponent(inventory);

        PlayerActions actions = new PlayerActions();
        player.addComponent(actions);

        inputComponent.setEntity(player);
    }

    /**
     * Tests that selecting an inventory slot equips the item if nothing
     * is currently equipped.
     */
    @Test
    @DisplayName("Test selecting an inventory slot equips the item")
    void checkSlotTest() {
        inputComponent.equipped = false;  // no item equipped initially
        inputComponent.checkSlot(0);       // select slot 0

        assertEquals(0, inputComponent.focusedItem);
        assertTrue(inputComponent.equipped); // should equip the item
    }

    /**
     * Tests that calling {@link KeyboardPlayerInputComponent#equipCurrentItem()}
     * sets the equipped state to true and maintains the focused slot.
     */
    @Test
    @DisplayName("Test equipping current item sets equipped to true")
    void equipCurrentItemTest() {
        inputComponent.focusedItem = 0;
        inputComponent.equipped = false;

        inputComponent.equipCurrentItem();

        assertTrue(inputComponent.equipped);
        assertEquals(0, inputComponent.focusedItem);
    }

    /**
     * Tests that calling {@link KeyboardPlayerInputComponent#unequipCurrentItem()}
     * sets the equipped state to false and resets the focused slot.
     */
    @Test
    @DisplayName("Test unequipping current item sets equipped to false")
    public void unequipCurrentItemTest() {
        inputComponent.focusedItem = 0;
        inputComponent.equipped = true;

        inputComponent.unequipCurrentItem();

        assertFalse(inputComponent.equipped);
        assertEquals(-1, inputComponent.focusedItem);
    }

}
