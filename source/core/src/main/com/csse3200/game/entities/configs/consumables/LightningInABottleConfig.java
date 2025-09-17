package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.AreaEffect;
import com.csse3200.game.effects.Effect;
import com.csse3200.game.effects.HealthEffect;

import java.util.ArrayList;

// Placeholder Consumable used for testing of Projectile Consumables
public class LightningInABottleConfig extends ProjectileConsumableConfig {
    public LightningInABottleConfig() {
        this.texturePath = "images/lightning_bottle.png";
        this.duration = 3;
        this.projectileDamage = 0;

        ArrayList<Effect> areaEffects = new ArrayList<>();
        areaEffects.add(new HealthEffect(100));
        this.effects = new ArrayList<>();
        this.effects.add(new AreaEffect(areaEffects, 2));
    }
}
