package com.csse3200.game.entities.configs.benches;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

public class SpeedBenchConfig extends BenchConfig {
    private boolean upgradeUsed;

    /**
     * Initialise SpeedBenchConfig
     */
    public SpeedBenchConfig() {
        benchType = ItemTypes.SPEED_BENCH;
        texturePath = "images/speedBench.png";
        price = 1000;
        promptText = "Press E to boost speed for " + price;
        upgradeUsed = false;
    }

    @Override
    public void upgrade(boolean playerNear, Entity player, Label buyPrompt) {

        //Check player and that upgrade hasn't been used already
        if (playerNear && player != null && !upgradeUsed
                && player.getComponent(InventoryComponent.class).hasProcessor(price)) {

            subtractPrice(player);
            player.getComponent(PlayerActions.class).upgradeSpeed();
            upgradeUsed = true;
            buyPrompt.setText("Upgrade Successful!");
            playUpgradeSound();

        } else if (upgradeUsed) { //Tell user if upgrade has been used
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
