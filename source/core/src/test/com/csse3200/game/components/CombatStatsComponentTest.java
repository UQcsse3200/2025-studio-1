package com.csse3200.game.components;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.entities.Entity;

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
      assertEquals(0, combat.getMaxHealth()); // ctor sets max from initial health, clamped
      assertTrue(combat.isDead());
    }
  }

  // ---=---
  @Nested
  @DisplayName("Objective: setHealth clamps to [0, maxHealth] and fires event")
  class SetHealthTests {
    @Test
    void positiveSet_aboveMax_clampsToMax_andFiresEvent() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.setHealth(150);
      assertEquals(100, combat.getHealth());  // capped at max=100
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

      combat.setHealth(100);
      int afterFirst = spy.cnt.get();
      combat.setHealth(100);
      assertTrue(spy.cnt.get() == afterFirst || spy.cnt.get() == afterFirst + 1,
              "Implementation may de-duplicate identical sets; allow either.");
    }

    @Test
    void loweringMaxHealth_capsCurrentHealth() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      // Simulate overheal attempt (should remain 100 due to cap)
      combat.addHealth(50);
      assertEquals(100, combat.getHealth());

      // Lower max to 60 -> health should clamp down
      AtomicInteger lastMax = new AtomicInteger(-1);
      Entity entity = new Entity().addComponent(combat);
      entity.getEvents().addListener("updateMaxHealth", lastMax::set);
      entity.create();

      combat.setMaxHealth(60);
      assertEquals(60, combat.getMaxHealth());
      // If implementation clamps health on reducing max, enforce it:
      // If you choose not to clamp here, relax the next line accordingly.
      assertEquals(60, combat.getHealth());
      assertEquals(60, lastMax.get());
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
  @DisplayName("Objective: addHealth respects cap and lower bound via setHealth")
  class AddHealthTests {
    @Test
    void heal_aboveMax_capsAtMax() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      HealthSpy spy = attachWithHealthSpy(combat);

      combat.addHealth(+25);
      assertEquals(100, combat.getHealth());   // capped to max
      assertEquals(100, spy.last.get());
      assertTrue(spy.cnt.get() >= 1);
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
    void reviveNotCovered_currentBehaviourNoAutoRevive() {
      CombatStatsComponent combat = new CombatStatsComponent(0, 5);
      combat.addHealth(+10);
      assertEquals(10, combat.getHealth());
      // isDead() semantics are tied to health==0; no extra assert here
    }

    @Test
    void isDead_whenHealthZero_andIgnoresFurtherDamage() {
      CombatStatsComponent combat = new CombatStatsComponent(100, 20);
      assertFalse(combat.isDead());

      combat.setHealth(0);
      assertTrue(combat.isDead());
      assertEquals(0, combat.getHealth());

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

      atk.setBaseAttack(11);
      assertEquals(11, atk.getBaseAttack());
    }

    @Test
    @DisplayName("Setter: idempotent sets do not change damage outcome")
    void setBaseAttack_idempotent_noEffectOnOutcome() {
      CombatStatsComponent def = new CombatStatsComponent(100, 0);
      CombatStatsComponent atk = new CombatStatsComponent(10, 15);

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

  // Legacy top-level tests kept for coverage parity
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
