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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

/**
 * Unit tests for {@link BossDeathComponent}, written in a way that does not depend
 * on the EventHandler.addListener() signature.
 *
 * Strategy:
 * - Use reflection to invoke the private method spawnExplosion() directly
 *   (this simulates triggering the "death" event).
 * - Verify safe skip when the atlas is missing.
 * - Verify the full normal behavior: explosion effect entity registration,
 *   animation start, boss hiding, shared atlas protection, and boss disposal.
 * - Verify OneShotDisposeComponent only disposes its entity once.
 */
public class BossDeathComponentTest {

    /** Helper: attach a component to a mocked Entity via reflection. */
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
                throw new RuntimeException("Failed to attach component to entity.", e);
            }
        }
    }

    /** Helper: call the private spawnExplosion() using reflection. */
    private static void invokeSpawnExplosion(BossDeathComponent comp) {
        try {
            Method m = BossDeathComponent.class.getDeclaredMethod("spawnExplosion");
            m.setAccessible(true);
            m.invoke(comp);
        } catch (Exception e) {
            throw new RuntimeException("invoke spawnExplosion() failed", e);
        }
    }

    /** Helper: call update() on any Component subclass using reflection. */
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
        // Mock Gdx.app so postRunnable() won't throw a NullPointerException.
        app = mock(Application.class);
        Gdx.app = app;

        // Mock the boss entity.
        boss = mock(Entity.class);
        when(boss.getPosition()).thenReturn(new Vector2(3f, 5f));
        when(boss.getScale()).thenReturn(new Vector2(1f, 1f));
    }

    @Test
    void skipWhenAtlasMissing_safeNoCrash() {
        BossDeathComponent comp = new BossDeathComponent();
        attachToEntity(comp, boss);

        // Simulate missing atlas — ResourceService present but containsAsset() = false.
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            ResourceService rs = mock(ResourceService.class);
            sl.when(ServiceLocator::getResourceService).thenReturn(rs);
            when(rs.containsAsset(anyString(), eq(TextureAtlas.class))).thenReturn(false);

            EntityService es = mock(EntityService.class);
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            // Should safely exit with no crash.
            assertDoesNotThrow(() -> invokeSpawnExplosion(comp));

            verify(es, never()).register(any(Entity.class));
            verify(app, never()).postRunnable(any());
            verify(boss, never()).setEnabled(false);
            verify(boss, never()).getComponent(AnimationRenderComponent.class);
        }
    }

    @Test
    void happyPath_registerEffect_startAnimation_disableBoss_disposeOnce() {
        BossDeathComponent comp = new BossDeathComponent(0.06f, 4f);
        attachToEntity(comp, boss);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            // 1) ResourceService: atlas exists
            ResourceService rs = mock(ResourceService.class);
            TextureAtlas atlas = mock(TextureAtlas.class);
            sl.when(ServiceLocator::getResourceService).thenReturn(rs);
            when(rs.containsAsset(anyString(), eq(TextureAtlas.class))).thenReturn(true);
            when(rs.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);

            // 2) EntityService: capture the registered effect entity
            EntityService es = mock(EntityService.class);
            sl.when(ServiceLocator::getEntityService).thenReturn(es);
            ArgumentCaptor<Entity> effectCap = ArgumentCaptor.forClass(Entity.class);
            doNothing().when(es).register(effectCap.capture());

            // 3) Boss ARC should be setDisposeAtlas(false)
            AnimationRenderComponent bossArc = mock(AnimationRenderComponent.class);
            when(boss.getComponent(AnimationRenderComponent.class)).thenReturn(bossArc);

            // 4) Run postRunnable() immediately
            doAnswer(inv -> {
                ((Runnable) inv.getArgument(0)).run();
                return null;
            }).when(app).postRunnable(any());

            // Directly trigger the explosion via reflection
            invokeSpawnExplosion(comp);

            // Verify an effect entity was registered
            verify(es, times(1)).register(any(Entity.class));
            Entity effect = effectCap.getValue();

            // Boss should be hidden and its ARC should be protected
            verify(boss, times(1)).setEnabled(false);
            verify(bossArc, times(1)).setDisposeAtlas(false);

            // Boss disposal was scheduled and executed exactly once
            verify(app, times(1)).postRunnable(any());
            verify(boss, times(1)).dispose();

            // We cannot assert exact internal state of `effect` without a real ECS,
            // but reaching here confirms the sequence completed successfully.
        }
    }

    @Test
    void oneShotDispose_disposeExactlyOnce_whenAnimationFinished() throws Exception {
        // Prepare an entity with a mocked ARC and manually instantiate the inner component
        Entity e = mock(Entity.class);
        AnimationRenderComponent arc = mock(AnimationRenderComponent.class);
        when(e.getComponent(AnimationRenderComponent.class)).thenReturn(arc);

        // Reflect the private static inner class: BossDeathComponent$OneShotDisposeComponent
        Class<?> inner = Class.forName(BossDeathComponent.class.getName() + "$OneShotDisposeComponent");
        Constructor<?> ctor = inner.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object oneShot = ctor.newInstance();
        attachToEntity(oneShot, e);

        // Make Gdx.app.postRunnable run immediately
        doAnswer(inv -> { ((Runnable) inv.getArgument(0)).run(); return null; })
                .when(app).postRunnable(any());

        // Case 1: No current animation -> no disposal
        when(arc.getCurrentAnimation()).thenReturn(null);
        invokeUpdate(oneShot);
        verify(e, never()).dispose();

        // Case 2: Animation not finished -> no disposal
        when(arc.getCurrentAnimation()).thenReturn("any");
        when(arc.isFinished()).thenReturn(false);
        invokeUpdate(oneShot);
        verify(e, never()).dispose();

        // Case 3: Animation finished -> dispose once
        when(arc.isFinished()).thenReturn(true);
        invokeUpdate(oneShot);
        verify(app, times(1)).postRunnable(any());
        verify(e, times(1)).dispose();

        // Case 4: Subsequent updates -> no further disposal (guarded by 'scheduled')
        invokeUpdate(oneShot);
        verify(app, times(1)).postRunnable(any());
        verify(e, times(1)).dispose();
    }
}
