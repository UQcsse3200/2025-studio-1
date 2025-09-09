package com.csse3200.game.entities;

import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class ItemComponentTest {
    @Nested
    @DisplayName("Testing Constructors")

    class ConstructorTest{
        @Test
        void testParameterisedConstructor(){
            Entity item = ItemFactory.createItem("images/mud.png");
            ItemComponent itemComponent = item.getComponent(ItemComponent.class);

            assertEquals(2, itemComponent.getCount());
            assertEquals("images/mud.png", itemComponent.getTexture());
        }

        @Test
        void testDefaultConstructor(){
            ItemComponent item = new ItemComponent();

            assertEquals(0, item.getCount());
            assertNull(item.getTexture());
        }
    }

    @Nested
    @DisplayName("Testing Getters and Setters")
    class GetterSetterTest{
        private ItemComponent item;

        @BeforeEach
        void setup(){
            item = new ItemComponent();
        }

        @Test
        public void testCountGetterSetter(){
            item.setCount(2);
            assertEquals(2, item.getCount());
        }

        @Test
        public void testTextureGetterSetter(){
            item.setTexture("images/mud.png");
        }
    }

    @Nested
    @DisplayName("Testing Edge cases")
    class EdgeTest{

        private ItemComponent item;

        @BeforeEach
        void setup(){
            item = new ItemComponent();
        }

        @Test
        public void testNegativeCountEdgeCase(){
            item.setCount(-1);
            assertFalse(item.getCount() > 0, "Count should be a positive integer");
        }

        @Test
        public void testOutOfBoundEdgeCountCase(){
            item.setCount(6);
            assertFalse(item.getCount() < 5, "Max item count can be 5.");
        }

        @Test
        public void testNullEdgeCountCase(){
            item.setCount(0);
            assertEquals(0, item.getCount());
        }

        @Test
        public void testNullTextureEdgeCase(){
            item.setTexture(null);
            assertNull(item.getTexture());
        }
    }
}
