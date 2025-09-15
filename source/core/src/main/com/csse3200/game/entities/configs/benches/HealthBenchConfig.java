package com.csse3200.game.entities.configs.benches;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class HealthBenchConfig extends BenchConfig {
    private boolean upgradeUsed;

    public HealthBenchConfig() {
        benchType = ItemTypes.HEALTH_BENCH;
        texturePath = "images/healthBench.png";
        price = 1000;
        promptText = "Press E to upgrade health for " + price;
        upgradeUsed = false;
    }

    @Override
    public void upgrade(boolean playerNear, Entity player, Label buyPrompt) {

        if (playerNear && player != null && !upgradeUsed
                && player.getComponent(InventoryComponent.class).hasProcessor(1000)) {
            System.out.println("CURR MAX HEALTH: " + player.getComponent(CombatStatsComponent.class).getMaxHealth());
            player.getComponent(CombatStatsComponent.class).upgradeMaxHealth();
            subtractPrice(player);
            upgradeUsed = true;
            buyPrompt.setText("Upgrade Successful!");
        } else if (upgradeUsed) {
            buyPrompt.setText("Upgrade Already Used!");
        } else {
            assert player != null;
            if (!player.getComponent(InventoryComponent.class).hasProcessor(1000)) {
                buyPrompt.setText("You are broke! Fries in the bag!");
            }
        }
    }
}
