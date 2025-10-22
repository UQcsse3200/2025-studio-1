package com.csse3200.game.lighting;


import box2dLight.ConeLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ConeLightComponentTest {

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void createsConeLight() {
        Entity e = new Entity();
        ConeLightComponent cone = new ConeLightComponent(
                new Color(0f,1f,0f,1f),
                true,
                new Vector2(1f,0f)
        );
        e.addComponent(cone);

        ConeLight mockLight = mock(ConeLight.class);
        setPrivateLight(cone, "light", mockLight);

        assertNotNull(cone);
    }

    @Test
    void disposesConeLight() {
        Entity e = new Entity();
        ConeLightComponent cone = new ConeLightComponent(
                new Color(1f, 0f, 0f, 0f),
                true,
                new Vector2(0f, 0f)
        );
        e.addComponent(cone);

        ConeLight mockLight = mock(ConeLight.class);
        setPrivateLight(cone, "light", mockLight);

        assertDoesNotThrow(cone::dispose);
        verify(mockLight, times(1)).remove();
    }

    private void setPrivateLight(Object target, String field, Object light) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, light);
        } catch (Exception e) {throw new RuntimeException(e);}
    }
}
