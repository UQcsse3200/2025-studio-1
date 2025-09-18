package com.csse3200.game.components.player;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemPickUpComponent focusing on event-driven behaviour:
 * - picking up the current target item
 * - focusing an inventory slot
 * - dropping the focused item
 */
@ExtendWith(GameExtension.class)
class ItemPickUpComponentTest {

    private static final int MAX_SLOTS = 5;
    private Entity player;
    private InventoryComponent inventory;
    private ItemPickUpComponent pickup;

    /**
     * Two helper methods to let us test the code directly.
     */

    private static void setPrivate(Object obj, String fieldName, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getPrivate(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        inventory = new InventoryComponent(/*processor=*/0);
        pickup = new ItemPickUpComponent(inventory);
        ServiceLocator.registerEntityService(new EntityService());

        player = new Entity()
                .addComponent(inventory)
                .addComponent(pickup);
        player.create();
    }

    @Nested
    @DisplayName("Pickup behaviour")
    class PickupBehaviour {

        @Test
        @DisplayName("Picking up a valid target item adds it to inventory and clears target")
        void pickUpAddsItemAndClearsTarget() {
            Entity worldItem = new Entity().addComponent(new ItemComponent());
            worldItem.create();

            // Simulate collision target present
            setPrivate(pickup, "targetItem", worldItem);

            player.getEvents().trigger("pick up");

            assertEquals(1, inventory.getSize(), "Item should be added to inventory");
            assertSame(worldItem, inventory.get(0), "First slot should contain picked up item");

            assertNull(getPrivate(pickup, "targetItem"), "targetItem should be cleared after pickup");
        }

        @Test
        @DisplayName("Pickup with full inventory does not clear target and does not add")
        void pickUpFailsWhenFull() {
            // Fill inventory
            for (int i = 0; i < MAX_SLOTS; i++) {
                assertTrue(inventory.addItem(new Entity().addComponent(new ItemComponent())));
            }
            assertTrue(inventory.isFull());

            Entity worldItem = new Entity().addComponent(new ItemComponent());
            setPrivate(pickup, "targetItem", worldItem);

            player.getEvents().trigger("pick up");

            assertEquals(MAX_SLOTS, inventory.getSize(), "Inventory size should remain full");
            assertSame(worldItem, getPrivate(pickup, "targetItem"),
                    "targetItem should remain (pickup failed, still in range)");
        }
    }

    @Nested
    @DisplayName("Focus behaviour")
    class FocusBehaviour {
        @Test
        @DisplayName("Valid focus indices (0..4) are accepted")
        void validFocusAccepted() {
            player.getEvents().trigger("focus item", 3);
            assertEquals(3, (int) getPrivate(pickup, "focusedIndex"));
        }

        @Test
        @DisplayName("Invalid focus indices clear focus")
        void invalidFocusClears() {
            player.getEvents().trigger("focus item", -1);
            assertEquals(-1, (int) getPrivate(pickup, "focusedIndex"));

            player.getEvents().trigger("focus item", 7);
            assertEquals(-1, (int) getPrivate(pickup, "focusedIndex"));
        }
    }

    @Nested
    @DisplayName("Drop behaviour")
    class DropBehaviour {
        @Test
        @DisplayName("Dropping with no focus does nothing")
        void dropWithoutFocusNoop() {
            setPrivate(pickup, "focusedIndex", -1);

            assertEquals(0, inventory.getSize());

            player.getEvents().trigger("drop focused");

            // Post-state unchanged
            assertEquals(0, inventory.getSize());
        }

        @Test
        @DisplayName("Dropping focused empty slot does nothing")
        void dropEmptyFocusedNoop() {
            // Focus slot 2, but leave it empty
            player.getEvents().trigger("focus item", 2);
            assertEquals(2, (int) getPrivate(pickup, "focusedIndex"));

            player.getEvents().trigger("drop focused");
            assertEquals(0, inventory.getSize(), "Nothing to remove");
            assertNull(inventory.get(2), "Slot remains empty");
        }

        @Test
        @DisplayName("Dropping focused slot with item removes it from inventory")
        void dropRemovesFocusedItem() {
            // Put two items into inventory (slots 0 and 1)
            Entity item0 = new Entity().addComponent(new ItemComponent());
            Entity item1 = new Entity().addComponent(new ItemComponent());
            assertTrue(inventory.addItem(item0));
            assertTrue(inventory.addItem(item1));
            assertEquals(2, inventory.getSize());

            // Focus slot 1 and drop
            player.getEvents().trigger("focus item", 1);
            player.getEvents().trigger("drop focused");

            assertEquals(1, inventory.getSize(), "One item should be removed");
            assertSame(item0, inventory.get(0), "Slot 0 remains item0");
            assertNull(inventory.get(1), "Slot 1 cleared");

            // Dropping again on the now-empty focused slot should do nothing
            player.getEvents().trigger("drop focused");
            assertEquals(1, inventory.getSize());
        }

        @Test
        @DisplayName("Drop ignores out-of-range focus values")
        void dropIgnoresOutOfRangeFocus() {
            setPrivate(pickup, "focusedIndex", 99);
            player.getEvents().trigger("drop focused");
            assertEquals(0, inventory.getSize());
        }
    }

    @Test
    @DisplayName("Drop clears focus and tolerates null GameArea")
    void dropClearsFocusWithoutGameArea() {
        // Put one item in slot 0
        Entity item0 = new Entity().addComponent(new ItemComponent());
        assertTrue(inventory.addItem(item0));

        // Focus slot 0 and drop with NO game area registered
        player.getEvents().trigger("focus item", 0);
        player.getEvents().trigger("drop focused");

        // Item removed and focus cleared
        assertEquals(0, inventory.getSize(), "Inventory should be empty after drop");
        assertEquals(-1, (int) getPrivate(pickup, "focusedIndex"), "Focus should be cleared (-1)");
    }

    @Test
    @DisplayName("Drop ignores unknown textures (no respawn)")
    void dropUnknownTextureNoRespawn() {
        // Make an item whose texture is unknown to the factory
        ItemComponent itemComponent = new ItemComponent();
        itemComponent.setTexture("images/dont-exist.png");
        Entity unknown = new Entity().addComponent(itemComponent);

        assertTrue(inventory.addItem(unknown));
        player.getEvents().trigger("focus item", 0);
        player.getEvents().trigger("drop focused");

        // Item removed; respawn skipped
        assertEquals(0, inventory.getSize());
    }

    @Test
    @DisplayName("Drop skips respawn when item has no texture")
    void dropSkipsRespawnWhenNoTexture() {
        // ItemComponent with no texture set
        Entity blank = new Entity().addComponent(new ItemComponent());
        assertTrue(inventory.addItem(blank));
        assertEquals(1, inventory.getSize());

        // Focus and drop
        player.getEvents().trigger("focus item", 0);
        player.getEvents().trigger("drop focused");

        // Removed but not respawned
        assertEquals(0, inventory.getSize());
    }
}
