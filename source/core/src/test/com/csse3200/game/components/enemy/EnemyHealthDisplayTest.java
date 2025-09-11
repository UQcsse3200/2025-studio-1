package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class EnemyHealthDisplayTest {
    @Test
    @DisplayName("Health bar display correct health value of enemy")
    public void correctHealthValue() {
        Entity enemy = new Entity().addComponent(new CombatStatsComponent(10));
        enemy.addComponent(new EnemyHealthDisplay());
        int enemyHealth = enemy.getComponent(CombatStatsComponent.class).getMaxHealth();
        int healthBarMaxHealth = enemy.getComponent(EnemyHealthDisplay.class).getCurrentHealthValue();
        assertEquals(enemyHealth, healthBarMaxHealth);
    }
}
