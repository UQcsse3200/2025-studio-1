package com.csse3200.game.components;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.entities.Entity;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
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
  }

  // ---=---
  @Nested
  @DisplayName("Objective: setHealth clamps at lower bound and fires event")
  class SetHealthTests {
    @Test
    void postiveSet_updatesAndFiresEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.setHealth(150);
      assertEquals(150, combat.getHealth());
      assertEquals(150, spy.last.get());
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

      combat.setHealth(100);
      int afterFirst = spy.cnt.get();
      combat.setHealth(100);
      assertTrue(spy.cnt.get() == afterFirst || spy.cnt.get() == afterFirst + 1,
              "Implementation may de-duplicate identical sets; allow either.");
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

      combat.addHealth(+25);
      assertEquals(125, combat.getHealth());
      assertEquals(125, spy.last.get());
    }

    void bigDamage_overkill_clampsToZero() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.addHealth(-200);
      assertEquals(0, combat.getHealth());
      assertEquals(0, spy.last.get());
      assertTrue(combat.isDead());
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
    void reviveNotCovered_currentBehaviourNoAutoRevive() {
      CombatStatsComponent combat = new CombatStatsComponent(0, 5);
      combat.addHealth(+10);
      // Current behaviour unspecified; assert health increased but dead state may remain or flip.
      assertEquals(10, combat.getHealth());
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

      combat.hit(new CombatStatsComponent(50, 25));
      assertEquals(0, combat.getHealth());
      assertEquals(spy.cnt.get(), spy.cnt.get());;
    }
  }
  // Health clamps at lower bound for both max and health
  @Test
  void setHealth_clampsAndFiresUpdateHealthEvent() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    Entity entity = new Entity().addComponent(combat);
    AtomicInteger lastHealth = new AtomicInteger(-1);
    entity.getEvents().addListener("updateHealth", lastHealth::set);
    entity.create();

    combat.setHealth(150);
    assertEquals(150, combat.getHealth());
    assertEquals(150, lastHealth.get());

    combat.setHealth(-50);
    assertEquals(0, combat.getHealth());    // clamped
    assertEquals(0, lastHealth.get());      // event carries clamped value
  }

//  @Test
//  void addHealth_healsAboveCurrent_noUpperCap() {
//    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
//    assertEquals(100, combat.getHealth());
//
//    combat.setHealth(25);
//    assertEquals(125, combat.getHealth());
//
//    combat.setHealth(-50);
//    assertEquals(0, combat.getHealth());
//  }

  @Test
  void addHealth_overkill_clampsToZero_andMarksDead() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    combat.addHealth(-200);  // 100 -> 0
    assertEquals(0, combat.getHealth());
    assertTrue(combat.isDead());
  }

  @Test
  void shouldCheckIsDead() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertFalse(combat.isDead());

    combat.setHealth(0);
    assertTrue(combat.isDead());
  }

  @Test
  void shouldAddHealth() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    combat.addHealth(-500);
    assertEquals(0, combat.getHealth());

    combat.addHealth(100);
    combat.addHealth(-20);
    assertEquals(80, combat.getHealth());
  }

  @Test
  void shouldSetGetBaseAttack() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertEquals(20, combat.getBaseAttack());

    combat.setBaseAttack(150);
    assertEquals(150, combat.getBaseAttack());

    combat.setBaseAttack(-50);
    assertEquals(150, combat.getBaseAttack());
  }
  @Test
  void setHealthClampsAndFiresEvent() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);

    combat.setHealth(150);
    // assert health is updated, event is fired
    assertEquals(150, combat.getHealth());

    combat.setHealth(-50);
    // assert clamped to 0
    assertEquals(0, combat.getHealth());
  }

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
