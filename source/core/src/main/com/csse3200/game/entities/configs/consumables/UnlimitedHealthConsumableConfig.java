package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.UnlimitedHealthEffect;

import java.util.ArrayList;

public class UnlimitedHealthConsumableConfig extends ConsumableConfig {

    public UnlimitedHealthConsumableConfig() {
        this.effects = new ArrayList<>();
        this.effects.add(new UnlimitedHealthEffect(20f)); // 20 seconds of unlimited health
        this.texturePath = "images/tree.png"; // Use existing image for now
        this.isProjectile = false;
    }
}
