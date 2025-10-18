package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * BossDeathComponent 的测试：
 * - atlas 未加载时安全跳过
 * - 正常路径：注册爆炸特效实体、启动动画、隐藏 Boss、保护共享 atlas、调度 Boss 安全销毁
 * - 内部 OneShotDisposeComponent：动画结束后只销毁一次
 *
 * 说明：
 * - 为了避免依赖事件系统签名，这里用反射直接调用 spawnExplosion() 来模拟 “death” 事件。
 */
public class BossDeathComponentTest {

    /** 把组件挂到 entity（避免依赖完整 ECS 生命周期） */
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

    /** 反射触发私有的 spawnExplosion()，等价于触发死亡事件 */
    private static void invokeSpawnExplosion(BossDeathComponent comp) {
        try {
            Method m = BossDeathComponent.class.getDeclaredMethod("spawnExplosion");
            m.setAccessible(true);
            m.invoke(comp);
        } catch (Exception e) {
            throw new RuntimeException("invoke spawnExplosion() failed", e);
        }
    }

    /** 反射调用组件的 update()（用于内部类测试） */
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
        // mock Gdx.app，postRunnable 由我们控制
        app = mock(Application.class);
        Gdx.app = app;

        // 基本的 boss entity
        boss = mock(Entity.class);
        when(boss.getPosition()).thenReturn(new Vector2(3f, 5f));
        when(boss.getScale()).thenReturn(new Vector2(1f, 1f));
    }

    @Test
    @DisplayName("Atlas 未加载时安全跳过，不抛异常")
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
    @DisplayName("OneShotDisposeComponent：动画结束后只销毁一次")
    void oneShotDispose_disposeExactlyOnce_whenAnimationFinished() throws Exception {
        // 构建一个带 ARC 的实体，并反射创建内部类实例
        Entity e = mock(Entity.class);
        AnimationRenderComponent arc = mock(AnimationRenderComponent.class);
        when(e.getComponent(AnimationRenderComponent.class)).thenReturn(arc);

        // 反射拿到私有静态内部类 BossDeathComponent$OneShotDisposeComponent
        Class<?> inner = Class.forName(BossDeathComponent.class.getName() + "$OneShotDisposeComponent");
        Constructor<?> ctor = inner.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object oneShot = ctor.newInstance();
        attachToEntity(oneShot, e);

        // postRunnable 立刻执行
        doAnswer(inv -> { ((Runnable) inv.getArgument(0)).run(); return null; })
                .when(Gdx.app).postRunnable(any());

        // 1) 无动画：不销毁
        when(arc.getCurrentAnimation()).thenReturn(null);
        invokeUpdate(oneShot);
        verify(e, never()).dispose();

        // 2) 有动画但未结束：不销毁
        when(arc.getCurrentAnimation()).thenReturn("any");
        when(arc.isFinished()).thenReturn(false);
        invokeUpdate(oneShot);
        verify(e, never()).dispose();

        // 3) 动画结束：销毁一次
        when(arc.isFinished()).thenReturn(true);
        invokeUpdate(oneShot);
        verify(Gdx.app, times(1)).postRunnable(any());
        verify(e, times(1)).dispose();

        // 4) 再次 update：由于内部 scheduled 标志，不应再次调度
        invokeUpdate(oneShot);
        verify(Gdx.app, times(1)).postRunnable(any());
        verify(e, times(1)).dispose();
    }
}
