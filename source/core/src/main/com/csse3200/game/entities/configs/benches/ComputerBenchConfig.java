package com.csse3200.game.entities.configs.benches;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.services.ServiceLocator;

public class ComputerBenchConfig extends BenchConfig {

    /**
     * Initialise ComputerBenchConfig
     */
    public ComputerBenchConfig() {
        benchType = ItemTypes.COMPUTER_BENCH;
        texturePath = "images/computerBench.png";
        price = 1000;
        promptText = "Press E to upgrade weapon for " + price;
    }

    @Override
    public void upgrade(boolean playerNear, Entity player, Label buyPrompt) {
        //Check the player is near
        if (playerNear && player != null) {
            Entity currItem = player.getComponent(InventoryComponent.class).getCurrItem();
            if (currItem != null) {
                WeaponsStatsComponent currItemStats = currItem.getComponent(WeaponsStatsComponent.class);
                //Check it's a weapon
                if (currItemStats != null) {
                    //Check funds
                    if (player.getComponent(InventoryComponent.class).hasProcessor(price) && !currItemStats.isMaxUpgraded()) {
                        currItemStats.upgrade();
                        subtractPrice(player);
                        buyPrompt.setText("Item has been upgraded");
                        playUpgradeSound();
                    } else if (currItemStats.isMaxUpgraded()) { //Check the gun can be upgraded
                        buyPrompt.setText("Weapon is fully upgraded already!");
                    } else {
                        buyPrompt.setText("You are broke! Fries in the bag!");
                    }
                } else {
                    buyPrompt.setText("Not a weapon!");
                }
            }
        }
    }
}
