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

        // Minimal graphics/GL mocks to avoid NPEs in texture scaling/render utilities
        Gdx.graphics = mock(Graphics.class);
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = Gdx.gl;
        // Stub width/height to avoid zero-size rendering math or NPEs in scaling utilities
        when(Gdx.graphics.getWidth()).thenReturn(800);
        when(Gdx.graphics.getHeight()).thenReturn(600);

        // Minimal ServiceLocator stubs
        serviceLocatorStatic = mockStatic(ServiceLocator.class);
        time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(0.016f);

        resources = mock(ResourceService.class);
        entities = mock(EntityService.class);

        when(ServiceLocator.getTimeSource()).thenReturn(time);
        when(ServiceLocator.getResourceService()).thenReturn(resources);
        when(ServiceLocator.getEntityService()).thenReturn(entities);
        // Physics service is required by PhysicsComponent constructor
        PhysicsService physicsService = mock(PhysicsService.class);
        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        when(physicsService.getPhysics()).thenReturn(physicsEngine);
        when(ServiceLocator.getPhysicsService()).thenReturn(physicsService);
        // Provide a default non-null GameArea (tests that need null will override it explicitly later)
        GameArea defaultArea = mock(GameArea.class);
        when(defaultArea.roomNumber()).thenReturn(1f);
        when(ServiceLocator.getGameArea()).thenReturn(defaultArea);
        // Provide a stub Texture with size to avoid NPEs inside TextureRenderComponent.scaleEntity()
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

    /**
     * Spawns one full ring when off cooldown: verifies entity registration count
     * and that projectile damage is scaled by Difficulty & roomNumber.
     */
    @Test
    void BossAttack2Tests_successSpawn_registersCountProjectiles_andDefaultDamage() {
      // Owner entity with base attack 10
      Entity owner = new Entity();
      owner.addComponent(new WeaponsStatsComponent(10));
      owner.setPosition(0, 0);

      EnemyMudRingSprayComponent spray = new EnemyMudRingSprayComponent(
          /*cooldown*/1f, /*count*/8, /*speed*/5f, /*life*/1.5f);
      owner.addComponent(spray);

      // Ensure default-branch behavior: no Difficulty service present
      when(ServiceLocator.getDifficulty()).thenReturn(null);

      // Small dt; component should spawn exactly one ring this frame
      when(time.getDeltaTime()).thenReturn(0.016f);

      spray.update();

      // Capture all registered projectiles
      ArgumentCaptor<Entity> cap = ArgumentCaptor.forClass(Entity.class);
      verify(entities, times(8)).register(cap.capture());
      assertEquals(8, cap.getAllValues().size(), "Should spawn exactly `count` projectiles");

      // With base=10 and scale defaulting to 1 (no difficulty), damage is (int)(1 * 10 / 8) == 1
      for (Entity p : cap.getAllValues()) {
        WeaponsStatsComponent w = p.getComponent(WeaponsStatsComponent.class);
        assertEquals(1, w.getBaseAttack(), "Default damage should be 10/8 when no difficulty service present");
      }
    }

    /**
     * After a successful spawn, the internal cooldown should block subsequent spawns
     * until the cooldown duration elapses.
     */
    @Test
    void BossAttack2Tests_cooldownBlocksSecondSpawn() {
      Entity owner = new Entity();
      owner.addComponent(new WeaponsStatsComponent(10));
      owner.addComponent(new EnemyMudRingSprayComponent(1f, 6, 5f, 1.5f));

      // First update triggers a spawn ring
      when(time.getDeltaTime()).thenReturn(0.016f);
      owner.getComponent(EnemyMudRingSprayComponent.class).update();
      verify(entities, times(6)).register(any());

      // While on cooldown, multiple updates should NOT spawn again
      when(time.getDeltaTime()).thenReturn(0.1f);
      for (int i = 0; i < 5; i++) {
        owner.getComponent(EnemyMudRingSprayComponent.class).update();
      }
      // Still only the original six registrations
      verify(entities, times(6)).register(any());
    }

    /**
     * Default branches: no boss WeaponsStats, no Difficulty and no GameArea →
     * scale defaults to 1 and base damage defaults to 10, so projectile damage becomes 10/8 = 1.
     */
    @Test
    void BossAttack2Tests_defaults_whenNoWeaponsStats_orNoDiff_orNoArea() {
      // No Difficulty/GameArea provided
      when(ServiceLocator.getDifficulty()).thenReturn(null);
      when(ServiceLocator.getGameArea()).thenReturn(null);

      Entity owner = new Entity();
      owner.setPosition(0, 0);
      EnemyMudRingSprayComponent spray = new EnemyMudRingSprayComponent(
          1f, 3, 5f, 1.5f);
      owner.addComponent(spray);

      when(time.getDeltaTime()).thenReturn(0.016f);
      spray.update();

      ArgumentCaptor<Entity> cap = ArgumentCaptor.forClass(Entity.class);
      verify(entities, times(3)).register(cap.capture());

      // With default base=10 and scale=1, damage becomes floor(10/8) = 1
      for (Entity p : cap.getAllValues()) {
        WeaponsStatsComponent w = p.getComponent(WeaponsStatsComponent.class);
        assertEquals(1, w.getBaseAttack(), "Default damage should be 10/8 when no difficulty service present");
      }
    }

    /**
     * Each successful ring spawn should advance angleOffset by 7 degrees (in radians).
     */
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
      assertEquals(before + expected, after, 1e-5f,
          "angleOffset should advance by 7 degrees (in radians) after each ring");
    }
}
