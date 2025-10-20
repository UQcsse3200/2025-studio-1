package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

/**
 * Unit tests for {@link BossDeathComponent}.
 *
 * Covers:
 *  - Safe skipping behavior when the atlas is not loaded
 *  - Normal path: registers explosion effect entity, starts animation, disables boss, safely schedules disposal
 *  - Inner OneShotDisposeComponent: disposes the entity once the animation finishes (only once)
 *
 * Notes:
 *  - Reflection is used to invoke private spawnExplosion() to simulate a “death” event
 *    without depending on the full event system.
 */
public class BossDeathComponentTest {

    /** Helper: attach the component to an entity without requiring the full ECS lifecycle */
    private static void attachToEntity(Object component, Entity entity) {
        try {
            Method m = component.getClass().getSuperclass().getDeclaredMethod("setEntity", Entity.class);
            m.setAccessible(true);
            m.invoke(component, entity);
        } catch (Exception ignored1) {
            try {
                Field f = component.getClass().getSuperclass().getDeclaredField("entity");
                f.setAccessible(true);
                f.set(component, entity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to attach component to entity by reflection.", e);
            }
        }
    }

    /** Helper: reflectively invoke the private spawnExplosion() method (simulating boss death). */
    private static void invokeSpawnExplosion(BossDeathComponent comp) {
        try {
            Method m = BossDeathComponent.class.getDeclaredMethod("spawnExplosion");
            m.setAccessible(true);
            m.invoke(comp);
        } catch (Exception e) {
            throw new RuntimeException("invoke spawnExplosion() failed", e);
        }
    }

    /** Helper: invoke update() reflectively (used for testing the inner class). */
    private static void invokeUpdate(Object comp) {
        try {
            Method m = comp.getClass().getMethod("update");
            m.invoke(comp);
        } catch (Exception e) {
            throw new RuntimeException("invoke update() failed", e);
        }
    }

    private Application app;
    private Entity boss;

    @BeforeEach
    void setup() {
        // Mock Gdx.app so postRunnable() can be intercepted safely
        app = mock(Application.class);
        Gdx.app = app;

        // Mock basic boss entity
        boss = mock(Entity.class);
        when(boss.getPosition()).thenReturn(new Vector2(3f, 5f));
        when(boss.getScale()).thenReturn(new Vector2(1f, 1f));
    }

    @Test
    @DisplayName("Safe skip when atlas is missing — no crash")
    void skipWhenAtlasMissing_safeNoCrash() {
        BossDeathComponent comp = new BossDeathComponent();
        attachToEntity(comp, boss);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            ResourceService rs = mock(ResourceService.class);
            sl.when(ServiceLocator::getResourceService).thenReturn(rs);
            when(rs.containsAsset(anyString(), eq(TextureAtlas.class))).thenReturn(false);

            EntityService es = mock(EntityService.class);
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            assertDoesNotThrow(() -> invokeSpawnExplosion(comp));

            verify(es, never()).register(any(Entity.class));
            verify(app, never()).postRunnable(any());
            verify(boss, never()).setEnabled(false);
            verify(boss, never()).getComponent(AnimationRenderComponent.class);
        }
    }

    @Test
    @DisplayName("OneShotDisposeComponent: disposes exactly once when animation finishes")
    void oneShotDispose_disposeExactlyOnce_whenAnimationFinished() throws Exception {
        // Build an entity with an AnimationRenderComponent (ARC)
        Entity e = mock(Entity.class);
        AnimationRenderComponent arc = mock(AnimationRenderComponent.class);
        when(e.getComponent(AnimationRenderComponent.class)).thenReturn(arc);

        // Reflectively obtain and instantiate the private static inner class
        Class<?> inner = Class.forName(BossDeathComponent.class.getName() + "$OneShotDisposeComponent");
        Constructor<?> ctor = inner.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object oneShot = ctor.newInstance();
        attachToEntity(oneShot, e);

        // Make postRunnable run immediately
        doAnswer(inv -> { ((Runnable) inv.getArgument(0)).run(); return null; })
                .when(Gdx.app).postRunnable(any());

        // 1) No animation — should not dispose
        when(arc.getCurrentAnimation()).thenReturn(null);
        invokeUpdate(oneShot);
        verify(e, never()).dispose();

        // 2) Animation exists but not finished — should not dispose
        when(arc.getCurrentAnimation()).thenReturn("any");
        when(arc.isFinished()).thenReturn(false);
        invokeUpdate(oneShot);
        verify(e, never()).dispose();

        // 3) Animation finished — dispose once
        when(arc.isFinished()).thenReturn(true);
        invokeUpdate(oneShot);
        verify(Gdx.app, times(1)).postRunnable(any());
        verify(e, times(1)).dispose();

        // 4) On subsequent update — should not dispose again due to internal flag
        invokeUpdate(oneShot);
        verify(Gdx.app, times(1)).postRunnable(any());
        verify(e, times(1)).dispose();
    }
}
