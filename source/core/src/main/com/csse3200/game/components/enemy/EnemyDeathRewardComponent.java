package com.csse3200.game.components.enemy;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

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


        //1/4 chance of enemy adding ammo to the player
        Random rand = new Random();

        int chance = rand.nextInt(4);

        if (chance == 0) {

            Entity player = playerInventory.getEntity();
            AmmoStatsComponent playerAmmo = player.getComponent(AmmoStatsComponent.class);
            int currentAmmo = playerAmmo.getAmmo();
            playerAmmo.setAmmo(currentAmmo + 30);
            Sound attackSound = ServiceLocator.getResourceService()
                    .getAsset("sounds/ammo_replenished.mp3", Sound.class);
            attackSound.play();
            player.getEvents().trigger("ammo replenished");

        }
    }

    /**
     * Adds processor to the player inventory if inventory is present. Used for testing
     * without random variable
     */
    public void rewardGuaranteedReload() {
        if (playerInventory == null) {
            return;
        }
        playerInventory.addProcessor(rewardProcessor);


        Entity player = playerInventory.getEntity();
        AmmoStatsComponent playerAmmo = player.getComponent(AmmoStatsComponent.class);
        int currentAmmo = playerAmmo.getAmmo();
        playerAmmo.setAmmo(currentAmmo + 30);

        Sound attackSound = ServiceLocator.getResourceService()
                .getAsset("sounds/ammo_replenished.mp3", Sound.class);
        attackSound.play();
        player.getEvents().trigger("ammo replenished");
    }


}
