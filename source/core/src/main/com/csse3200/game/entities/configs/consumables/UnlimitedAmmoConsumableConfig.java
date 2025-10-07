package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.effects.RapidFireEffect;
import com.csse3200.game.effects.UnlimitedAmmoEffect;

import java.util.ArrayList;

public class UnlimitedAmmoConsumableConfig extends ConsumableConfig {

    public UnlimitedAmmoConsumableConfig(PlayerActions playerActions) {
        this.effects = new ArrayList<>();
        this.effects.add(new UnlimitedAmmoEffect(20f, playerActions));
        this.texturePath = "images/rapidfirepowerup.png";
        this.isProjectile = false;
    }
}
