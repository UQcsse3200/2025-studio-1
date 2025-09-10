package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(GameExtension.class)
@DisplayName("Health System - CombatStatsComponent")
class CombatStatsComponentTest {

  // --- helper ---
  private static class HealthSpy {
    final AtomicInteger last = new AtomicInteger(-1);
    final AtomicInteger cnt = new AtomicInteger(0);

    void onUpdate(Integer h) {last.set(h); cnt.incrementAndGet(); }
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
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      assertEquals(100, combat.getHealth());
      assertEquals(20, combat.getBaseAttack());
      assertEquals(100, combat.getMaxHealth());
      assertFalse(combat.isDead());
    }

    @Test
    void zeroHealth_isDeadImmediately() {
      CombatStatsComponent combat = new CombatStatsComponent(0, 5);
      assertEquals(0, combat.getHealth());
      assertTrue(combat.isDead());
    }

    @Test
    void negativeInitialHealth_clampsHealth_andMaxHealthToZero() {
      CombatStatsComponent combat = new CombatStatsComponent(-5, 3);
      assertEquals(0, combat.getHealth());
      assertEquals(0, combat.getMaxHealth()); // exposes ctor bug if it fails
      assertTrue(combat.isDead());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: setHealth clamps at lower bound and fires event")
  class SetHealthTests {
    @Test
    void positiveSet_updatesAndFiresEvent_withClampToMax() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.setHealth(150); // attempt to set above max
      assertEquals(100, combat.getHealth());        // now clamps to maxHealth
      assertEquals(100, spy.last.get());
      assertEquals(1, spy.cnt.get());
    }

    @Test
    void negativeSet_clampsToZero_andFiresEventOnce() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.setHealth(-50);
      assertEquals(0, combat.getHealth());
      assertEquals(0, spy.last.get());
      assertEquals(1, spy.cnt.get());
      assertTrue(combat.isDead());
    }

    @Test
    void idempotentSet_sameValue_mayNotFireDuplicateEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      // First change to a different value
      combat.addHealth(-10);
      assertEquals(90, combat.getHealth());
      int afterFirst = spy.cnt.get();

