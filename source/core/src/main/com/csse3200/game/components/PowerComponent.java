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
            Entity entityPowerup = entities.get(i);
            TagComponent tag = entityPowerup.getComponent(TagComponent.class);
            if (tag != null) {
                if (tag.getTag().equals("rapidfire")) {
                    if (entityPowerup.getCenterPosition().dst(player.getCenterPosition()) < 1f) {
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
                        entityPowerup.dispose();
                    }
                }

                if (tag.getTag().equals("unlimitedammo")) {
                    if (entityPowerup.getCenterPosition().dst(player.getCenterPosition()) < 1f) {
                        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
                        Entity equippedWeapon = inventory.getCurrItem();

                        if (equippedWeapon != null) {
                            PowerupComponent powerup = player.getComponent(PowerupComponent.class);
                            powerup.setEquippedWeapon(equippedWeapon);
                            PlayerActions playerActions = player.getComponent(PlayerActions.class);
                            playerActions.getUnlimitedAmmoEffect().apply(equippedWeapon);
                            powerup.addEffect(playerActions.getUnlimitedAmmoEffect());
                        }
                        entityPowerup.dispose();
                    }
                }

                if (tag.getTag().equals("aimbot")) {
                    if (entityPowerup.getCenterPosition().dst(player.getCenterPosition()) < 1f) {
                        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
                        Entity equippedWeapon = inventory.getCurrItem();

                        if (equippedWeapon != null) {
                            PowerupComponent powerup = player.getComponent(PowerupComponent.class);
                            powerup.setEquippedWeapon(equippedWeapon);
                            PlayerActions playerActions = player.getComponent(PlayerActions.class);
                            playerActions.getAimbotEffect().apply(equippedWeapon);
                            powerup.addEffect(playerActions.getAimbotEffect());
                        }
                        entityPowerup.dispose();
                    }
                }

                if (tag.getTag().equals("doubleprocessors")) {
                    if (entityPowerup.getCenterPosition().dst(player.getCenterPosition()) < 1f) {
                        PowerupComponent powerup = player.getComponent(PowerupComponent.class);
                        if (powerup != null) {
                            DoubleProcessorsEffect effect = new DoubleProcessorsEffect(30f);
                            powerup.addEffect(effect);
                        }
                        entityPowerup.dispose();
                    }
                }
            }
        }
    }
}
