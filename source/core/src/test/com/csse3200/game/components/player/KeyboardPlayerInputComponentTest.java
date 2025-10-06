package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link KeyboardPlayerInputComponent}.
 *
 * These tests verify basic functionality such as selecting inventory slots,
 * equipping items, and unequipping items. No mocking is used; only simple
 * components are attached to the entity for testing purposes.
 */

public class KeyboardPlayerInputComponentTest {
    private Entity player;
    private KeyboardPlayerInputComponent inputComponent;

    /**
     * Sets up a new player entity with a KeyboardPlayerInputComponent,
     * InventoryComponent, and PlayerActions component before each test.
     */
    @BeforeEach
    public void setup() {
        player = new Entity();
        inputComponent = new KeyboardPlayerInputComponent();
        player.addComponent(inputComponent);

        InventoryComponent inventory = new InventoryComponent(5);
        player.addComponent(inventory);

        PlayerActions actions = new PlayerActions();
        player.addComponent(actions);

        inputComponent.setEntity(player);
    }


//    @Test
//    public void testTriggersWalkEvent() {
//        verify(events).trigger("walk", Vector2Utils.LEFT);
//    }

    /**
     * Tests that selecting an inventory slot equips the item if nothing
     * is currently equipped.
     */
    @Test
    @DisplayName("Test selecting an inventory slot equips the item")
    public void checkSlotTest() {
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
    public void equipCurrentItemTest() {
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
