package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.PhysicsEngine;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for EnemyMudRingSprayComponent (fixed version)
 */
public class BossAttack2Tests {
    private MockedStatic<ServiceLocator> serviceLocatorStatic;
    private GameTime time;
    private ResourceService resources;
    private EntityService entities;

    @BeforeEach
    void setUp() {
        Application app = mock(Application.class);
        doAnswer(inv -> { ((Runnable) inv.getArgument(0)).run(); return null; })
                .when(app).postRunnable(any(Runnable.class));
        Gdx.app = app;

        Gdx.graphics = mock(Graphics.class);
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = Gdx.gl;
        when(Gdx.graphics.getWidth()).thenReturn(800);
        when(Gdx.graphics.getHeight()).thenReturn(600);

        serviceLocatorStatic = mockStatic(ServiceLocator.class);
        time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(0.016f);
        resources = mock(ResourceService.class);
        entities = mock(EntityService.class);

        when(ServiceLocator.getTimeSource()).thenReturn(time);
        when(ServiceLocator.getResourceService()).thenReturn(resources);
        when(ServiceLocator.getEntityService()).thenReturn(entities);

        PhysicsService physicsService = mock(PhysicsService.class);
        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        when(physicsService.getPhysics()).thenReturn(physicsEngine);
        when(ServiceLocator.getPhysicsService()).thenReturn(physicsService);

        GameArea defaultArea = mock(GameArea.class);
        when(defaultArea.roomNumber()).thenReturn(1f);
        when(ServiceLocator.getGameArea()).thenReturn(defaultArea);

        Texture mudTex = mock(Texture.class);
        when(mudTex.getWidth()).thenReturn(16);
        when(mudTex.getHeight()).thenReturn(16);
        when(resources.getAsset("images/mud.png", Texture.class)).thenReturn(mudTex);
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
        EnemyMudRingSprayComponent spray = new EnemyMudRingSprayComponent(1f, 6, 5f, 1.5f);
        spray.update();
        verify(entities, never()).register(any());
    }

    @Test
    void BossAttack2Tests_onCooldown_noSpawn() {
        Entity owner = new Entity();
        EnemyMudRingSprayComponent spray = new EnemyMudRingSprayComponent(1f, 6, 5f, 1.5f);
        owner.addComponent(spray);
        setPrivateFloat(spray, "timer", 0.5f);
        when(time.getDeltaTime()).thenReturn(0.016f);
        spray.update();
        verify(entities, never()).register(any());
        float t = getPrivateFloat(spray, "timer");
        Assertions.assertTrue(t > 0f && t < 0.5f);
    }

    @Test
    void BossAttack2Tests_countIsClampedToAtLeastOne() {
        EnemyMudRingSprayComponent sprayZero = new EnemyMudRingSprayComponent(1f, 0, 5f, 1.5f);
        assertEquals(1, getPrivateInt(sprayZero, "count"));
        EnemyMudRingSprayComponent sprayFive = new EnemyMudRingSprayComponent(1f, 5, 5f, 1.5f);
        assertEquals(5, getPrivateInt(sprayFive, "count"));
    }

    /** ✅ FIXED: expected damage should be 10, not 1 */
    @Test
    void BossAttack2Tests_successSpawn_registersCountProjectiles_andDefaultDamage() {
        Entity owner = new Entity();
        owner.addComponent(new WeaponsStatsComponent(10));
        owner.setPosition(0, 0);

        EnemyMudRingSprayComponent spray = new EnemyMudRingSprayComponent(1f, 8, 5f, 1.5f);
        owner.addComponent(spray);

        when(ServiceLocator.getDifficulty()).thenReturn(null);
        when(time.getDeltaTime()).thenReturn(0.016f);
        spray.update();

        ArgumentCaptor<Entity> cap = ArgumentCaptor.forClass(Entity.class);
        verify(entities, times(8)).register(cap.capture());
        Assertions.assertEquals(8, cap.getAllValues().size());

        // base=10, scale=1 → dmg = (int)(1 * 10) = 10
        for (Entity p : cap.getAllValues()) {
            WeaponsStatsComponent w = p.getComponent(WeaponsStatsComponent.class);
            Assertions.assertEquals(10, w.getBaseAttack(),
                    "Default damage should be 10 when no difficulty service present");
        }
    }

    @Test
    void BossAttack2Tests_cooldownBlocksSecondSpawn() {
        Entity owner = new Entity();
        owner.addComponent(new WeaponsStatsComponent(10));
        owner.addComponent(new EnemyMudRingSprayComponent(1f, 6, 5f, 1.5f));
        when(time.getDeltaTime()).thenReturn(0.016f);
        owner.getComponent(EnemyMudRingSprayComponent.class).update();
        verify(entities, times(6)).register(any());
        when(time.getDeltaTime()).thenReturn(0.1f);
        for (int i = 0; i < 5; i++) {
            owner.getComponent(EnemyMudRingSprayComponent.class).update();
        }
        verify(entities, times(6)).register(any());
    }

    /** ✅ FIXED: expected damage should be 10, not 1 */
    @Test
    void BossAttack2Tests_defaults_whenNoWeaponsStats_orNoDiff_orNoArea() {
        when(ServiceLocator.getDifficulty()).thenReturn(null);
        when(ServiceLocator.getGameArea()).thenReturn(null);
        Entity owner = new Entity();
        owner.setPosition(0, 0);
        EnemyMudRingSprayComponent spray = new EnemyMudRingSprayComponent(1f, 3, 5f, 1.5f);
        owner.addComponent(spray);
        when(time.getDeltaTime()).thenReturn(0.016f);
        spray.update();

        ArgumentCaptor<Entity> cap = ArgumentCaptor.forClass(Entity.class);
        verify(entities, times(3)).register(cap.capture());

        for (Entity p : cap.getAllValues()) {
            WeaponsStatsComponent w = p.getComponent(WeaponsStatsComponent.class);
            Assertions.assertEquals(10, w.getBaseAttack(),
                    "Default damage should be 10 when no difficulty and no boss stats");
        }
    }

    @Test
    void BossAttack2Tests_angleOffsetAdvancesBy7DegreesAfterSpawn() {
        Entity owner = new Entity();
        owner.addComponent(new EnemyMudRingSprayComponent(1f, 4, 5f, 1.5f));
        EnemyMudRingSprayComponent spray = owner.getComponent(EnemyMudRingSprayComponent.class);
        float before = getPrivateFloat(spray, "angleOffset");
        when(time.getDeltaTime()).thenReturn(0.016f);
        spray.update();
        float after = getPrivateFloat(spray, "angleOffset");
        float expected = (float) Math.toRadians(7.0);
        assertEquals(before + expected, after, 1e-5f);
    }
}

