package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ItemFactoryTest {

    @BeforeEach
    void registerServices() {
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ResourceService rs = mock(ResourceService.class);
        when(rs.getAsset("images/heart.png", Texture.class)).thenReturn(mock(Texture.class));
        ServiceLocator.registerResourceService(rs);
        ServiceLocator.registerEntityService(new EntityService());
    }

    @Test
    void createItem_buildsExpectedEntity() {
        Entity item = ItemFactory.createItem();
        assertNotNull(item.getComponent(TextureRenderComponent.class));
        assertNotNull(item.getComponent(PhysicsComponent.class));
        assertNotNull(item.getComponent(ColliderComponent.class));
        assertNotNull(item.getComponent(HitboxComponent.class));
        assertNotNull(item.getComponent(ItemComponent.class));

        PhysicsComponent phys = item.getComponent(PhysicsComponent.class);
        assertNotNull(phys.getBody(), "Physics body should exist");
        assertEquals(BodyType.StaticBody, phys.getBody().getType());

        assertEquals(PhysicsLayer.NONE, item.getComponent(ColliderComponent.class).getLayer());

        assertEquals("images/heart.png", item.getComponent(ItemComponent.class).getTexture());

        // Body userData points back to the entity
        assertSame(item, phys.getBody().getUserData());
    }

    @Test
    void constructor_isPrivateAndThrows() throws Exception {
        Constructor<ItemFactory> ctor = ItemFactory.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            try {
                ctor.newInstance();
            } catch (InvocationTargetException ite) {
                throw ite.getCause(); // unwrap the cause
            }
        });
        assertEquals("Instantiating static util class", ex.getMessage());
    }
}