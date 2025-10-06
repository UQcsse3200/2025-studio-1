package com.csse3200.game.components.shop;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.ComponentPriority;
import com.csse3200.game.components.attachments.BulletEnhancerComponent;
import com.csse3200.game.components.attachments.LaserComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ShopManager")
@ExtendWith(GameExtension.class)

public class ShopManagerTest {
    static HeadlessApplication app;
    private ShopManager shopManager;
    private Entity player;
    private InventoryComponent inventory;

    @BeforeEach
    void setup() {
        player = new Entity();
        inventory = mock(InventoryComponent.class);
        player.addComponent(inventory);

        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        Body physicsBody = mock(Body.class);
        when(physicsEngine.createBody(any())).thenReturn(physicsBody);
        PhysicsService physicsService = new PhysicsService(physicsEngine);
        ServiceLocator.registerPhysicsService(physicsService);

        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);
        Texture texture = mock(Texture.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);

        RenderService renderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(renderService);
    }

    private Entity mkItemEntity(String name) {
        Entity e = new Entity();
        ItemComponent ic = new ItemComponent();
        ic.setName(name);
        ic.setTexture("x.png");
        e.addComponent(ic);
        if (name.equals("laser")) {
            LaserComponent laserComponent = mock(LaserComponent.class);
            when(laserComponent.getPrio()).thenReturn(ComponentPriority.MEDIUM);
            e.addComponent(laserComponent);
        } else if (name.equals("bullet")) {
            BulletEnhancerComponent comp = mock(BulletEnhancerComponent.class);
            when(comp.getPrio()).thenReturn(ComponentPriority.MEDIUM);
            e.addComponent(comp);
        }
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

        @Test
        @DisplayName("Purchase Succeeds: Buying bullet attachment for ranged weapon")
        void buyBulletOnRifleWeapon() {
            CatalogEntry bullet = mkEntry("bullet", 5, true, 1, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(bullet))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 100);
            Entity weapon = WeaponsFactory.createWeaponWithAttachment(Weapons.RIFLE, false, false);
            inv.addItem(weapon);
            inv.setEquippedSlot(0);
            assertEquals(inv.getCurrSlot(), weapon);

            //Check if the purchase was ok and the player has the attachment
            PurchaseResult r = manager.purchase(player, bullet, 1);
            assertTrue(inv.getCurrSlot().hasComponent(BulletEnhancerComponent.class));
            assertTrue(r.ok());
            assertEquals(PurchaseError.NONE, r.error());
            assertEquals(95, inv.getProcessor()); // 100 - 5
        }

        @Test
        @DisplayName("Purchase Succeeds: Buying bullet attachment for ranged weapon")
        void buyBulletOnPistolWeapon() {
            CatalogEntry bullet = mkEntry("bullet", 5, true, 1, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(bullet))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 100);
            Entity weapon = WeaponsFactory.createWeaponWithAttachment(Weapons.PISTOL, false, false);
            inv.addItem(weapon);
            inv.setEquippedSlot(0);
            assertEquals(inv.getCurrSlot(), weapon);

            //Check if the purchase was ok and the player has the attachment
            PurchaseResult r = manager.purchase(player, bullet, 1);
            assertTrue(inv.getCurrSlot().hasComponent(BulletEnhancerComponent.class));
            assertTrue(r.ok());
            assertEquals(PurchaseError.NONE, r.error());
            assertEquals(95, inv.getProcessor()); // 100 - 5
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

        @Test
        @DisplayName("Fails when not a ranged weapon")
        void failsWhenNotRangedWeapon() {
            CatalogEntry bullet = mkEntry("bullet", 5, true, 1, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(bullet))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 100);
            Entity weapon = WeaponsFactory.createWeapon(Weapons.DAGGER);
            inv.addItem(weapon);
            inv.setEquippedSlot(0);
            assertEquals(inv.getCurrSlot(), weapon);

            PurchaseResult r = manager.purchase(player, bullet, 1);
            assertFalse(inv.getCurrSlot().hasComponent(BulletEnhancerComponent.class));
            assertFalse(r.ok());
            assertEquals(PurchaseError.INVALID_WEAPON, r.error());
            assertEquals(100, inv.getProcessor());
        }

        @Test
        @DisplayName("Fails when not holding anything")
        void failsWhenNotHolding() {
            CatalogEntry bullet = mkEntry("bullet", 5, true, 1, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(bullet))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 100);

            PurchaseResult r = manager.purchase(player, bullet, 1);
            assertFalse(r.ok());
            assertEquals(PurchaseError.INVALID_WEAPON, r.error());
            assertEquals(100, inv.getProcessor());
        }

        @Test
        @DisplayName("Fails when gun already has attachment")
        void failsWhenDuplicateAttachment() {
            CatalogEntry bullet = mkEntry("bullet", 5, true, 1, 1);
            ShopManager manager = mkManager(new CatalogService(new ArrayList<>(List.of(bullet))));
            Entity player = new Entity();
            InventoryComponent inv = attachInventory(player, 100);
            Entity weapon = WeaponsFactory.createWeaponWithAttachment(Weapons.RIFLE, false, true);
            inv.addItem(weapon);
            inv.setEquippedSlot(0);
            assertEquals(inv.getCurrSlot(), weapon);
            assertTrue(weapon.hasComponent(BulletEnhancerComponent.class));

            PurchaseResult r = manager.purchase(player, bullet, 1);
            assertFalse(r.ok());
            assertEquals(PurchaseError.ALREADY_HAVE_BULLET, r.error());
            assertEquals(100, inv.getProcessor());
        }

    }
}