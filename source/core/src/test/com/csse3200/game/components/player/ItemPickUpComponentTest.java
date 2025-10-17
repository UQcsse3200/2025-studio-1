package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.factories.PowerupsFactory;
import com.csse3200.game.entities.factories.items.ArmourFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
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
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    private PlayerEquipComponent equip;

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
        pickup = spy(new ItemPickUpComponent(inventory));
        equip = new PlayerEquipComponent();
        ServiceLocator.registerEntityService(new EntityService());

        // Create a mock ResourceService
        ResourceService rs = mock(ResourceService.class);
        // Make getAsset to always return a mock Texture when asked for any texture path
        when(rs.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        // Register the mocked ResourceService and PhysicsService
        ServiceLocator.registerResourceService(rs);
        RenderService renderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(renderService);

        PhysicsService physicsService = mock(PhysicsService.class);
        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        when(physicsService.getPhysics()).thenReturn(physicsEngine);
        when(physicsEngine.createBody(any())).thenReturn(mock(Body.class));
        ServiceLocator.registerPhysicsService(physicsService);
        //ServiceLocator.registerPhysicsService(mock(PhysicsService.class));

        player = new Entity()
                .addComponent(inventory)
                .addComponent(equip)
                .addComponent(pickup);
        player.create();
    }

    @Nested
    @DisplayName("Pickup behaviour")
    class PickupBehaviour {

        @Test
        @DisplayName("Picking up a valid target item adds it to inventory and clears target")
        void pickUpAddsItemAndClearsTarget() {
            Entity dummyItem = new Entity().addComponent(new ItemComponent());

            try (MockedStatic<WeaponsFactory> wf = mockStatic(WeaponsFactory.class);
                 MockedStatic<ArmourFactory> af = mockStatic(ArmourFactory.class)) {
                // any weapon -> our stub
                wf.when(() -> WeaponsFactory.createWeapon(any())).thenReturn(dummyItem);
                // armour path not used here, but safe:
                af.when(() -> ArmourFactory.createArmour(any())).thenReturn(new Entity().addComponent(new ItemComponent()));

                var ic = new ItemComponent();
                ic.setTexture("images/pistol.png"); // must match a real Weapons config texture
                Entity worldItem = new Entity().addComponent(ic);
                worldItem.create();

                setPrivate(pickup, "targetItem", worldItem);

                player.getEvents().trigger("pick up");

                assertEquals(1, inventory.getSize(), "Item should be added to inventory");

                Entity stored = inventory.get(0);
                assertNotNull(stored);
                assertNotSame(worldItem, stored);
                assertNotNull(stored.getComponent(ItemComponent.class));
                assertNull(getPrivate(pickup, "targetItem"), "targetItem should be cleared after pickup");
            }       }

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

    @Nested
    @DisplayName("Create item from texture")
    class CreationBehaviour {
        ItemPickUpComponent component;

        @BeforeEach
        void setUp() {
            component = new ItemPickUpComponent(mock(InventoryComponent.class));
        }

        @Test
        void shouldCreateDagger() {
            String tex = "dagger.png";
            Entity dagger = component.createItemFromTexture(tex);
            assertEquals("dagger", dagger.getComponent(ItemComponent.class).getName());
            assertEquals(ItemTypes.MELEE, dagger.getComponent(ItemComponent.class).getType());
        }

        @Test
        void shouldCreatePistol() {
            String tex = "pistol.png";
            Entity pistol = component.createItemFromTexture(tex);
            assertEquals("pistol", pistol.getComponent(ItemComponent.class).getName());
            assertEquals(ItemTypes.RANGED, pistol.getComponent(ItemComponent.class).getType());
        }

        @Test
        void shouldCreateRifle() {
            String tex = "rifle.png";
            Entity rifle = component.createItemFromTexture(tex);
            assertEquals("rifle", rifle.getComponent(ItemComponent.class).getName());
            assertEquals(ItemTypes.RANGED, rifle.getComponent(ItemComponent.class).getType());
        }

        @Test
        void shouldCreateLightsaber() {
            String tex = "lightsaberSingle.png";
            Entity lightsaber = component.createItemFromTexture(tex);
            assertEquals("lightsaber", lightsaber.getComponent(ItemComponent.class).getName());
            assertEquals(ItemTypes.MELEE, lightsaber.getComponent(ItemComponent.class).getType());
        }

        @Test
        void shouldCreateRocketLauncher() {
            String tex = "rocketlauncher.png";
            Entity rocketlauncher = component.createItemFromTexture(tex);
            assertEquals("rocketlauncher", rocketlauncher.getComponent(ItemComponent.class).getName());
            assertEquals(ItemTypes.RANGED, rocketlauncher.getComponent(ItemComponent.class).getType());
        }

        @Test
        void shouldntCreate() {
            String tex = "random";
            Entity ran = component.createItemFromTexture(tex);
            assertNull(ran);
        }
    }

    @Nested
    @DisplayName("Collision events")
    class CollisionEvents {
        private Fixture me, other;
        private com.badlogic.gdx.physics.box2d.Body otherBody;

        @BeforeEach
        void collisionSetup() {
            me = mock(Fixture.class);
            other = mock(Fixture.class);
            otherBody = mock(com.badlogic.gdx.physics.box2d.Body.class);
            when(other.getBody()).thenReturn(otherBody);
        }

        @Test
        @DisplayName("collisionStart: sets target when other has pickupable ItemComponent")
        void collisionStartSetsTarget() {
            Entity worldItem = new Entity().addComponent(new ItemComponent());
            worldItem.create();

            BodyUserData bud = new BodyUserData();
            bud.entity = worldItem;
            when(otherBody.getUserData()).thenReturn(bud);
            // Sanity: target initially null
            assertNull(getPrivate(pickup, "targetItem"));
            player.getEvents().trigger("collisionStart", me, other);

            // Target now set
            assertSame(worldItem, getPrivate(pickup, "targetItem"));
        }

        @Test
        @DisplayName("collisionStart: ignores when userData missing or not BodyUserData")
        void collisionStartIgnoresNoUserData() {
            when(otherBody.getUserData()).thenReturn(null);
            player.getEvents().trigger("collisionStart", me, other);
            assertNull(getPrivate(pickup, "targetItem"));
        }

        @Test
        @DisplayName("collisionStart: ignores when other entity has no ItemComponent")
        void collisionStartIgnoresNonItem() {
            Entity worldNonItem = new Entity();
            worldNonItem.create();
            BodyUserData bud = new BodyUserData();
            bud.entity = worldNonItem;
            when(otherBody.getUserData()).thenReturn(bud);
            player.getEvents().trigger("collisionStart", me, other);
            assertNull(getPrivate(pickup, "targetItem"));
        }

        @Test
        @DisplayName("collisionEnd: clears target when moving away from same item")
        void collisionEndClearsTarget() {
            Entity worldItem = new Entity().addComponent(new ItemComponent());
            worldItem.create();
            setPrivate(pickup, "targetItem", worldItem);

            BodyUserData bud = new BodyUserData();
            bud.entity = worldItem;
            when(otherBody.getUserData()).thenReturn(bud);

            player.getEvents().trigger("collisionEnd", me, other);
            assertNull(getPrivate(pickup, "targetItem"));
        }

        @Test
        @DisplayName("collisionEnd: ignores when userData missing or different entity")
        void collisionEndIgnoresDifferent() {
            // Set some targetItem
            Entity target = new Entity().addComponent(new ItemComponent());
            target.create();
            setPrivate(pickup, "targetItem", target);

            // Different entity ends collision
            Entity different = new Entity().addComponent(new ItemComponent());
            different.create();

            BodyUserData bud = new BodyUserData();
            bud.entity = different;
            when(otherBody.getUserData()).thenReturn(bud);

            player.getEvents().trigger("collisionEnd", me, other);
            assertSame(target, getPrivate(pickup, "targetItem"), "Target should remain unchanged");

            // Also verify null userData path does not throw/clear
            when(otherBody.getUserData()).thenReturn(null);
            player.getEvents().trigger("collisionEnd", me, other);
            assertSame(target, getPrivate(pickup, "targetItem"));
        }
    }

    @Nested
    @DisplayName("Pickup request edge cases")
    class PickupRequestEdges {
        @Test
        @DisplayName("Pick up: no target -> does nothing")
        void pickupWithNoTargetNoop() {
            assertNull(getPrivate(pickup, "targetItem"));
            player.getEvents().trigger("pick up");
            assertEquals(0, inventory.getSize());
        }

        @Test
        @DisplayName("Pick up: item with missing texture -> ignored")
        void pickupWithMissingTextureIgnored() {
            Entity worldItem = new Entity().addComponent(new ItemComponent());
            worldItem.create();
            setPrivate(pickup, "targetItem", worldItem);

            player.getEvents().trigger("pick up");

            // No changes: still 0, target remains (still in range)
            assertEquals(0, inventory.getSize());
            assertSame(worldItem, getPrivate(pickup, "targetItem"));
        }

        @Test
        @DisplayName("Pick up: unknown texture -> ignored")
        void pickupWithUnknownTextureIgnored() {
            ItemComponent ic = new ItemComponent();
            ic.setTexture("images/not-a-real-item.png");
            Entity worldItem = new Entity().addComponent(ic);
            worldItem.create();
            setPrivate(pickup, "targetItem", worldItem);

            player.getEvents().trigger("pick up");

            // Not added; target unchanged
            assertEquals(0, inventory.getSize());
            assertSame(worldItem, getPrivate(pickup, "targetItem"));
        }
    }

    @Nested
    @DisplayName("Focus boundaries")
    class FocusBoundaries {
        @Test
        @DisplayName("Focus lower boundary 0 accepted; 5 rejected")
        void focusBounds() {
            player.getEvents().trigger("focus item", 0);
            assertEquals(0, (int) getPrivate(pickup, "focusedIndex"));
            player.getEvents().trigger("focus item", 5);
            assertEquals(-1, (int) getPrivate(pickup, "focusedIndex"));
        }
    }

    @Nested
    @DisplayName("Drop behaviour with GameArea and respawn factory")
    class DropWithGameArea {
        @Test
        @DisplayName("Drop: unknown texture path skips respawn but still clears focus")
        void dropUnknownTextureStillClearsFocus() {
            // Put an item with unknown texture into inventory
            ItemComponent ic = new ItemComponent();
            ic.setTexture("images/unknown.png");
            Entity e = new Entity().addComponent(ic);
            assertTrue(inventory.addItem(e));

            player.getEvents().trigger("focus item", 0);
            player.getEvents().trigger("drop focused");

            assertEquals(0, inventory.getSize());
            assertEquals(-1, (int) getPrivate(pickup, "focusedIndex"));
        }

        @Test
        @DisplayName("Drop: valid texture but no WorldPickUpFactory match -> safe no-op after removal")
        void dropValidButNoFactoryMatch() {
            ItemComponent ic = new ItemComponent();
            ic.setTexture("images/pistol.png");
            Entity e = new Entity().addComponent(ic);
            assertTrue(inventory.addItem(e));

            // Register a fake GameArea so spawnEntity would be callable if factory returns non-null
            com.csse3200.game.areas.GameArea area = mock(com.csse3200.game.areas.GameArea.class);
            ServiceLocator.registerGameArea(area);

            player.getEvents().trigger("focus item", 0);
            player.getEvents().trigger("drop focused");

            // Always removed
            assertEquals(0, inventory.getSize());
            assertEquals(-1, (int) getPrivate(pickup, "focusedIndex"));
        }
    }

    @Nested
    @DisplayName("Cheat: pickupAll")
    class PickupAllBehaviour {
        @Test
        @DisplayName("pickupAll: picks up ItemComponent entities until inventory full; leaves others")
        void pickupAllFillsInventoryAndStops() {
            EntityService es = ServiceLocator.getEntityService();

            // Add 10 item entities
            for (int i = 0; i < 10; i++) {
                es.register(new Entity().addComponent(new ItemComponent())); // pickupable by default
            }
            // Add a couple of non-item entities (ignored)
            es.register(new Entity());
            es.register(new Entity());

            // Run cheat
            player.getEvents().trigger("pickupAll");

            // Inventory should be full (5)
            assertEquals(5, inventory.getSize());
            assertTrue(inventory.isFull());
        }

        @Test
        @DisplayName("pickupAll: with empty world does not throw and picks none")
        void pickupAllEmptyWorldNoop() {
            // Reset to a fresh, isolated world
            ServiceLocator.registerEntityService(new EntityService());
            assertEquals(0, inventory.getSize());

            player.getEvents().trigger("pickupAll");
            assertEquals(0, inventory.getSize());
        }
    }

    @Nested
    @DisplayName("createItemFromTexture extras")
    class CreateFromTextureExtras {
        ItemPickUpComponent component;

        @BeforeEach
        void localSetup() {
            component = new ItemPickUpComponent(mock(InventoryComponent.class));
        }

        @Test
        @DisplayName("Creates rapid fire powerup when suffix matches")
        void createsRapidFirePowerup() {
            try (MockedStatic<PowerupsFactory> pf = mockStatic(PowerupsFactory.class)) {
                pf.when(PowerupsFactory::createRapidFire)
                        .thenReturn(new Entity().addComponent(new ItemComponent())); // no colliders

                Entity p = component.createItemFromTexture("rapidfirepowerup.png");
                assertNotNull(p);
                assertNotNull(p.getComponent(ItemComponent.class));
            }
        }

        @Test
        @DisplayName("Creates tree obstacle when suffix matches")
        void createsTreeObstacle() {
            try (MockedStatic<ObstacleFactory> of = mockStatic(ObstacleFactory.class)) {
                of.when(ObstacleFactory::createTree)
                        .thenReturn(new Entity()); // no physics

                Entity t = component.createItemFromTexture("tree.png");
                assertNotNull(t);
            }
        }
    }

}
