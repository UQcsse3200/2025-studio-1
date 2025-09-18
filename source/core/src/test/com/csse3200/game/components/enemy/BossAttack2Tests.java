package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests cover behavier of EnemyMudRingSprayComponent
 */
public class BossAttack2Tests {
    private MockedStatic<ServiceLocator> serviceLocatorStatic;
    private GameTime time;
    private ResourceService resources;
    private EntityService entities;

    @BeforeEach
    void setUp() {
        // Minimal Gdx.app so postRunnable works
        Application app = mock(Application.class);
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        })
                .when(app).postRunnable(any(Runnable.class));
        Gdx.app = app;

        // Minimal ServiceLocator stubs
        serviceLocatorStatic = mockStatic(ServiceLocator.class);
        time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(0.016f);

        resources = mock(ResourceService.class);
        entities = mock(EntityService.class);

        when(ServiceLocator.getTimeSource()).thenReturn(time);
        when(ServiceLocator.getResourceService()).thenReturn(resources);
        when(ServiceLocator.getEntityService()).thenReturn(entities);
    }

    @AfterEach
    void tearDown() {
        serviceLocatorStatic.close();
    }

    private static void setPrivateFloat(Object obj, String field, float value) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.setFloat(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static float getPrivateFloat(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getFloat(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int getPrivateInt(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getInt(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void BossAttack2Tests_noEntity_noSpawn() {
        // Component not attached to any entity -> update should early-return
        EnemyMudRingSprayComponent spray = new EnemyMudRingSprayComponent(
                1f, 6, 5f, 1.5f);

        spray.update();

        // Nothing registered
        verify(entities, never()).register(any());
    }

    @Test
    void BossAttack2Tests_onCooldown_noSpawn() {
        Entity owner = new Entity();
        EnemyMudRingSprayComponent spray = new EnemyMudRingSprayComponent(
                1f, 6, 5f, 1.5f);
        owner.addComponent(spray);

        // Put the component on cooldown (timer > 0) so update returns early
        setPrivateFloat(spray, "timer", 0.5f);
        when(time.getDeltaTime()).thenReturn(0.016f);

        spray.update();

        verify(entities, never()).register(any());
        // Timer should have decreased but still > 0
        float t = getPrivateFloat(spray, "timer");
        Assertions.assertTrue(t > 0f && t < 0.5f);
    }

    @Test
    void BossAttack2Tests_countIsClampedToAtLeastOne() {
        // Create with count = 0; ctor clamps to 1
        EnemyMudRingSprayComponent sprayZero = new EnemyMudRingSprayComponent(
                1f, 0, 5f, 1.5f);
        assertEquals(1, getPrivateInt(sprayZero, "count"));

        // count stays as given for positive values
        EnemyMudRingSprayComponent sprayFive = new EnemyMudRingSprayComponent(
                1f, 5, 5f, 1.5f);
        assertEquals(5, getPrivateInt(sprayFive, "count"));
    }
}