      // Add 0: no change in health, may or may not fire event once
      combat.addHealth(0);
      assertTrue(spy.cnt.get() == afterFirst || spy.cnt.get() == afterFirst + 1,
              "Implementation may or may not fire event when health unchanged");
      assertEquals(90, combat.getHealth());
    }

    @Test
    void setHealthZero_whenAlreadyZero_mayNotFireDuplicateEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(0, 5);
      HealthSpy spy = attachWithHealthSpy(combat);
      combat.setHealth(0);
      assertTrue(spy.cnt.get() <= 1); // allow implementation differences
    }

  }

  // ---=---
  @Nested
  @DisplayName("Objective: addHealth adjusts via setHealth (heal and damage)")
  class AddHealthTests {
    @Test
    void idempotentSet_sameValue_mayNotFireDuplicateEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      // First set to some value (clamped to maxHealth = 100)
      combat.setHealth(100);
      int afterFirst = spy.cnt.get();

      // Set to the same value again
      combat.setHealth(100);

      // Health remains at maxHealth
      assertEquals(100, combat.getHealth());
      // Depending on implementation, event may or may not fire on the same value
      assertTrue(spy.cnt.get() == afterFirst || spy.cnt.get() == afterFirst + 1);
    }


    @Test
    void bigDamage_overkill_clampsToZero() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.addHealth(-200);
      assertEquals(0, combat.getHealth());
      assertEquals(0, spy.last.get());
      assertTrue(combat.isDead());
    }

    @Test
    void addHealthZero_mayFireEvent_implementationDependent() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 10);
      HealthSpy spy = attachWithHealthSpy(combat);
      int before = spy.cnt.get();

      combat.addHealth(0);

      assertTrue(spy.cnt.get() == before || spy.cnt.get() == before + 1);
      assertEquals(100, combat.getHealth());
    }

    @Test
    void hitZero_doesNotFireEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 10);
      HealthSpy spy = attachWithHealthSpy(combat);
      int before = spy.cnt.get();

      combat.hit(0);

      assertEquals(before, spy.cnt.get());
      assertEquals(100, combat.getHealth());
    }

  }

  // ---=---
  @Nested
  @DisplayName("Objective: Death state toggles correctly")
  class DeathStateTests {
    @Test
    void reducedToZero_marksDead() {
      CombatStatsComponent combat = new CombatStatsComponent(1, 5);
      combat.addHealth(-1);
      assertEquals(0, combat.getHealth());
      assertTrue(combat.isDead());
    }

    @Test
    void reviveNotCovered_currentBehaviour_allowsHealIfMaxPositive() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 5);
      combat.setHealth(0);
      assertTrue(combat.isDead());

      combat.addHealth(+10);
      assertEquals(10, combat.getHealth());
      assertFalse(combat.isDead()); // because health > 0 clears death
    }

    @Test
    void isDead_whenHealthZero_andIgnoresFurtherDamage() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      assertFalse(combat.isDead());

      combat.setHealth(0);
      assertTrue(combat.isDead());
      assertEquals(0, combat.getHealth());

      // Dead entities ignore further damage
      combat.hit(999);
      assertTrue(combat.isDead());
      assertEquals(0, combat.getHealth());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: Dead entities ignore further damage")
  class IgnoreWhenDeadTests {
    @Test
    void hitInt_noChange_noEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(10, 5);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.setHealth(0);
      int before = spy.cnt.get();
      combat.hit(5);

      assertEquals(0, combat.getHealth());
      assertTrue(combat.isDead());
      assertEquals(before, spy.cnt.get()); // unchanged
    }

    @Test
    void hitAttacker_noChange_noEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(10, 5);
      HealthSpy spy = attachWithHealthSpy(combat);
      combat.setHealth(0);

      int before = spy.cnt.get();
      combat.hit(new CombatStatsComponent(50, 25));
      assertEquals(0, combat.getHealth());
      assertEquals(before, spy.cnt.get());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: Direct damage via hit(int)")
  class HitIntTests {
    @Test
    void positiveDamage_reducesHealth_andFiresEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.hit(30);
      assertEquals(70, combat.getHealth());
      assertEquals(70, spy.last.get());
      assertEquals(1, spy.cnt.get());
    }

    @Test
    void nonPositiveDamage_isIgnored() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.hit(0);
      combat.hit(-10);
      assertEquals(100, combat.getHealth());
      assertEquals(0, spy.cnt.get());
    }

    @Test
    void multipleHits_accumulate() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 10);
      combat.hit(15);
      combat.hit(5);
      combat.hit(10);
      assertEquals(70, combat.getHealth());
    }

    @Test
    void overkill_clampsToZero_andMarksDead() {
      CombatStatsComponent combat = new CombatStatsComponent(30, 20);
      combat.hit(100);
      assertEquals(0, combat.getHealth());
      assertTrue(combat.isDead());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: Entity damage via hit(attacker)")
  class HitFromAttackerTests {
    @Test
    void usesAttackersBaseAttack() {
      CombatStatsComponent def = new CombatStatsComponent(100, 20);
      CombatStatsComponent atk = new CombatStatsComponent(50, 25);
      def.hit(atk);
      assertEquals(75, def.getHealth());
    }

    @Test
    void nullAttacker_isNoOp() {
      CombatStatsComponent def = new CombatStatsComponent(100, 20);
      def.hit((CombatStatsComponent) null);
      assertEquals(100, def.getHealth());
    }

    @Test
    void negativeInitialBaseAttack_defaultsToZero() {
      CombatStatsComponent combat = new CombatStatsComponent(10, -5);
      assertEquals(0, combat.getBaseAttack());
    }


    @Test
    void negativeBaseAttack_isIgnored_currentBehaviour() {
      CombatStatsComponent def = new CombatStatsComponent(100, 20);
      CombatStatsComponent weird = new CombatStatsComponent(20, -5); // normalised to 0 in setter
      def.hit(weird);
      assertEquals(100, def.getHealth());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: Damage ignored when already dead (int and attacker)")
  class IgnoreBothPathsWhenDeadTests {
    @Test
    void ignoreBothVariants() {
      CombatStatsComponent def = new CombatStatsComponent(0, 20);
      def.hit(999);
      def.hit(new CombatStatsComponent(10, 10));
      assertEquals(0, def.getHealth());
      assertTrue(def.isDead());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: Base attack semantics (setter, ctor, and usage)")
  class BaseAttackValidationTests {

    @Test
    @DisplayName("Ctor: negative baseAttack defaults to 0 and causes no damage")
    void ctor_negativeBaseAttack_defaultsToZero_andNoDamage() {
      CombatStatsComponent def = new CombatStatsComponent(100, 20);
      CombatStatsComponent atk = new CombatStatsComponent(10, -5); // -> 0
      assertEquals(0, atk.getBaseAttack());

      def.hit(atk);
      assertEquals(100, def.getHealth());
    }

    @Test
    @DisplayName("Setter: negative values are rejected and previous value is kept")
    void setBaseAttack_negative_rejected_keepsPrevious() {
      CombatStatsComponent atk = new CombatStatsComponent(50, 7);
      atk.setBaseAttack(-3);
      assertEquals(7, atk.getBaseAttack());

      // Subsequent valid set still works
      atk.setBaseAttack(11);
      assertEquals(11, atk.getBaseAttack());
    }

    @Test
    @DisplayName("Setter: idempotent sets do not change damage outcome")
    void setBaseAttack_idempotent_noEffectOnOutcome() {
      CombatStatsComponent def = new CombatStatsComponent(100, 0);
      CombatStatsComponent atk = new CombatStatsComponent(10, 15);

      // Same value twice
      atk.setBaseAttack(15);
      atk.setBaseAttack(15);

      def.hit(atk);
      assertEquals(85, def.getHealth());
    }

    @Test
    @DisplayName("Runtime: changing baseAttack affects subsequent hits (not past ones)")
    void changingBaseAttack_updatesSubsequentDamageOnly() {
      CombatStatsComponent def = new CombatStatsComponent(100, 0);
      CombatStatsComponent atk = new CombatStatsComponent(10, 5);

      def.hit(atk);                 // -5 -> 95
      assertEquals(95, def.getHealth());

      atk.setBaseAttack(30);        // update
      def.hit(atk);                 // -30 -> 65
      assertEquals(65, def.getHealth());
    }

    @Test
    @DisplayName("Bounds: very large non-negative baseAttack overkills but clamps health to 0")
    void extremeLargeBaseAttack_overkill_clampsToZero() {
      CombatStatsComponent def = new CombatStatsComponent(100, 0);
      CombatStatsComponent atk = new CombatStatsComponent(10, Integer.MAX_VALUE);

      def.hit(atk);
      assertEquals(0, def.getHealth());
      assertTrue(def.isDead());
    }

    @Test
    @DisplayName("Zero baseAttack is a no-op on hit(attacker)")
    void zeroBaseAttack_attackerDoesNoDamage() {
      CombatStatsComponent def = new CombatStatsComponent(100, 20);
      CombatStatsComponent atk = new CombatStatsComponent(10, 0);
      def.hit(atk);
      assertEquals(100, def.getHealth());
    }
  }


  // ---=---
  @Nested
  @DisplayName("Objective: setMaxHealth behaviour and event (known defect)")
  class MaxHealthTests {
    @Test
    void updatesMax_andFiresEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
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
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
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
  @Nested
  @DisplayName("Objective: Health clamps to maxHealth on overheal")
  class OverhealBehaviourTests {
    @Test
    void healingBeyondMax_isCapped() {
      CombatStatsComponent combat = new CombatStatsComponent(50, 10);
      assertEquals(50, combat.getMaxHealth());
      combat.addHealth(200); // try to overheal
      assertEquals(50, combat.getHealth()); // capped at maxHealth
    }

    @Test
    void setHealthAboveMax_isCapped() {
      CombatStatsComponent combat = new CombatStatsComponent(50, 10);
      combat.setMaxHealth(50);
      combat.setHealth(100); // try to set above max
      assertEquals(50, combat.getHealth());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: Event routing sanity (exactly once per effective change)")
  class EventRoutingTests {
    @Test
    void firesOncePerEffectiveChange_andNotWhenDead() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.hit(10);             // effective
      combat.hit(0);              // ignored
      combat.hit(-5);             // ignored
      combat.setHealth(90);       // same as current -> may be ignored by impl
      int beforeDeath = spy.cnt.get();

      combat.setHealth(0);        // death event
      int atDeath = spy.cnt.get();
      combat.hit(999);            // ignored when dead
      combat.hit(new CombatStatsComponent(10, 10)); // ignored when dead

      assertTrue(beforeDeath >= 1);
      assertEquals(atDeath, spy.cnt.get(), "No events after dead");
      assertEquals(0, combat.getHealth());
    }
  }
  
  @Test
  void enemyShouldBeDeadWhenHealthZero() {
    CombatStatsComponent enemy = new CombatStatsComponent(50, 10);
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
  void hitRemovesHealth() {
    //Damage an entity and check the new health is correct
    Entity victim = new Entity();
    victim.addComponent(new CombatStatsComponent(100, 0));
    assertEquals(100, victim.getComponent(CombatStatsComponent.class).getHealth());
    victim.getComponent(CombatStatsComponent.class).hit(new CombatStatsComponent(0, 50));
    assertEquals(50, victim.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldSetGetCooldown() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 10);
    assertEquals(0, combat.getCoolDown());
    combat.setCoolDown(100);
    assertEquals(100, combat.getCoolDown());
    combat.setCoolDown(-100);
    assertEquals(0, combat.getCoolDown());
  }

  // @Test
  // void deathHitRemovesEntity() {
  //   //Tests if killing an entity removes it from the game
  //   PhysicsEngine physicsEngine;
  //   PhysicsService physicsService = new PhysicsService();
  //   ServiceLocator.registerPhysicsService(physicsService);
  //   physicsEngine = physicsService.getPhysics();
  //   Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);
  //   Renderer renderer = RenderFactory.createRenderer();
  //   renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
  //   renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());
  //   Entity victim = new Entity();
  //   victim.addComponent(new CombatStatsComponent(10, 10));
  //   GameArea area = new ForestGameArea(new TerrainFactory(renderer.getCamera()), renderer.getCamera());
  //   ServiceLocator.registerGameArea(area);
  //   ServiceLocator.registerEntityService(new EntityService());
  //   area.spawnEntity(victim);
  //   // Add a listener to simulate death removal as in the real game
  //   victim.getEvents().addListener("death", () -> area.removeEntity(victim));
  //   victim.getComponent(CombatStatsComponent.class).hit(new CombatStatsComponent(0, 10));
  //   assertTrue(victim.getComponent(CombatStatsComponent.class).isDead());
  //   assertEquals(new ArrayList<>(), area.getEntities());

  // }

  @Test
  void shouldTakeDirectDamage() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    combat.hit(20);
    assertEquals(80, combat.getHealth());
  }

  @Test
  void shouldTakeDamageFromAttacker() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    CombatStatsComponent attacker = new CombatStatsComponent(50, 15);
    combat.hit(attacker);
    assertEquals(85, combat.getHealth());
  }
}
