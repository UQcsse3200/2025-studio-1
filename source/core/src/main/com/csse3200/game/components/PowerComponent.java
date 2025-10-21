package com.csse3200.game.components;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.effects.DoubleProcessorsEffect;
import com.csse3200.game.effects.Effect;
import com.csse3200.game.effects.RapidFireEffect;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.consumables.RapidFireConsumableConfig;
import com.csse3200.game.services.ServiceLocator;

public class PowerComponent extends Component {
    Entity player;

    public PowerComponent(Entity player) {
        this.player = player;
    }

    @Override
    public void update() {
        var entities = ServiceLocator.getEntityService().getEntities();
        for (int i = 0; i < entities.size; i++) {
            Entity entityPowerUp = entities.get(i);
            TagComponent tag = entityPowerUp.getComponent(TagComponent.class);
            if (tag != null && entityPowerUp.getCenterPosition().dst(player.getCenterPosition()) < 1f) {
                switch (tag.getTag()) {
                    case "rapidfire":
                        applyRapidFire();
                        entityPowerUp.dispose();
                        break;
                    case "unlimitedammo":
                        applyUnlimitedAmmo();
                        entityPowerUp.dispose();
                        break;
                    case "aimbot":
                        applyAimBot();
                        entityPowerUp.dispose();
                        break;
                    case "doubleprocessors":
                        applyDoubleProcessors();
                        entityPowerUp.dispose();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Applies the rapid-fire effect to the player's currently equipped weapon.
     * This method assumes the player is already in range of the power-up entity.
     */
    private void applyRapidFire() {
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        Entity equippedWeapon = inventory.getCurrItem();

        if (equippedWeapon != null) {
            RapidFireConsumableConfig config = new RapidFireConsumableConfig();
            for (Effect e : config.effects) {
                if (e instanceof RapidFireEffect rapidFireEffect) {
                    player.getComponent(PowerupComponent.class).setEquippedWeapon(equippedWeapon);
                    player.getComponent(PowerupComponent.class).addEffect(rapidFireEffect);
                }
            }
        }
    }

    /**
     * Applies the unlimited ammo effect to the player's currently equipped weapon.
     * This method assumes the player is already in range of the power-up entity.
     */
    private void applyUnlimitedAmmo() {
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        Entity equippedWeapon = inventory.getCurrItem();

        if (equippedWeapon != null) {
            PowerupComponent powerUp = player.getComponent(PowerupComponent.class);
            powerUp.setEquippedWeapon(equippedWeapon);
            PlayerActions playerActions = player.getComponent(PlayerActions.class);
            playerActions.getUnlimitedAmmoEffect().apply(equippedWeapon);
            powerUp.addEffect(playerActions.getUnlimitedAmmoEffect());
        }
    }

    /**
     * Applies the aimbot effect to the player's currently equipped weapon.
     * This method assumes the player is already in range of the power-up entity.
     */
    private void applyAimBot() {
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        Entity equippedWeapon = inventory.getCurrItem();

        if (equippedWeapon != null) {
            PowerupComponent powerUp = player.getComponent(PowerupComponent.class);
            powerUp.setEquippedWeapon(equippedWeapon);
            PlayerActions playerActions = player.getComponent(PlayerActions.class);
            playerActions.getAimbotEffect().apply(equippedWeapon);
            powerUp.addEffect(playerActions.getAimbotEffect());
        }
    }

    /**
     * Applies the double processors effect to the player.
     * This method assumes the player is already in range of the power-up entity.
     */
    private void applyDoubleProcessors() {
        PowerupComponent powerUp = player.getComponent(PowerupComponent.class);
        if (powerUp != null) {
            DoubleProcessorsEffect effect = new DoubleProcessorsEffect(30f);
            powerUp.addEffect(effect);
        }
    }
}