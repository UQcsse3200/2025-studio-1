package com.csse3200.game.components.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class ItemComponentTest {

    @BeforeEach
    void registerResourceService() {
        PhysicsService physicsService = mock(PhysicsService.class);
        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        Body body = mock(Body.class);

        when(physicsService.getPhysics()).thenReturn(physicsEngine);
        when(physicsEngine.createBody(any())).thenReturn(body);

        ServiceLocator.registerPhysicsService(physicsService);

        ResourceService resourceService = mock(ResourceService.class);
        Texture texture = mock(Texture.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
        ServiceLocator.registerResourceService(resourceService);
    }


    @Nested
    @DisplayName("Testing Constructors")
    class ConstructorTest {
        @Test
        void testParameterisedConstructor() {
            Entity item = ItemFactory.createItem("images/mud.png");
            ItemComponent itemComponent = item.getComponent(ItemComponent.class);

            assertEquals(1, itemComponent.getCount());
            assertEquals("images/mud.png", itemComponent.getTexture());
        }

        @Test
        void testDefaultConstructor() {
            ItemComponent item = new ItemComponent();

            assertEquals(1, item.getCount());
            assertNull(item.getTexture());
        }
    }

    @Nested
    @DisplayName("Testing Getters and Setters")
    class GetterSetterTest {
        private ItemComponent item;

        @BeforeEach
        void setup() {
            item = new ItemComponent();
        }

        @Test
        public void testCountGetterSetter() {
            item.setCount(2);
            assertEquals(2, item.getCount());
        }

        @Test
        public void testTextureGetterSetter() {
            item.setTexture("images/mud.png");
            assertEquals("images/mud.png", item.getTexture());
        }

        @Test
        void shouldSetName() {
            item.setName("test");
            assertEquals("test", item.getName());
        }

        @Test
        void shouldSetType() {
            item.setType(ItemTypes.ARMOUR);
            assertEquals(ItemTypes.ARMOUR, item.getType());
        }
    }

    @Nested
    @DisplayName("Testing Edge cases")
    class EdgeTest {

        private ItemComponent item;

        @BeforeEach
        void setup() {
            item = new ItemComponent();
        }

        @Test
        public void testNegativeCountEdgeCase() {
            item.setCount(-1);
            assertFalse(item.getCount() > 0, "Count should be a positive integer");
        }

        @Test
        public void testOutOfBoundEdgeCountCase() {
            item.setCount(6);
            assertFalse(item.getCount() < 5, "Max item count can be 5.");
        }

        @Test
        public void testNullEdgeCountCase() {
            item.setCount(0);
            assertEquals(0, item.getCount());
        }

        @Test
        public void testNullTextureEdgeCase() {
            item.setTexture(null);
            assertNull(item.getTexture());
        }
    }

    @Nested
    @DisplayName("EuqipTests")
    class EquipTest {
        ItemComponent item;

        @BeforeEach
        void setUp() {
            item = new ItemComponent();
        }

        @Test
        void shouldUnlockPickup() {
            assertTrue(item.isPickupable());
            item.setPickupable(true);
            assertTrue(item.isPickupable());
        }

        @Test
        void shouldLockPickup() {
            item.setPickupable(false);
            assertFalse(item.isPickupable());
        }

        @Test
        @Description("rifle")
        void shouldGetCorrectOffset1() {
            item.setName("rifle");
            assertEquals(new Vector2(0.8f, 0.15f), item.getEquipOffset());
        }

        @Test
        @Description("lightsaber")
        void shouldGetCorrectOffset2() {
            item.setName("lightsaber");
            assertEquals(new Vector2(0.7f, -0.2f), item.getEquipOffset());
        }

        @Test
        @Description("dagger")
        void shouldGetCorrectOffset3() {
            item.setName("dagger");
            assertEquals(new Vector2(1.0f, 0.3f), item.getEquipOffset());
        }

        @Test
        @Description("pistol")
        void shouldGetCorrectOffset4() {
            item.setName("pistol");
            assertEquals(new Vector2(0.75f, -0.1f), item.getEquipOffset());
        }

        @Test
        @Description("rocket launcher")
        void shouldGetCorrectOffset5() {
            item.setName("rocketlauncher");
            assertEquals(new Vector2(0.75f, -0.1f), item.getEquipOffset());
        }

        @Test
        @Description("unknown")
        void shouldGetCorrectOffset6() {
            item.setName("unknown");
            assertEquals(new Vector2(0.7f, 0.3f), item.getEquipOffset());
        }
    }
}
