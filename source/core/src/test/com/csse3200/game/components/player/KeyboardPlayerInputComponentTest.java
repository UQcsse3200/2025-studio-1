package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.Vector2Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link KeyboardPlayerInputComponent}.
 * <p>
 * These tests verify basic functionality such as selecting inventory slots,
 * equipping items, and unequipping items. No mocking is used; only simple
 * components are attached to the entity for testing purposes.
 */
class KeyboardPlayerInputComponentTest {
    private final Vector2 walkLeft = Vector2Utils.LEFT;
    private final Vector2 walkRight = Vector2Utils.RIGHT;

    private KeyboardPlayerInputComponent inputComponent;
    private PlayerActions actions;

    /**
     * Sets up a new player entity with a KeyboardPlayerInputComponent,
     * InventoryComponent, and PlayerActions component before each test.
     */
    @BeforeEach
    void setup() {
        EntityService entityService = mock(EntityService.class);
        ServiceLocator.registerInputService(mock(InputService.class));
        ServiceLocator.registerEntityService(entityService);
        ServiceLocator.registerTimeSource(mock(GameTime.class));
        ServiceLocator.registerResourceService(mock(ResourceService.class));

        when(ServiceLocator.getEntityService().getEntities()).thenReturn(new Array<Entity>());

        Body body = mock(Body.class);
        PhysicsComponent physics = mock(PhysicsComponent.class);
        when(physics.getBody()).thenReturn(body);
        when(physics.getBody().getLinearVelocity()).thenReturn(Vector2.Zero);

        Entity player = new Entity();
        inputComponent = new KeyboardPlayerInputComponent();
        player.addComponent(inputComponent);

        InventoryComponent inventory = new InventoryComponent(5);
        player.addComponent(inventory);

        actions = spy(new PlayerActions());

        player.addComponent(actions);
        player.addComponent(mock(StaminaComponent.class));
        player.addComponent(physics);

        inputComponent.setEntity(player);

        actions.create();
        inputComponent.create();
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    // NOTE: Testing walking
    @Test
    void shouldTriggerWalkLeftOnAPress() {
        assertTrue(inputComponent.keyPressed(Keys.A));
        verify(actions).walk(walkLeft);
    }

    @Test
    void shouldStopWalkingLeftWhenAReleased() {
        assertTrue(inputComponent.keyPressed(Keys.A));
        assertTrue(inputComponent.keyReleased(Keys.A));
        verify(actions).stopWalking();
    }

    @Test
    void shouldTriggerWalkRightOnDPress() {
        assertTrue(inputComponent.keyPressed(Keys.D));
        verify(actions).walk(walkRight);
    }

    @Test
    void shouldStopWalkingRightOnRelease() {
        assertTrue(inputComponent.keyPressed(Keys.D));
        assertTrue(inputComponent.keyReleased(Keys.D));
        verify(actions).stopWalking();
    }

    @Test
    void shouldNotMove() {
        assertTrue(inputComponent.keyPressed(Keys.A));
        assertTrue(inputComponent.keyPressed(Keys.D));
        verify(actions).stopWalking();
    }

    // NOTE: Testing crouching
    @Test
    void shouldCrouchSPress() {
        assertTrue(inputComponent.keyPressed(Keys.S));
        verify(actions).crouchAttempt();
    }

    @Test
    void shouldCrouchSRelease() {
        assertTrue(inputComponent.keyReleased(Keys.S));
    }

    // NOTE: Testing Sprinting
    @Test
    void shouldSprintOnPress() {
        assertTrue(inputComponent.keyPressed(Keys.SHIFT_LEFT));
        verify(actions).sprintAttempt();
    }

    @Test
    void shouldSprintOnRelease() {
        assertTrue(inputComponent.keyReleased(Keys.SHIFT_LEFT));
        verify(actions).stopSprinting();
    }

    @Test
    void shouldTriggerDash() {
        assertTrue(inputComponent.keyPressed(Keys.CONTROL_LEFT));
        verify(actions).dash();
    }

    @Test
    void shouldReload() {
        assertTrue(inputComponent.keyPressed(Keys.Q));
        verify(actions).reload();
    }

    @Test
    void shouldJump() {
        when(ServiceLocator.getResourceService()
                .getAsset("sounds/jump.mp3", Sound.class)).thenReturn(mock(Sound.class));

        assertTrue(inputComponent.keyPressed(Keys.SPACE));
        verify(actions).jump();
    }

    @Test //NOTE how to test
    void shouldInteractOnPress() {
        assertTrue(inputComponent.keyPressed(Keys.E));
    }

    @Test
    void shouldDisplayInventory() {
        assertTrue(inputComponent.keyPressed(Keys.I));
    }

    // NOTE: Test selecting slot
    @Test
    void shouldEquipWhenSlotSelected() {
        assertTrue(inputComponent.keyReleased(Keys.NUM_1));
        assertEquals(0, inputComponent.focusedItem);

        assertTrue(inputComponent.keyReleased(Keys.NUM_2));
        assertEquals(1, inputComponent.focusedItem);

        assertTrue(inputComponent.keyReleased(Keys.NUM_3));
        assertEquals(2, inputComponent.focusedItem);

        assertTrue(inputComponent.keyReleased(Keys.NUM_4));
        assertEquals(3, inputComponent.focusedItem);

        assertTrue(inputComponent.keyReleased(Keys.NUM_5));
        assertEquals(4, inputComponent.focusedItem);
        assertTrue(inputComponent.equipped);
    }

    @Test
    void shouldUnequipWhenSameSlotSelected() {
        assertTrue(inputComponent.keyReleased(Keys.NUM_1));
        assertTrue(inputComponent.equipped);
        assertTrue(inputComponent.keyReleased(Keys.NUM_1));
        assertEquals(-1, inputComponent.focusedItem);
        assertFalse(inputComponent.equipped);
    }

    @Test
    void shouldUnequipWhenSlotSelected() {
        assertTrue(inputComponent.keyReleased(Keys.NUM_1));
        assertTrue(inputComponent.keyReleased(Keys.NUM_2));
        assertTrue(inputComponent.equipped);
    }

    @Test
    void shouldDropFocused() {
        assertTrue(inputComponent.keyReleased(Keys.R));
    }

    @Test
    void shouldFailOnUnknownKey() {
        assertFalse(inputComponent.keyPressed(Keys.ENTER));
        assertFalse(inputComponent.keyReleased(Keys.ENTER));
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


    // NOTE: Testing equipItem() and unequipItem()

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
    void unequipCurrentItemTest() {
        inputComponent.focusedItem = 0;
        inputComponent.equipped = true;

        inputComponent.unequipCurrentItem();

        assertFalse(inputComponent.equipped);
        assertEquals(-1, inputComponent.focusedItem);
    }
}