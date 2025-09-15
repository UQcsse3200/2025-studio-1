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

public class ShopManagerTest {

    private CatalogEntry mkEntry(String name, int price, boolean enabled, int maxStack, int bundleQty) {
        Entity itemEntity = new Entity();
        ItemComponent ic = new ItemComponent();
        ic.setName(name);
        ic.setTexture("test.png"); // dummy path
        itemEntity.addComponent(ic);
        itemEntity.create();
        return new CatalogEntry(itemEntity, price, enabled, maxStack, bundleQty);
    }

    private Entity mkPlayerWithProcessors(int processors) {
        Entity player = new Entity();
        InventoryComponent inv = new InventoryComponent(0); // start at 0
        player.addComponent(inv);
        player.create();
        inv.setProcessor(processors);
        return player;
    }

    private ShopManager mkManagerWithEntity(CatalogService catalog) {
        ShopManager manager = new ShopManager(catalog);
        Entity shopEntity = new Entity();
        shopEntity.addComponent(manager);
        shopEntity.create(); // attach so fail() can trigger events safely
        return manager;
    }

    private void fillInventory(InventoryComponent inv, int count) {
        for (int i = 0; i < count; i++) {
            Entity e = new Entity();
            ItemComponent ic = new ItemComponent();
            ic.setName("Item" + i);
            ic.setTexture("i" + i + ".png");
            e.addComponent(ic);
            e.create();
            assertTrue(inv.addItem(e));
        }
    }

    private void addSpecificItemToInventory(InventoryComponent inv, Entity item, int count) {
        ItemComponent ic = item.getComponent(ItemComponent.class);
        ic.setCount(count);
        if (ic.getTexture() == null) ic.setTexture("pre.png");
        assertTrue(inv.addItem(item));
    }

    @Nested
    @DisplayName("Objective: Success behaviour")
    class SuccessTests {

        @Test
        @DisplayName("Purchase succeeds: charges funds and adds item")
        void purchaseSucceeds_chargesFunds_andAddsItem() {
            CatalogEntry sword = mkEntry("Sword", 50, true, 10, 1);
            CatalogService catalog = new CatalogService(new ArrayList<>(List.of(sword)));
            ShopManager manager = mkManagerWithEntity(catalog);

            Entity player = mkPlayerWithProcessors(200);
            InventoryComponent inv = player.getComponent(InventoryComponent.class);

            PurchaseResult r = manager.purchase(player, sword, 1);

            assertTrue(r.ok());
            assertEquals(PurchaseError.NONE, r.error());
            assertEquals(150, inv.getProcessor()); // 200 - 50
            assertTrue(inv.getInventory().contains(sword.getItem()));
            assertEquals(1, sword.getItem().getComponent(ItemComponent.class).getCount());
            assertSame(sword, r.entry());
            assertEquals(1, r.qty());
        }

        @Test
        @DisplayName("Stacks when same entity is in inventory and within max stack")
        void stacksExistingItemWithinMaxStack() {
            CatalogEntry potion = mkEntry("Potion", 10, true, 5, 1);
            CatalogService catalog = new CatalogService(new ArrayList<>(List.of(potion)));
            ShopManager manager = mkManagerWithEntity(catalog);

            Entity player = mkPlayerWithProcessors(100);
            InventoryComponent inv = player.getComponent(InventoryComponent.class);

            // Pre-add the SAME entity as the catalog entry with count 2
            addSpecificItemToInventory(inv, potion.getItem(), 2);

            PurchaseResult r = manager.purchase(player, potion, 1);

            assertTrue(r.ok());
            assertEquals(PurchaseError.NONE, r.error());
            assertEquals(90, inv.getProcessor()); // 100 - 10
            assertTrue(inv.getInventory().contains(potion.getItem()));
            assertEquals(3, potion.getItem().getComponent(ItemComponent.class).getCount());
        }
    }

    @Nested
    @DisplayName("Objective: Failure behaviour")
    class FailureTests {

