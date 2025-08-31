package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;

/**
 * Awards the player processor when the enemy dies.
 */
public class EnemyDeathRewardComponent extends Component {
    private final int rewardProcessor;
    private final InventoryComponent playerInventory;

    public EnemyDeathRewardComponent(int rewardProcessor, InventoryComponent playerInventory) {
        this.rewardProcessor = rewardProcessor;
        this.playerInventory = playerInventory;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("death", this::onDeath);
    }

    private void onDeath() {
        if (playerInventory == null) {
            return;
        }
        playerInventory.addProcessor(rewardProcessor);
    }
}
