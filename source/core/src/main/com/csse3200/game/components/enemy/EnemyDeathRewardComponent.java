package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;

/**
 * Awards the player gold when the ghost dies.
 */
public class EnemyDeathRewardComponent extends Component {
    private final int rewardGold;
    private final InventoryComponent playerInventory;

    public EnemyDeathRewardComponent(int rewardGold, InventoryComponent playerInventory) {
        this.rewardGold = rewardGold;
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
        playerInventory.addGold(rewardGold);
    }
}
