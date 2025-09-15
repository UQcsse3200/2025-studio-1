package com.csse3200.game.components.shop;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryOperations: addOrStack()")
class InventoryOperationsTest {

    private Entity mkItem(String name) {
        Entity e = new Entity();
        ItemComponent item = new ItemComponent();
        item.setName(name);
        item.setTexture(name.toLowerCase() + ".png");
        e.addComponent(item);
        e.create();
        return e;
    }

    private InventoryComponent mkInventory(int startingProcessors) {
        Entity player = new Entity();
        InventoryComponent inv = new InventoryComponent(startingProcessors);
        player.addComponent(inv);
        player.create();
        return inv;
    }

    @Nested
    @DisplayName("Objective: insert new items")
    class InsertNewItems {
        @Test
        @DisplayName("Inserts into empty inventory at index 0 and sets count")
        void insertsIntoEmptyInventory() {
            InventoryComponent inv = mkInventory(0);
            Entity sword = mkItem("Sword");

            int idx = InventoryOperations.addOrStack(inv, sword, 1, 5);

            assertEquals(0, idx);
            assertSame(sword, inv.get(0));
            assertEquals(1, sword.getComponent(ItemComponent.class).getCount());
            assertEquals(1, inv.getSize());
        }

        @Test
        @DisplayName("Fails when inventory is full")
        void failsWhenFull() {
            InventoryComponent inv = mkInventory(0);
            // Fill 5-slot inventory with distinct items
            for (int i = 0; i < 5; i++) {
                Entity it = mkItem("Item" + i);
                assertTrue(InventoryOperations.addOrStack(inv, it, 1, 5) >= 0);
            }
            Entity extra = mkItem("Extra");
            int idx = InventoryOperations.addOrStack(inv, extra, 1, 5);
            assertEquals(-1, idx);
            assertEquals(5, inv.getSize());
        }
    }

    @Nested
    @DisplayName("Objective: stacking existing items")
    class Stacking {
        @Test
        @DisplayName("Stacks within maxStack and returns same index")
        void stacksWithinLimit() {
            InventoryComponent inv = mkInventory(0);
            Entity apple = mkItem("Apple");

            int first = InventoryOperations.addOrStack(inv, apple, 1, 5);
            assertEquals(0, first);
            assertEquals(1, apple.getComponent(ItemComponent.class).getCount());

            int again = InventoryOperations.addOrStack(inv, apple, 2, 5);
            assertEquals(0, again); // same slot
            assertEquals(3, apple.getComponent(ItemComponent.class).getCount());
            assertEquals(1, inv.getSize()); // still one slot used
        }

        @Test
        @DisplayName("Fails when stacking would exceed maxStack; count unchanged")
        void failsWhenExceedingMaxStack() {
            InventoryComponent inv = mkInventory(0);
            Entity bolt = mkItem("Bolt");

            assertEquals(0, InventoryOperations.addOrStack(inv, bolt, 3, 5));
            assertEquals(3, bolt.getComponent(ItemComponent.class).getCount());

            int res = InventoryOperations.addOrStack(inv, bolt, 3, 5); // 3 + 3 > 5
            assertEquals(-1, res);
            assertEquals(3, bolt.getComponent(ItemComponent.class).getCount()); // unchanged
        }
    }

    @Nested
    @DisplayName("Objective: invalid inputs")
    class InvalidInputs {
        @Test
        @DisplayName("Nulls or non-positive amounts/maxStack return FAILURE")
        void invalidArgsReturnFailure() {
            InventoryComponent inv = mkInventory(0);
            Entity item = mkItem("Thing");

            assertEquals(-1, InventoryOperations.addOrStack(null, item, 1, 5));
            assertEquals(-1, InventoryOperations.addOrStack(inv, null, 1, 5));
            assertEquals(-1, InventoryOperations.addOrStack(inv, item, 0, 5));
            assertEquals(-1, InventoryOperations.addOrStack(inv, item, -2, 5));
            assertEquals(-1, InventoryOperations.addOrStack(inv, item, 1, 0));
            assertEquals(-1, InventoryOperations.addOrStack(inv, item, 1, -3));
        }

        @Test
        @DisplayName("Missing ItemComponent returns FAILURE")
        void missingItemComponentReturnsFailure() {
            InventoryComponent inv = mkInventory(0);
            Entity bare = new Entity();
            bare.create();
            assertEquals(-1, InventoryOperations.addOrStack(inv, bare, 1, 5));
        }
    }
}