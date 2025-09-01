package com.csse3200.game.components;

import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.NPCFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(GameExtension.class)
class CombatStatsComponentTest {
  @Test
  void shouldSetGetHealth() {
    CombatStatsComponent combat = new CombatStatsComponent(100, 20);
    assertEquals(100, combat.getHealth());

    combat.setHealth(150);
    assertEquals(150, combat.getHealth());

    combat.setHealth(-50);
    assertEquals(0, combat.getHealth());
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

  @Test
  void deathHitRemovesEntity() {
    //Tests if killing an entity removes it from the game
    Entity victim = new Entity();
    victim.addComponent(new CombatStatsComponent(10, 10));
    GameArea area = new ForestGameArea(new TerrainFactory(new CameraComponent()));
    ServiceLocator.registerGameArea(area);
    ServiceLocator.registerEntityService(new EntityService());
    area.spawnEntity(victim);
    victim.getComponent(CombatStatsComponent.class).hit(new CombatStatsComponent(0, 10));
    assertTrue(victim.getComponent(CombatStatsComponent.class).isDead());
    assertEquals(new ArrayList<>(), area.getEntities());
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
