package com.csse3200.game.components;

import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(GameExtension.class)
class CombatStatsComponentTest {
  CombatStatsComponent combat;
  CombatStatsComponent enemy;

  @BeforeEach
  void setup() {
    combat = new CombatStatsComponent(100, 20);
    enemy = new CombatStatsComponent(100, 20);
  }

  @Test
  void shouldSetGetHealth() {
    assertEquals(100, combat.getHealth());

    combat.setHealth(150);
    assertEquals(150, combat.getHealth());

    combat.setHealth(-50);
    assertEquals(0, combat.getHealth());
  }

  @Test
  void shouldCheckIsDead() {
    assertFalse(combat.isDead());

    combat.setHealth(0);
    assertTrue(combat.isDead());
  }

  @Test
  void shouldAddHealth() {
    combat.addHealth(-500);
    assertEquals(0, combat.getHealth());

    combat.addHealth(100);
    combat.addHealth(-20);
    assertEquals(80, combat.getHealth());
  }

  @Test
  void shouldSetGetBaseAttack() {
    assertEquals(20, combat.getBaseAttack());

    combat.setBaseAttack(150);
    assertEquals(150, combat.getBaseAttack());

    combat.setBaseAttack(-50);
    assertEquals(150, combat.getBaseAttack());
  }

  @Test
  void enemyShouldBeDeadWhenHealthZero() {
    assertFalse(enemy.isDead());
    enemy.addHealth(-100);
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
    assertEquals(0, combat.getCoolDown());
    combat.setCoolDown(100);
    assertEquals(100, combat.getCoolDown());
    combat.setCoolDown(-100);
    assertEquals(0, combat.getCoolDown());
  }

  @Test
  void deathHitRemovesEntity() {
    //Tests if killing an entity removes it from the game
    Entity victim = new Entity();
    victim.addComponent(new CombatStatsComponent(10, 10));
    GameArea area = new ForestGameArea(new TerrainFactory(new CameraComponent()));
    ServiceLocator.registerGameArea(area);
    ServiceLocator.registerEntityService(new EntityService());
    area.spawnEntity(victim);
    // Add a listener to simulate death removal as in the real game
    victim.getEvents().addListener("death", () -> area.removeEntity(victim));
    victim.getComponent(CombatStatsComponent.class).hit(new CombatStatsComponent(0, 10));
    assertTrue(victim.getComponent(CombatStatsComponent.class).isDead());
    assertEquals(new ArrayList<>(), area.getEntities());

  }

  @Test
  void shouldTakeDirectDamage() {
    combat.hit(20);
    assertEquals(80, combat.getHealth()); 
  }

  @Test
  void shouldTakeDamageFromAttacker(){
    combat.hit(enemy);
    assertEquals(80, combat.getHealth());
  }

  @Test
  void shouldSetGetMaxHealth() {
    assertEquals(100, combat.getMaxHealth());
    combat.setMaxHealth(200);
    assertEquals(200, combat.getMaxHealth());
  }

  @Test
  void nullAttackerDoesNothing() {
    combat.hit(null);
    assertEquals(100, combat.getHealth());
  }

  @Test
  void applyDamageToDeadDoesNothing() {
    combat.hit(100);
    assertTrue(combat.isDead());
    assertEquals(0, combat.getHealth());
    combat.hit(1000);
    assertEquals(0, combat.getHealth());
  }

  @Test
  void testAmmo() {

    combat.setAmmo(0);
    Assert.assertEquals(combat.getAmmo(), 0);

    combat.setAmmo(100);
    Assert.assertEquals(combat.getAmmo(), 100);
  }

  @AfterEach
  void cleanup() {
    ServiceLocator.clear();
  }
}
