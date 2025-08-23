package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;

/**
 * Awards the player gold when the ghost dies.
 */
public class EnemyDeathRewardComponent extends Component {
    private final int rewardGold;

    public EnemyDeathRewardComponent(int rewardGold) {
        this.rewardGold = rewardGold;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("death", this::onDeath);
    }

    private void onDeath() {
        // Find the player entity
        EntityService entityService = EntityService.getInstance();
        Entity player = entityService.getPlayer();
        if (player != null) {
            InventoryComponent inventory = player.getComponent(InventoryComponent.class);
            if (inventory != null) {
                inventory.addGold(rewardGold);
            }
        }
    }
}
