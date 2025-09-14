package com.csse3200.game.entities.configs.benches;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ComputerBenchConfig extends BenchConfig {
    public ComputerBenchConfig() {
        benchType = ItemTypes.COMPUTER_BENCH;
        texturePath = "images/computerBench.png";
        promptText  = "Press E for upgrade";
    }

    @Override
    public void upgrade(boolean playerNear, Entity player, Label buyPrompt)  {
        if (playerNear && player != null) {

            Entity currItem = player.getComponent(InventoryComponent.class).getCurrItem();

            WeaponsStatsComponent currItemStats = currItem.getComponent(WeaponsStatsComponent.class);
            if (currItemStats != null) {
                currItemStats.upgrade();
                buyPrompt.setText("Item has been upgraded");
            } else {
                buyPrompt.setText("Item is already fully upgraded!");
            }
        }
    }
}
