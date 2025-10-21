package com.csse3200.game.lighting;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class DiscoBallLightComponentTest {

    private static void setPrivate(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    @DisplayName("updates light position/color/radius")
    void update_callsLightSetters() {
        GameTime time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(0.016f);
        ServiceLocator.registerTimeSource(time);

        Entity e = new Entity();
        e.setPosition(new Vector2(5f, 6f));

        DiscoBallLightComponent comp = new DiscoBallLightComponent(
                64, new Vector2(1f, 2f),
                new Color(1f, 0f, 0f, 0.7f), new Color(0f, 0f, 1f, 0.7f),
                10f, 4f, 1f, true);

        e.addComponent(comp);

        PointLight mockLight = mock(PointLight.class);
        setPrivate(comp, "light", mockLight);

        // Act
        comp.update();

        verify(mockLight, atLeastOnce()).setPosition(anyFloat(), anyFloat());
        verify(mockLight, atLeastOnce()).setColor(any(Color.class));
        verify(mockLight, atLeastOnce()).setDistance(anyFloat());
    }

    @Test
    @DisplayName("dispose is safe")
    void dispose_removesLight_once() {
        Entity e = new Entity();
        DiscoBallLightComponent comp = new DiscoBallLightComponent(
                32, new Vector2(0f, 0f),
                Color.WHITE, Color.BLACK, 8f, 2f, 1f, true);
        e.addComponent(comp);

        PointLight mockLight = mock(PointLight.class);
        setPrivate(comp, "light", mockLight);

        assertDoesNotThrow(comp::dispose);
        verify(mockLight, times(1)).remove();

        assertDoesNotThrow(comp::dispose);
        verifyNoMoreInteractions(mockLight);
    }
}