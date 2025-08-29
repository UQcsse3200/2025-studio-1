package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnemyDeathRewardComponentTest {

    @Test
    @DisplayName("Awards gold to player when enemy dies")
    void awardsGoldOnDeath() {
        int startingGold = 10;
        int reward = 15;

        Entity player = new Entity().addComponent(new InventoryComponent(startingGold));
        player.create();
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);

        Entity enemy = new Entity()
                .addComponent(new CombatStatsComponent(5, 1))
                .addComponent(new EnemyDeathRewardComponent(reward, inventory));
        enemy.create();

        // Kill enemy
        enemy.getComponent(CombatStatsComponent.class).setHealth(0);

        assertEquals(startingGold + reward, inventory.getGold(), "Player should receive reward gold on enemy death");
    }

    @Test
    @DisplayName("Reward only applied once even if setHealth(0) called repeatedly")
    void rewardOnlyOnce() {
        int startingGold = 5;
        int reward = 20;

        Entity player = new Entity().addComponent(new InventoryComponent(startingGold));
        player.create();
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);

        Entity enemy = new Entity()
                .addComponent(new CombatStatsComponent(3, 1))
                .addComponent(new EnemyDeathRewardComponent(reward, inventory));
        enemy.create();

        CombatStatsComponent stats = enemy.getComponent(CombatStatsComponent.class);
        stats.setHealth(0); // first death triggers reward
        stats.setHealth(0); // should not trigger again

        assertEquals(startingGold + reward, inventory.getGold(), "Player should only be rewarded once");
    }

    @Test
    @DisplayName("Handles null player inventory without crashing")
    void handlesNullInventory() {
        Entity enemy = new Entity()
                .addComponent(new CombatStatsComponent(4, 1))
                .addComponent(new EnemyDeathRewardComponent(50, null));
        enemy.create();

        // Should not throw when enemy dies
        assertDoesNotThrow(() -> enemy.getComponent(CombatStatsComponent.class).setHealth(0));
    }
}

