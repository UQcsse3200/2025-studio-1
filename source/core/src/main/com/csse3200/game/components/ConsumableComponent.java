package com.csse3200.game.components;

import com.csse3200.game.effects.AreaEffect;
import com.csse3200.game.effects.Effect;

import java.util.ArrayList;

public class ConsumableComponent extends Component {
    // Currently unable to update consumable stats however subject to future changes
    private final ArrayList<Effect> effects;
    private final int duration;

    public ConsumableComponent(ArrayList<Effect> effects, int duration) {
        this.effects = effects;
        this.duration = duration;
    }

    public Effect getEffect(Class<? extends Effect> effectClass) {
        for (Effect e : effects) {
            if (e instanceof AreaEffect area) {
                for (Effect inner : area.getEffects()) {
                    if (effectClass.isInstance(inner)) {
                        return inner;
                    }
                }
            }
            if (effectClass.isInstance(e)) {
                return e;
            }
        }
        return null;
    }

    public boolean hasEffect(Class<? extends Effect> effectClass) {
        return getEffect(effectClass) != null;
    }

    public int getDuration() {
        return duration;
    }

    public boolean firesProjectile() {
        return entity.hasComponent(WeaponsStatsComponent.class);
    }
}
