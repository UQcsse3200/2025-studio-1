package com.csse3200.game.entities.configs.benches;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

public class HealthBenchConfig extends BenchConfig {
    private boolean upgradeUsed;

    /**
     * Initialise HealthBenchConfig
     */
    public HealthBenchConfig() {
        benchType = ItemTypes.HEALTH_BENCH;
        texturePath = "images/healthBench.png";
        price = 1000;
        promptText = "Press E to upgrade health for " + price;
        upgradeUsed = false;
    }

    @Override
    public void upgrade(boolean playerNear, Entity player, Label buyPrompt) {
        //check the player and the upgrade hasnt been used yet
        if (playerNear && player != null && !upgradeUsed
                && player.getComponent(InventoryComponent.class).hasProcessor(price)) {

            player.getComponent(CombatStatsComponent.class).upgradeMaxHealth();
            subtractPrice(player);
            upgradeUsed = true;

            buyPrompt.setText("Upgrade Successful!");
            playUpgradeSound();

        } else if (upgradeUsed) { //If upgrade has been used, tell player
            buyPrompt.setText("Upgrade Already Used!");
        } else {
            assert player != null;
            //Tell player if they don't have funds
            if (!player.getComponent(InventoryComponent.class).hasProcessor(price)) {
                buyPrompt.setText("You are broke! Fries in the bag!");
            }
        }
    }
}
