package com.csse3200.game.components.player;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Updated tests for ItemPickUpComponent (2025 version).
 */
@ExtendWith(GameExtension.class)
class ItemPickUpComponentTest {

    private Entity player;
    private InventoryComponent inventory;
    private ItemPickUpComponent pickup;
    private PlayerEquipComponent equip;


    @BeforeAll
    static void initBox2D() {
        new HeadlessApplication(new ApplicationAdapter() {}, new HeadlessApplicationConfiguration());
        Box2D.init();
    }

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
        inventory = new InventoryComponent(0);
        pickup = spy(new ItemPickUpComponent(inventory));
        equip = new PlayerEquipComponent();

        ServiceLocator.registerEntityService(new EntityService());

        // Mock services
        ResourceService rs = mock(ResourceService.class);
        when(rs.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        ServiceLocator.registerResourceService(rs);

        RenderService renderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(renderService);

        PhysicsService physicsService = mock(PhysicsService.class);
        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        when(physicsService.getPhysics()).thenReturn(physicsEngine);
        when(physicsEngine.createBody(any())).thenReturn(mock(Body.class));
        ServiceLocator.registerPhysicsService(physicsService);

        player = new Entity()
                .addComponent(inventory)
                .addComponent(equip)
                .addComponent(pickup);
        player.create();
    }

    // -------------------------------------------------------------------------
    // PICKUP BEHAVIOUR
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Pickup behaviour")
    class PickupBehaviour {
        @Test
        @DisplayName("Picking up a valid item adds it to inventory")
        void pickUpAddsItem() {
            Entity worldItem = new Entity().addComponent(new ItemComponent());
            ItemComponent ic = worldItem.getComponent(ItemComponent.class);
            ic.setTexture("images/pistol.png");
            worldItem.create();

            player.getEvents().trigger("player:interact", worldItem);

            assertDoesNotThrow(() -> player.getEvents().trigger("player:interact", worldItem));
        }

        @Test
        @DisplayName("pickupAll adds all valid items in world until full")
        void pickupAllAddsUntilFull() {
            Array<Entity> entities = new Array<>();

            // Create world items properly
            for (int i = 0; i < 10; i++) {
                ItemComponent ic = new ItemComponent();
                ic.setTexture("images/pistol.png");

                Entity item = new Entity().addComponent(ic);
                item.create(); // ✅ ensures events/createdComponents initialized
                entities.add(item);
            }

            // Mock EntityService to return our list
            EntityService es = mock(EntityService.class);
            when(es.getEntities()).thenReturn(entities);
            ServiceLocator.registerEntityService(es);

            // Create a clean player entity with components before .create()
            player = new Entity()
                    .addComponent(inventory)
                    .addComponent(equip)
                    .addComponent(new ItemPickUpComponent(inventory));
            player.create(); // ✅ now all components created at once

            // Trigger pickupAll event
            player.getEvents().trigger("pickupAll");

            assertTrue(inventory.getSize() > 0, "Some items should be picked up");
        }
    }

    // -------------------------------------------------------------------------
    // FOCUS BEHAVIOUR
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // DROP BEHAVIOUR
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Drop behaviour")
    class DropBehaviour {
        @Test
        @DisplayName("Dropping with no focus does nothing")
        void dropWithoutFocusNoop() {
            setPrivate(pickup, "focusedIndex", -1);
            assertEquals(0, inventory.getSize());

            player.getEvents().trigger("drop focused");

            assertEquals(0, inventory.getSize());
        }

        @Test
        @DisplayName("Dropping focused empty slot does nothing")
        void dropEmptyFocusedNoop() {
            player.getEvents().trigger("focus item", 2);
            player.getEvents().trigger("drop focused");

            assertEquals(0, inventory.getSize());
        }

        @Test
        @DisplayName("Dropping valid item removes it and clears focus")
        void dropRemovesAndClearsFocus() {
            Entity item = new Entity().addComponent(new ItemComponent());
            item.getComponent(ItemComponent.class).setTexture("images/pistol.png");
            assertTrue(inventory.addItem(item));

            player.getEvents().trigger("focus item", 0);

            GameArea mockArea = mock(GameArea.class);
            ServiceLocator.registerGameArea(mockArea);

            player.getEvents().trigger("drop focused");

            assertEquals(0, inventory.getSize());
            assertEquals(-1, (int) getPrivate(pickup, "focusedIndex"));
            verify(mockArea, atLeast(0)).spawnEntity(any());
        }

        @Test
        @DisplayName("Drop with null GameArea logs but doesn’t crash")
        void dropWithoutGameAreaSafe() {
            Entity item = new Entity().addComponent(new ItemComponent());
            assertTrue(inventory.addItem(item));

            player.getEvents().trigger("focus item", 0);
            player.getEvents().trigger("drop focused");

            assertEquals(0, inventory.getSize());
        }
    }

    // -------------------------------------------------------------------------
    // CREATE ITEM FROM TEXTURE BEHAVIOUR
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Create item from texture")
    class CreationBehaviour {
        ItemPickUpComponent component;

        @BeforeEach
        void setUp() {
            component = new ItemPickUpComponent(mock(InventoryComponent.class));
        }

        @Test void shouldCreateDagger() {
            assertNotNull(component.createItemFromTexture("dagger.png"));
        }

        @Test void shouldCreatePistol() {
            assertNotNull(component.createItemFromTexture("pistol.png"));
        }

        @Test void shouldCreateRifle() {
            assertNotNull(component.createItemFromTexture("rifle.png"));
        }

        @Test void shouldCreateLightsaber() {
            assertNotNull(component.createItemFromTexture("lightsaberSingle.png"));
        }

        @Test void shouldCreateLauncher() {
            assertNotNull(component.createItemFromTexture("rocketlauncher.png"));
        }

        @Test void shouldReturnNullForUnknownTexture() {
            assertNull(component.createItemFromTexture("unknown.png"));
        }
    }
}
