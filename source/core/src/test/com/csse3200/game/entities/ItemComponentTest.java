package com.csse3200.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.ItemComponent;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;

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
    class ConstructorTest{
        @Test
        void testParameterisedConstructor(){
            Entity item = ItemFactory.createItem("images/mud.png");
            ItemComponent itemComponent = item.getComponent(ItemComponent.class);

            assertEquals(1, itemComponent.getCount());
            assertEquals("images/mud.png", itemComponent.getTexture());
        }

        @Test
        void testDefaultConstructor(){
            ItemComponent item = new ItemComponent();

            assertEquals(1, item.getCount());
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
