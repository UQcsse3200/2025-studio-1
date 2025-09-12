package com.csse3200.game.components.enemy;

import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;

import java.util.Random;

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

    /**
     * Adds processor to the player inventory if inventory is present.
     */
    private void onDeath() {
        if (playerInventory == null) {
            return;
        }
        playerInventory.addProcessor(rewardProcessor);



        //1/3 chance of enemy adding ammo to the player
        Random rand = new Random();

        int chance = rand.nextInt(3);

        if (chance == 0) {

            Entity player = playerInventory.getEntity();
            AmmoStatsComponent playerAmmo = player.getComponent(AmmoStatsComponent.class);
            int currentAmmo = playerAmmo.getAmmo();
            playerAmmo.setAmmo(currentAmmo + 200);
            player.getEvents().trigger("ammo replenished");

        }
    }
}
