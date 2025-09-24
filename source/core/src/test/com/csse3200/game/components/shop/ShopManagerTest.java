package com.csse3200.game.components.shop;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShopManager")
public class ShopManagerTest {

    private Entity mkItemEntity(String name) {
        Entity e = new Entity();
        ItemComponent ic = new ItemComponent();
        ic.setName(name);
        ic.setTexture("x.png");
        e.addComponent(ic);
        e.create();
        return e;
    }

    private CatalogEntry mkEntry(String name, int price, boolean enabled, int maxStack, int bundleQty) {
        return new CatalogEntry(mkItemEntity(name), price, enabled, maxStack, bundleQty);
    }

    private InventoryComponent attachInventory(Entity player, int processors) {
        InventoryComponent inv = new InventoryComponent(processors);
        player.addComponent(inv);
        player.create();
        return inv;
    }

    /**
     * Attach manager to an entity so it can fire events without NPEs.
     */
    private ShopManager mkManager(CatalogService catalog) {
        ShopManager m = new ShopManager(catalog);
        Entity shop = new Entity();
        shop.addComponent(m);
        shop.create();
        return m;
    }

    @Nested
    @DisplayName("Objective: Success behaviour")
    class Success {

        @Test
        @DisplayName("Purchase succeeds: charges funds and adds item")
        void purchaseSucceeds_chargesFunds_andAddsItem() {
            CatalogEntry sword = mkEntry("Sword", 50, true, 10, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(sword))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 200);

            PurchaseResult r = manager.purchase(player, sword, 1);

            assertTrue(r.ok());
            assertEquals(PurchaseError.NONE, r.error());
            assertEquals(150, inv.getProcessor()); // 200 - 50
            assertTrue(inv.getInventory().contains(sword.getItem()));
            assertEquals(1, sword.getItem().getComponent(ItemComponent.class).getCount());
        }

        @Test
        @DisplayName("Stacks when same entity exists and within max stack")
        void stacksWhenSameEntity_andWithinMaxStack() {
            CatalogEntry arrows = mkEntry("Arrow", 5, true, 5, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(arrows))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 100);

            // Put the same entity in inventory first with count=1
            inv.addItem(arrows.getItem());
            arrows.getItem().getComponent(ItemComponent.class).setCount(1);

            PurchaseResult r = manager.purchase(player, arrows, 1);
            assertTrue(r.ok());
            assertEquals(PurchaseError.NONE, r.error());
            assertEquals(95, inv.getProcessor()); // 100 - 5
            assertEquals(2, arrows.getItem().getComponent(ItemComponent.class).getCount());
        }
    }

    @Nested
    @DisplayName("Objective: Failure behaviour")
    class Failure {

        @Test
        @DisplayName("Fails when insufficient funds")
        void failsWhenInsufficientFunds() {
            CatalogEntry sword = mkEntry("Sword", 50, true, 10, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(sword))));
            Entity player = new Entity();
            attachInventory(player, 10);

            PurchaseResult r = manager.purchase(player, sword, 1);
            assertFalse(r.ok());
            assertEquals(PurchaseError.INSUFFICIENT_FUNDS, r.error());
        }

        @Test
        @DisplayName("Fails when item not found in catalog")
        void failsWhenItemNotFound() {
            CatalogEntry ghost = mkEntry("Ghost", 10, true, 10, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>())); // empty
            Entity player = new Entity();
            attachInventory(player, 100);

            PurchaseResult r = manager.purchase(player, ghost, 1);
            assertFalse(r.ok());
            assertEquals(PurchaseError.NOT_FOUND, r.error());
        }

        @Test
        @DisplayName("Fails when item is disabled")
        void failsWhenItemDisabled() {
            CatalogEntry disabled = mkEntry("Relic", 20, false, 10, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(disabled))));
            Entity player = new Entity();
            attachInventory(player, 100);

            PurchaseResult r = manager.purchase(player, disabled, 1);
            assertFalse(r.ok());
            assertEquals(PurchaseError.DISABLED, r.error());
        }

        @Test
        @DisplayName("Fails with UNEXPECTED when player has no InventoryComponent")
        void failsWhenNoInventoryComponent() {
            CatalogEntry item = mkEntry("Thing", 1, true, 10, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(item))));
            Entity player = new Entity();

            PurchaseResult r = manager.purchase(player, item, 1);
            assertFalse(r.ok());
            assertEquals(PurchaseError.UNEXPECTED, r.error());
        }

        @Test
        @DisplayName("Fails when inventory is full for a new item")
        void failsWhenInventoryFull_newItem() {
            CatalogEntry item = mkEntry("Rune", 5, true, 10, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(item))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 100);

            // Fill 5 slots (InventoryComponent maxCapacity = 5)
            for (int i = 0; i < 5; i++) {
                inv.addItem(mkItemEntity("Filler-" + i));
            }

            PurchaseResult r = manager.purchase(player, item, 1);
            assertFalse(r.ok());
            assertEquals(PurchaseError.INVENTORY_FULL, r.error());
        }

        @Test
        @DisplayName("Fails when stacking would exceed max stack")
        void failsWhenStackWouldExceedMax() {
            CatalogEntry item = mkEntry("Potion", 3, true, 2, 1); // maxStack = 2
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(item))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 100);

            inv.addItem(item.getItem());
            item.getItem().getComponent(ItemComponent.class).setCount(2); // already at max

            PurchaseResult r = manager.purchase(player, item, 1);
            assertFalse(r.ok());
            assertEquals(PurchaseError.LIMIT_REACHED, r.error());
            assertEquals(100, inv.getProcessor(), "Funds should not be deducted on failure");
        }
    }
}