package com.csse3200.game.entities.configs.benches;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.services.ServiceLocator;

public abstract class BenchConfig {
    public ItemTypes benchType = ItemTypes.BENCH;
    public String texturePath = "";
    public String promptText = "";
    public int price = 0;

    /**
     * Subtracts the price of the station from the player
     *
     * @param player the player
     */
    public void subtractPrice(Entity player) {
        player.getComponent(InventoryComponent.class)
                .setProcessor(player.getComponent(InventoryComponent.class).getProcessor() - price);
    }

    /**
     * Gets the price of the station
     *
     * @return the price of the station
     */
    public int getPrice() {
        return this.price;
    }

    /**
     * Plays the upgrade sound
     */
    public void playUpgradeSound() {
        Sound upgradeSound = ServiceLocator.getResourceService()
                .getAsset("sounds/upgradeSound.mp3", Sound.class);
        upgradeSound.play();
    }

    /**
     * Triggers the station's upgrade
     *
     * @param playerNear is the player near
     * @param player     the player Entity
     * @param buyPrompt  the buyPrompt for the station
     */
    public abstract void upgrade(boolean playerNear, Entity player, Label buyPrompt);

}
