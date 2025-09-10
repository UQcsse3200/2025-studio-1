package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
@DisplayName("Health System - CombatStatsComponent")
class CombatStatsComponentTest {

  // --- helper ---
  private static class HealthSpy {
    final AtomicInteger last = new AtomicInteger(-1);
    final AtomicInteger cnt = new AtomicInteger(0);
    void onUpdate(Integer h) { last.set(h); cnt.incrementAndGet(); }
  }

  /** Attach a component to an Entity and subscribe a spy to "updateHealth". */
  private static HealthSpy attachWithHealthSpy(CombatStatsComponent combat) {
    HealthSpy spy = new HealthSpy();
    Entity entity = new Entity().addComponent(combat);
    entity.getEvents().addListener("updateHealth", spy::onUpdate);
    entity.create();
    return spy;
  }

  // ---=---
  @Nested
  @DisplayName("Objective: Verify initialisation")
  class InitTests {
    @Test
    void healthAttackMax_areInitialised() {
      CombatStatsComponent combat = new CombatStatsComponent(100);
      assertEquals(100, combat.getHealth());
      assertEquals(100, combat.getMaxHealth());
      assertFalse(combat.isDead());
    }

    @Test
    void zeroHealth_isDeadImmediately() {
      CombatStatsComponent combat = new CombatStatsComponent(0);
      assertEquals(0, combat.getHealth());
      assertTrue(combat.isDead());
    }

    @Test
    void negativeInitialHealth_clampsHealth_andMaxHealthToZero() {
      CombatStatsComponent combat = new CombatStatsComponent(-5);
      assertEquals(0, combat.getHealth());
      assertEquals(0, combat.getMaxHealth());
      assertTrue(combat.isDead());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: setHealth clamps at lower bound and fires event")
  class SetHealthTests {
    @Test
    void positiveSet_updatesAndFiresEvent_withClampToMax() {
      CombatStatsComponent combat = new CombatStatsComponent(100);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.setHealth(150); // attempt to set above max
      assertEquals(100, combat.getHealth());
      assertEquals(100, spy.last.get());
      assertEquals(1, spy.cnt.get());
    }

    @Test
    void negativeSet_clampsToZero_andFiresEventOnce() {
      CombatStatsComponent combat = new CombatStatsComponent(100);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.setHealth(-50);
      assertEquals(0, combat.getHealth());
      assertEquals(0, spy.last.get());
      assertEquals(1, spy.cnt.get());
      assertTrue(combat.isDead());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: addHealth adjusts via setHealth (heal and damage)")
  class AddHealthTests {
    @Test
    void bigDamage_overkill_clampsToZero() {
      CombatStatsComponent combat = new CombatStatsComponent(100);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.addHealth(-200);
      assertEquals(0, combat.getHealth());
      assertEquals(0, spy.last.get());
      assertTrue(combat.isDead());
    }

    @Test
    void healingBeyondMax_isCapped() {
      CombatStatsComponent combat = new CombatStatsComponent(50);
      combat.addHealth(200); // overheal
      assertEquals(50, combat.getHealth());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: Death state toggles correctly")
  class DeathStateTests {
    @Test
    void reducedToZero_marksDead() {
      CombatStatsComponent combat = new CombatStatsComponent(1);
      combat.addHealth(-1);
      assertEquals(0, combat.getHealth());
      assertTrue(combat.isDead());
    }

    @Test
    void reviveFromZero_allowsHealIfMaxPositive() {
      CombatStatsComponent combat = new CombatStatsComponent(100);
      combat.setHealth(0);
      assertTrue(combat.isDead());

      combat.addHealth(+10);
      assertEquals(10, combat.getHealth());
      assertFalse(combat.isDead());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: setMaxHealth behaviour and event")
  class MaxHealthTests {
    @Test
    void updatesMax_andFiresEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100);
      AtomicInteger lastMax = new AtomicInteger(-1);
      Entity entity = new Entity().addComponent(combat);
      entity.getEvents().addListener("updateMaxHealth", lastMax::set);
      entity.create();

      combat.setMaxHealth(250);
      assertEquals(250, combat.getMaxHealth());
      assertEquals(250, lastMax.get());
    }

    @Test
    void setMaxHealth_negative_clampsToZero_andFiresEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100);
      AtomicInteger lastMax = new AtomicInteger(-1);
      Entity entity = new Entity().addComponent(combat);
      entity.getEvents().addListener("updateMaxHealth", lastMax::set);
      entity.create();

      combat.setMaxHealth(-10);
      assertEquals(0, combat.getMaxHealth());
      assertEquals(0, lastMax.get());
    }
  }

  // ---=---
  @Test
  void enemyShouldBeDeadWhenHealthZero() {
    CombatStatsComponent enemy = new CombatStatsComponent(50);
    assertFalse(enemy.isDead());
    enemy.addHealth(-50);
    assertEquals(0, enemy.getHealth());
    assertTrue(enemy.isDead());
    // Overkill
    enemy.addHealth(-100);
    assertEquals(0, enemy.getHealth());
    assertTrue(enemy.isDead());
  }

  @Test
  void takeDamageRemovesHealth() {
    CombatStatsComponent combat = new CombatStatsComponent(100);
    assertEquals(100, combat.getHealth());
    combat.takeDamage(50);
    assertEquals(50, combat.getHealth());
  }

  @Test
  void deathHitRemovesEntity() {
    Entity victim = new Entity();
    victim.addComponent(new CombatStatsComponent(10));
    GameArea area = new ForestGameArea(new TerrainFactory(new CameraComponent()), new CameraComponent());
    ServiceLocator.registerGameArea(area);
    ServiceLocator.registerEntityService(new EntityService());
    area.spawnEntity(victim);

    victim.getEvents().addListener("death", () -> area.removeEntity(victim));
    victim.getComponent(CombatStatsComponent.class).takeDamage(10);

    assertTrue(victim.getComponent(CombatStatsComponent.class).isDead());
    assertEquals(new ArrayList<>(), area.getEntities());
  }
}
