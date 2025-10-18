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
 * Tests for BlackholeAttackComponent without referencing a TimeSource type.
 * Covers:
 *  - early return when entity == null
 *  - early return when target == null
 *  - pull when player within radius
 *  - no pull when player outside radius
 *  - disposal after lifeTime via Gdx.app.postRunnable() and only once
 */
public class BlackholeAttackComponentTest {

    // Attach component to a mocked Entity without full ECS
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

    @BeforeEach
    void setupGdxApp() {
        // Mock Gdx.app; tests可在需要时让postRunnable立刻执行
        Application app = mock(Application.class);
        Gdx.app = app;
    }

    @Test
    void update_returnsEarly_whenEntityIsNull() {
        Entity target = mock(Entity.class);
        BlackholeAttackComponent comp = new BlackholeAttackComponent(target, 5f, 10f);

        assertDoesNotThrow(comp::update);
        verifyNoInteractions(target);
    }

    @Test
    void update_returnsEarly_whenTargetIsNull() {
        Entity host = mock(Entity.class);
        BlackholeAttackComponent comp = new BlackholeAttackComponent(null, 5f, 10f);
        attachToEntity(comp, host);

        assertDoesNotThrow(comp::update);
        verify(host, never()).dispose();
    }

    @Test
    void update_appliesPull_whenWithinRadius() {
        // 使用 deep stubs：直接链式 stub ServiceLocator.getTimeSource().getDeltaTime()
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(0.016f);

            Entity host = mock(Entity.class);
            Entity player = mock(Entity.class);

            // 黑洞中心 (10,10)
            when(host.getCenterPosition()).thenReturn(new Vector2(10f, 10f));
            // 玩家中心 (12,10) 在半径5内；玩家当前位置用于 setPosition 计算
            when(player.getCenterPosition()).thenReturn(new Vector2(12f, 10f));
            when(player.getPosition()).thenReturn(new Vector2(12f, 10f));

            BlackholeAttackComponent comp = new BlackholeAttackComponent(player, 5f, 10f);
            attachToEntity(comp, host);

            comp.update();

            // 期望：pullFactor=0.07 -> (11.86, 10.0)
            verify(player).setPosition(floatThat(x -> Math.abs(x - 11.86f) < 1e-3f),
                    floatThat(y -> Math.abs(y - 10f) < 1e-3f));
            verify(host, never()).dispose();
        }
    }

    @Test
    void update_doesNotPull_whenOutsideRadius() {
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(0.016f);

            Entity host = mock(Entity.class);
            Entity player = mock(Entity.class);

            // 距离很大：> radius 5
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

    @Test
    void update_disposesOnLifetimeExpiry_onlyOnce() {
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            // 两帧 0.6 + 0.6 > lifeTime(1.0)
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime())
                    .thenReturn(0.6f, 0.6f, 0.6f);

            // 让 postRunnable 立即执行 runnable
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

            // 第一次：0.6 未到期
            comp.update();
            verify(host, never()).dispose();

            // 第二次：1.2 到期 -> 应只调度/销毁一次
            comp.update();
            verify(Gdx.app, times(1)).postRunnable(any());
            verify(host, times(1)).dispose();

            // 第三次：已处于 disposed 状态，不应再次调度
            comp.update();
            verify(Gdx.app, times(1)).postRunnable(any());
            verify(host, times(1)).dispose();
        }
    }
}
