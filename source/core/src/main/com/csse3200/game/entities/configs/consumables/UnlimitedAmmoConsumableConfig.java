package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.UnlimitedAmmoEffect;

import java.util.ArrayList;

public class UnlimitedAmmoConsumableConfig extends ConsumableConfig {

    public UnlimitedAmmoConsumableConfig() {
        this.effects = new ArrayList<>();
        this.effects.add(new UnlimitedAmmoEffect(20f));
        this.texturePath = "images/rapidfirepowerup.png";
        this.isProjectile = false;
    }
}
