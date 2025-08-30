package com.csse3200.game.components.entity.item;

import com.csse3200.game.components.entity.EntityComponent;
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
            ItemComponent item = new ItemComponent(2);

            assertEquals(2, item.getCount());
        }

        @Test
        void testDefaultConstructor(){
            ItemComponent item = new ItemComponent();

            assertEquals(0, item.getCount());
        }
    }

    @Nested
    @DisplayName("Testing Getters and Setters")

    public static class GetterSetterTest{
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
    }

    @Nested
    @DisplayName("Testing Edge cases")

    public static class EdgeTest{

        private ItemComponent item;

        @BeforeEach
        void setup(){
            item = new ItemComponent();
        }

        @Test
        public void testNegativeCountEdgeCase(){}

        public void testOutOfBoundEdgeCase(){}

        public void testNullEdgeCase(){}
    }
}
