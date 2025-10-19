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
 class ItemComponentTest {
    /**
     * Test cases for verifying constructor behaviour and default values.
     */
    @Nested
    @DisplayName("Testing Constructors")
    class ConstructorTest {
        /**
         * Tests that a newly created {@link ItemComponent} correctly stores
         * a manually assigned texture and initializes with a count of 1.
         */
        @Test
        void testParameterisedConstructor() {
            ItemComponent itemComponent = new ItemComponent();
            itemComponent.setTexture("images/mud.png");

            assertEquals(1, itemComponent.getCount());
            assertEquals("images/mud.png", itemComponent.getTexture());
        }

        /**
         * Tests that the default constructor sets a count of 1
         * and leaves the texture field uninitialized (null).
         */
        @Test
        void testDefaultConstructor() {
            ItemComponent item = new ItemComponent();

            assertEquals(1, item.getCount());
            assertNull(item.getTexture());
        }
    }

    /**
     * Test cases for verifying getter and setter methods of {@link ItemComponent}.
     */
    @Nested
    @DisplayName("Testing Getters and Setters")
    class GetterSetterTest {
        private ItemComponent item;

        @BeforeEach
        void setup() {
            item = new ItemComponent();
        }


        /**
         * Ensures that {@link ItemComponent#setCount(int)} and
         * {@link ItemComponent#getCount()} correctly store and retrieve item count.
         */
        @Test
         void testCountGetterSetter() {
            item.setCount(2);
            assertEquals(2, item.getCount());
        }

        /**
         * Ensures that {@link ItemComponent#setTexture(String)} and
         * {@link ItemComponent#getTexture()} behave correctly.
         */
        @Test
         void testTextureGetterSetter() {
            item.setTexture("images/mud.png");
            assertEquals("images/mud.png", item.getTexture());
        }

        /**
         * Tests that {@link ItemComponent#setName(String)} correctly updates the item name.
         */
        @Test
        void shouldSetName() {
            item.setName("test");
            assertEquals("test", item.getName());
        }


        /**
         * Tests that {@link ItemComponent#setType(ItemTypes)} correctly updates the item type.
         */
        @Test
        void shouldSetType() {
            item.setType(ItemTypes.ARMOUR);
            assertEquals(ItemTypes.ARMOUR, item.getType());
        }
    }

    /**
     * Test cases for edge and boundary conditions related to
     * item count and texture.
     */
    @Nested
    @DisplayName("Testing Edge cases")
    class EdgeTest {

        private ItemComponent item;

        @BeforeEach
        void setup() {
            item = new ItemComponent();
        }

        /**
         * Tests negative count assignment behaviour.
         */
        @Test
         void testNegativeCountEdgeCase() {
            item.setCount(-1);
            assertFalse(item.getCount() > 0, "Count should be a positive integer");
        }

        /**
         * Tests the behaviour when item count exceeds an
         * assumed maximum threshold.
         */
        @Test
         void testOutOfBoundEdgeCountCase() {
            item.setCount(6);
            assertFalse(item.getCount() < 5, "Max item count can be 5.");
        }

        /**
         * Tests that zero count is handled correctly.
         */
        @Test
         void testNullEdgeCountCase() {
            item.setCount(0);
            assertEquals(0, item.getCount());
        }

        /**
         * Tests setting the texture path to null.
         */
        @Test
         void testNullTextureEdgeCase() {
            item.setTexture(null);
            assertNull(item.getTexture());
        }
    }

    /**
     * Test cases covering pickup behaviour and item
     * equipment offset calculations.
     */
    @Nested
    @DisplayName("EquipTests")
    class EquipTest {
        ItemComponent item;

        @BeforeEach
        void setUp() {
            item = new ItemComponent();
        }

        /**
         * Tests that pickup can be enabled.
         */
        @Test
        void shouldUnlockPickup() {
            assertTrue(item.isPickupable());
            item.setPickupable(true);
            assertTrue(item.isPickupable());
        }

        /**
         * Tests that pickup can be disabled.
         */
        @Test
        void shouldLockPickup() {
            item.setPickupable(false);
            assertFalse(item.isPickupable());
        }

        /**
         * Tests the correct equipment offset for a rifle.
         */
        @Test
        @Description("rifle")
        void shouldGetCorrectOffset1() {
            item.setName("rifle");
            assertEquals(new Vector2(0.8f, 0.15f), item.getEquipOffset());
        }

        /**
         * Tests the correct equipment offset for a lightsaber.
         */
        @Test
        @Description("lightsaber")
        void shouldGetCorrectOffset2() {
            item.setName("lightsaber");
            assertEquals(new Vector2(0.7f, -0.2f), item.getEquipOffset());
        }

        /**
         * Tests the correct equipment offset for a dagger.
         */
        @Test
        @Description("dagger")
        void shouldGetCorrectOffset3() {
            item.setName("dagger");
            assertEquals(new Vector2(1.0f, 0.3f), item.getEquipOffset());
        }

        /**
         * Tests the correct equipment offset for a pistol.
         */
        @Test
        @Description("pistol")
        void shouldGetCorrectOffset4() {
            item.setName("pistol");
            assertEquals(new Vector2(0.75f, -0.1f), item.getEquipOffset());
        }

        /**
         * Tests the correct equipment offset for a rocket launcher.
         */
        @Test
        @Description("rocket launcher")
        void shouldGetCorrectOffset5() {
            item.setName("rocketlauncher");
            assertEquals(new Vector2(0.75f, -0.1f), item.getEquipOffset());
        }

        /**
         * Tests the default equipment offset for unknown item names.
         */
        @Test
        @Description("unknown")
        void shouldGetCorrectOffset6() {
            item.setName("unknown");
            assertEquals(new Vector2(0.7f, 0.3f), item.getEquipOffset());
        }
    }

    /**
     * Tests that the {@link ItemComponent} constructor correctly defaults
     * the item type to {@link ItemTypes#NONE}.
     */
    @Test
    @DisplayName("Constructor should default type to NONE")
    void shouldDefaultTypeToNone() {
        ItemComponent item = new ItemComponent();
        assertEquals(ItemTypes.NONE, item.getType());
    }

    /**
     * Tests that setters can overwrite existing
     * field values properly.
     */
    @Test
    @DisplayName("Setters should correctly overwrite existing values")
    void shouldOverwriteExistingValues() {
        ItemComponent item = new ItemComponent();
        item.setName("Old");
        item.setName("New");
        assertEquals("New", item.getName());

        item.setCount(10);
        item.setCount(5);
        assertEquals(5, item.getCount());
    }

    /**
     * Tests that {@link ItemComponent#getEquipOffset()} returns
     * the default offset when the item name is null.
     */
    @Test
    @DisplayName("getEquipOffset should return default offset when name is null")
    void shouldReturnDefaultOffsetWhenNameIsNull() {
        ItemComponent item = new ItemComponent();
        item.setName(null);
        assertEquals(new Vector2(0.7f, 0.3f), item.getEquipOffset());
    }

    /**
     * Tests that {@link ItemComponent#getDescription()} correctly generates
     * a formatted description string using real entity data.
     * <p>
     * This version avoids using mocks and validates integration with a real
     * {@link Entity} instance.
     */
    @Test
    @DisplayName("getDescription should return formatted item description without mocking")
    void shouldReturnFormattedDescriptionWithoutMocking() {
        ItemComponent item = new ItemComponent();
        Entity entity = new Entity();
        item.setEntity(entity);

        item.setName("Health Potion");
        item.setType(ItemTypes.CONSUMABLE);
        item.setCount(3);

        String description = item.getDescription();

        assertTrue(description.contains("Item: Health Potion"));
        assertTrue(description.contains("Id: " + entity.getId())); // use real entity id
        assertTrue(description.contains("Type: CONSUMABLE"));
        assertTrue(description.contains("Count: 3"));
    }
}