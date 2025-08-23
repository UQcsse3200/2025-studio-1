package com.csse3200.game.components;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerComponent;
import com.csse3200.game.components.enemy.EnemyDeathRewardComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;

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
  void enemyShouldBeDeadWhenHealthZero() {
    CombatStatsComponent ghost = new CombatStatsComponent(50, 10);
    assertFalse(ghost.isDead());
    ghost.addHealth(-50);
    assertEquals(0, ghost.getHealth());
    assertTrue(ghost.isDead());
    // Overkill
    ghost.addHealth(-100);
    assertEquals(0, ghost.getHealth());
    assertTrue(ghost.isDead());
  }

  @Test
  void playerReceivesGoldWhenEnemyDies() {
    // Setup EntityService
    EntityService entityService = EntityService.getInstance();
    // Create player entity with PlayerComponent and InventoryComponent
    Entity player = new Entity();
    player.addComponent(new PlayerComponent());
    InventoryComponent inventory = new InventoryComponent(0);
    player.addComponent(inventory);
    entityService.register(player);

    // Create ghost entity with CombatStatsComponent and GhostDeathRewardComponent
    Entity ghost = new Entity();
    CombatStatsComponent ghostStats = new CombatStatsComponent(10, 0);
    ghost.addComponent(ghostStats);
    ghost.addComponent(new EnemyDeathRewardComponent(15)); // Reward: 15 gold
    entityService.register(ghost);

    // Damage ghost until it dies
    ghostStats.addHealth(-10);

    // Assert player received gold
    assertEquals(15, inventory.getGold());
  }
}
