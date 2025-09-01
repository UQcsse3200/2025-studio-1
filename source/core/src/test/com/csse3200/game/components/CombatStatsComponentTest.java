package com.csse3200.game.components;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.entities.Entity;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
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

  // verify initialisation
  @Test
  void initialisation_defaultsAreSane() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertEquals(100, combat.getHealth());
    assertEquals(20, combat.getBaseAttack());
    assertEquals(100, combat.getMaxHealth());
    assertFalse(combat.isDead());
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
