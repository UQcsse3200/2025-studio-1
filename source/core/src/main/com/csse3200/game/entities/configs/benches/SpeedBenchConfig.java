package com.csse3200.game.entities.configs.benches;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class SpeedBenchConfig extends BenchConfig {
    private boolean upgradeUsed;

    /**
     * Initialise SpeedBenchConfig
     */
    public SpeedBenchConfig() {
        benchType = ItemTypes.HEALTH_BENCH;
        texturePath = "images/speedBench.png";
        price = 1000;
        promptText = "Press E to boost speed for " + price;
        upgradeUsed = false;
    }

    @Override
    public void upgrade(boolean playerNear, Entity player, Label buyPrompt) {

        if (playerNear && player != null && !upgradeUsed
                && player.getComponent(InventoryComponent.class).hasProcessor(price)) {
            player.getComponent(CombatStatsComponent.class).upgradeMaxHealth();
            subtractPrice(player);
            player.getComponent(PlayerActions.class).upgradeSpeed();
            upgradeUsed = true;
            buyPrompt.setText("Upgrade Successful!");
        } else if (upgradeUsed) {
            buyPrompt.setText("Upgrade Already Used!");
        } else {
            assert player != null;
            if (!player.getComponent(InventoryComponent.class).hasProcessor(price)) {
                buyPrompt.setText("You are broke! Fries in the bag!");
            }
        }
    }
}
