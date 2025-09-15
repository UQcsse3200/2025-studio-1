package com.csse3200.game.components.shop;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryOperations: addOrStack()")
public class InventoryOperationsTest {

    private Entity mkItem(String name) {
        Entity e = new Entity();
        ItemComponent ic = new ItemComponent();
        ic.setName(name);
        ic.setTexture("x.png");
        e.addComponent(ic);
        e.create();
        return e;
    }

    private InventoryComponent mkInventory(int processors) {
        Entity player = new Entity();
        InventoryComponent inv = new InventoryComponent(processors);
        player.addComponent(inv);
        player.create();
        return inv;
    }

    @Nested
    @DisplayName("Objective: insert new items")
    class InsertNew {
        @Test
        @DisplayName("insertsIntoEmpty_atIndex0_andSetsCount()")
        void insertsIntoEmpty_atIndex0_andSetsCount() {
            InventoryComponent inv = mkInventory(0);
            Entity item = mkItem("Gem");

            int idx = InventoryOperations.addOrStack(inv, item, 1, 10);
            assertTrue(idx >= 0, "Expected a valid index");
            assertTrue(inv.getInventory().contains(item), "Item should be in inventory");
            assertEquals(1, item.getComponent(ItemComponent.class).getCount());
        }

        @Test
        @DisplayName("failsWhenInventoryIsFull()")
        void failsWhenInventoryIsFull() {
            InventoryComponent inv = mkInventory(0);
            // Fill 5 slots (InventoryComponent maxCapacity = 5)
            for (int i = 0; i < 5; i++) {
                inv.addItem(mkItem("Filler-" + i));
            }
            Entity item = mkItem("New");
            int code = InventoryOperations.addOrStack(inv, item, 1, 10);
            assertEquals(PurchaseError.INVENTORY_FULL.getCode(), code);
        }
    }

    @Nested
    @DisplayName("Objective: stacking existing items")
    class Stacking {
        @Test
        @DisplayName("stacksWithinMaxStack_returnsSameIndex()")
        void stacksWithinMaxStack_returnsSameIndex() {
            InventoryComponent inv = mkInventory(0);
            Entity item = mkItem("Potion");
            inv.addItem(item); // put same entity in inventory

            int idx = InventoryOperations.addOrStack(inv, item, 2, 5);
            assertTrue(idx >= 0, "Should return index of existing slot");
            assertEquals(3, item.getComponent(ItemComponent.class).getCount()); // 1 + 2
        }

        @Test
        @DisplayName("failsWhenStackWouldExceedMaxStack_countUnchanged()")
        void failsWhenStackWouldExceedMaxStack_countUnchanged() {
            InventoryComponent inv = mkInventory(0);
            Entity item = mkItem("Arrow");
            inv.addItem(item);
            item.getComponent(ItemComponent.class).setCount(3);

            int code = InventoryOperations.addOrStack(inv, item, 1, 3); // would exceed
            assertEquals(PurchaseError.LIMIT_REACHED.getCode(), code);
            assertEquals(3, item.getComponent(ItemComponent.class).getCount(), "Count should not change");
        }
    }

    @Nested
    @DisplayName("Objective: invalid inputs")
    class InvalidInputs {
        @Test
        @DisplayName("nullsOrNonPositiveAmountsOrMaxStack_returnFailure()")
        void nullsOrNonPositiveAmountsOrMaxStack_returnFailure() {
            Entity item = mkItem("Any");
            InventoryComponent inv = mkInventory(0);

            assertEquals(PurchaseError.UNEXPECTED.getCode(), InventoryOperations.addOrStack(null, item, 1, 5));
            assertEquals(PurchaseError.UNEXPECTED.getCode(), InventoryOperations.addOrStack(inv, null, 1, 5));
            assertEquals(PurchaseError.UNEXPECTED.getCode(), InventoryOperations.addOrStack(inv, item, 0, 5));
            assertEquals(PurchaseError.UNEXPECTED.getCode(), InventoryOperations.addOrStack(inv, item, 1, 0));
        }

        @Test
        @DisplayName("missingItemComponent_returnsFailure()")
        void missingItemComponent_returnsFailure() {
            InventoryComponent inv = mkInventory(0);
            Entity bare = new Entity();
            bare.create();

            int code = InventoryOperations.addOrStack(inv, bare, 1, 5);
            assertEquals(PurchaseError.INVALID_ITEM.getCode(), code);
        }
    }
}