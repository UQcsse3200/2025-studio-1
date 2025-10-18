package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

/**
 * Unit tests for {@link BlackholeAttackComponent}.
 *
 * This test class verifies all logical branches without relying on a TimeSource type.
 * It covers:
 *  - Early return when entity == null
 *  - Early return when target == null
 *  - Pull effect when player is within radius
 *  - No pull when player is outside the radius
 *  - Disposal after lifetime expiration (via Gdx.app.postRunnable) and ensuring it only happens once
 */
public class BlackholeAttackComponentTest {

    /**
     * Helper: Attach the component to a mocked Entity using reflection,
     * so we don’t require a full ECS environment.
     */
    private static void attachToEntity(Object component, Entity entity) {
        try {
            // Preferred method: use setEntity(Entity)
            Method m = component.getClass().getSuperclass().getDeclaredMethod("setEntity", Entity.class);
            m.setAccessible(true);
            m.invoke(component, entity);
        } catch (Exception ignored1) {
            try {
                // Fallback: directly assign to the protected 'entity' field
                Field f = component.getClass().getSuperclass().getDeclaredField("entity");
                f.setAccessible(true);
                f.set(component, entity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to attach component to entity by reflection.", e);
            }
        }
    }

    @BeforeEach
    void setupGdxApp() {
        // Mock Gdx.app so that postRunnable can be safely intercepted in tests
        Application app = mock(Application.class);
        Gdx.app = app;
    }

    /** Verifies that update() exits early if entity == null (component not attached). */
    @Test
    void update_returnsEarly_whenEntityIsNull() {
        Entity target = mock(Entity.class);
        BlackholeAttackComponent comp = new BlackholeAttackComponent(target, 5f, 10f);

        assertDoesNotThrow(comp::update);
        verifyNoInteractions(target);
    }

    /** Verifies that update() exits early if target == null (no player target). */
    @Test
    void update_returnsEarly_whenTargetIsNull() {
        Entity host = mock(Entity.class);
        BlackholeAttackComponent comp = new BlackholeAttackComponent(null, 5f, 10f);
        attachToEntity(comp, host);

        assertDoesNotThrow(comp::update);
        verify(host, never()).dispose();
    }

    /** Verifies the pull effect occurs when the player is within the blackhole’s radius. */
    @Test
    void update_appliesPull_whenWithinRadius() {
        // Using deep stubs allows chaining: ServiceLocator.getTimeSource().getDeltaTime()
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(0.016f);

            Entity host = mock(Entity.class);
            Entity player = mock(Entity.class);

            // Blackhole center (10,10)
            when(host.getCenterPosition()).thenReturn(new Vector2(10f, 10f));
            // Player center (12,10) — within radius 5
            when(player.getCenterPosition()).thenReturn(new Vector2(12f, 10f));
            when(player.getPosition()).thenReturn(new Vector2(12f, 10f));

            BlackholeAttackComponent comp = new BlackholeAttackComponent(player, 5f, 10f);
            attachToEntity(comp, host);

            comp.update();

            // Expected pullFactor = 0.07 -> new position ≈ (11.86, 10.0)
            verify(player).setPosition(floatThat(x -> Math.abs(x - 11.86f) < 1e-3f),
                    floatThat(y -> Math.abs(y - 10f) < 1e-3f));
            verify(host, never()).dispose();
        }
    }

    /** Verifies that no pull occurs if the player is outside the radius. */
    @Test
    void update_doesNotPull_whenOutsideRadius() {
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(0.016f);

            Entity host = mock(Entity.class);
            Entity player = mock(Entity.class);

            // Very far apart (> radius 5)
            when(host.getCenterPosition()).thenReturn(new Vector2(0f, 0f));
            when(player.getCenterPosition()).thenReturn(new Vector2(100f, 0f));
            when(player.getPosition()).thenReturn(new Vector2(100f, 0f));

            BlackholeAttackComponent comp = new BlackholeAttackComponent(player, 5f, 10f);
            attachToEntity(comp, host);

            comp.update();

            verify(player, never()).setPosition(anyFloat(), anyFloat());
            verify(host, never()).dispose();
        }
    }

    /**
     * Verifies that:
     *  - The component disposes the entity once the lifetime is exceeded.
     *  - The disposal happens only once, even after multiple updates.
     */
    @Test
    void update_disposesOnLifetimeExpiry_onlyOnce() {
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            // Three frames: 0.6 + 0.6 = 1.2 > lifeTime (1.0)
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime())
                    .thenReturn(0.6f, 0.6f, 0.6f);

            // Make postRunnable execute the Runnable immediately
            doAnswer(invocation -> {
                Runnable r = invocation.getArgument(0);
                r.run();
                return null;
            }).when(Gdx.app).postRunnable(any());

            Entity host = mock(Entity.class);
            Entity player = mock(Entity.class);

            when(host.getCenterPosition()).thenReturn(new Vector2(0f, 0f));
            when(player.getCenterPosition()).thenReturn(new Vector2(0f, 0f));
            when(player.getPosition()).thenReturn(new Vector2(0f, 0f));

            BlackholeAttackComponent comp = new BlackholeAttackComponent(player, 5f, 1.0f);
            attachToEntity(comp, host);

            // 1st frame: 0.6s elapsed, not expired yet
            comp.update();
            verify(host, never()).dispose();

            // 2nd frame: total 1.2s -> expired -> should dispose once
            comp.update();
            verify(Gdx.app, times(1)).postRunnable(any());
            verify(host, times(1)).dispose();

            // 3rd frame: already disposed -> should not dispose again
            comp.update();
            verify(Gdx.app, times(1)).postRunnable(any());
            verify(host, times(1)).dispose();
        }
    }
}