        @Test
        @DisplayName("Fails when insufficient funds")
        void purchaseFails_whenInsufficientFunds() {
            CatalogEntry sword = mkEntry("Sword", 50, true, 10, 1);
            CatalogService catalog = new CatalogService(new ArrayList<>(List.of(sword)));
            ShopManager manager = mkManagerWithEntity(catalog);

            Entity player = mkPlayerWithProcessors(10);
            InventoryComponent inv = player.getComponent(InventoryComponent.class);

            PurchaseResult r = manager.purchase(player, sword, 1);

            assertFalse(r.ok());
            assertEquals(PurchaseError.INSUFFICIENT_FUNDS, r.error());
            assertEquals(10, inv.getProcessor()); // unchanged
            assertFalse(inv.getInventory().contains(sword.getItem()));
        }

        @Test
        @DisplayName("Fails when item is disabled")
        void purchaseFails_whenDisabled() {
            CatalogEntry shield = mkEntry("Shield", 40, false, 10, 1);
            CatalogService catalog = new CatalogService(new ArrayList<>(List.of(shield)));
            ShopManager manager = mkManagerWithEntity(catalog);

            Entity player = mkPlayerWithProcessors(100);

            PurchaseResult r = manager.purchase(player, shield, 1);

            assertFalse(r.ok());
            assertEquals(PurchaseError.DISABLED, r.error());
        }

        @Test
        @DisplayName("Fails when item not found in catalog")
        void purchaseFails_whenNotFound() {
            CatalogEntry inCatalog = mkEntry("Bow", 30, true, 10, 1);
            CatalogEntry differentEntry = mkEntry("Bow", 30, true, 10, 1);
            CatalogService catalog = new CatalogService(new ArrayList<>(List.of(inCatalog)));
            ShopManager manager = mkManagerWithEntity(catalog);

            Entity player = mkPlayerWithProcessors(100);

            PurchaseResult r = manager.purchase(player, differentEntry, 1);

            assertFalse(r.ok());
            assertEquals(PurchaseError.NOT_FOUND, r.error());
        }

        @Test
        @DisplayName("Fails when inventory is full for a new item")
        void purchaseFails_whenInventoryFull_newItem() {
            CatalogEntry gem = mkEntry("Gem", 5, true, 10, 1);
            CatalogService catalog = new CatalogService(new ArrayList<>(List.of(gem)));
            ShopManager manager = mkManagerWithEntity(catalog);

            Entity player = mkPlayerWithProcessors(100);
            InventoryComponent inv = player.getComponent(InventoryComponent.class);

            // Fill the 5-slot inventory completely with different items
            fillInventory(inv, 5);
            assertTrue(inv.isFull());

            PurchaseResult r = manager.purchase(player, gem, 1);

            assertFalse(r.ok());
            assertEquals(PurchaseError.INVENTORY_FULL, r.error());
            // Funds unchanged and item not added
            assertEquals(100, inv.getProcessor());
            assertFalse(inv.getInventory().contains(gem.getItem()));
        }

        @Test
        @DisplayName("Fails when stacking would exceed max stack")
        void purchaseFails_whenExceedMaxStack() {
            CatalogEntry arrow = mkEntry("Arrow", 2, true, 3, 1); // maxStack = 3
            CatalogService catalog = new CatalogService(new ArrayList<>(List.of(arrow)));
            ShopManager manager = mkManagerWithEntity(catalog);

            Entity player = mkPlayerWithProcessors(50);
            InventoryComponent inv = player.getComponent(InventoryComponent.class);

            // Pre-add SAME entity with count = 3 (already at max)
            addSpecificItemToInventory(inv, arrow.getItem(), 3);

            PurchaseResult r = manager.purchase(player, arrow, 1);

            assertFalse(r.ok());
            assertEquals(PurchaseError.INVENTORY_FULL, r.error()); // mapped from addOrStack() failure
            // Funds unchanged, count unchanged
            assertEquals(50, inv.getProcessor());
            assertEquals(3, arrow.getItem().getComponent(ItemComponent.class).getCount());
        }

        @Test
        @DisplayName("Fails with UNEXPECTED when player has no InventoryComponent")
        void purchaseFails_whenNoInventoryComponent() {
            CatalogEntry orb = mkEntry("Orb", 10, true, 5, 1);
            CatalogService catalog = new CatalogService(new ArrayList<>(List.of(orb)));
            ShopManager manager = mkManagerWithEntity(catalog);

            Entity playerWithoutInv = new Entity(); // no InventoryComponent
            playerWithoutInv.create();

            PurchaseResult r = manager.purchase(playerWithoutInv, orb, 1);

            assertFalse(r.ok());
            assertEquals(PurchaseError.UNEXPECTED, r.error());
        }
    }
}