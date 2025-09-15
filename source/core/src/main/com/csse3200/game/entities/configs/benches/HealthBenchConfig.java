package com.csse3200.game.entities.configs.benches;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class HealthBenchConfig extends BenchConfig {
    private boolean upgradeUsed;
    public HealthBenchConfig() {
        benchType = ItemTypes.HEALTH_BENCH;
        texturePath = "images/healthBench.png";
        promptText = "Press E for health upgrade";
        upgradeUsed = false;
    }

    @Override
    public void upgrade(boolean playerNear, Entity player, Label buyPrompt) {
        if (playerNear && player != null) {
            System.out.println("CURR MAX HEALTH: " + player.getComponent(CombatStatsComponent.class).getMaxHealth());
            player.getComponent(CombatStatsComponent.class).upgradeMaxHealth();
        }
    }
}
