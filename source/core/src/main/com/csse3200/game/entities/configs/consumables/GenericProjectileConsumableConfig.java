package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.AreaEffect;
import com.csse3200.game.effects.Effect;
import com.csse3200.game.effects.HealthEffect;

import java.util.ArrayList;

// Placeholder Consumable used for testing of Projectile Consumables
public class GenericProjectileConsumableConfig extends ProjectileConsumableConfig {
    public GenericProjectileConsumableConfig() {
        this.texturePath = "images/round.png";
        this.duration = 3;
        this.projectileDamage = 0;

        ArrayList<Effect> areaEffects = new ArrayList<>();
        areaEffects.add(new HealthEffect(1000000));
        this.effects = new ArrayList<>();
        this.effects.add(new AreaEffect(areaEffects, 2));
    }
}
