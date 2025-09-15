package com.csse3200.game.components;

import com.csse3200.game.effects.Effect;
import com.csse3200.game.effects.RapidFireEffect;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

public class PowerupComponent extends Component {
    private final List<Effect> activeEffects = new ArrayList<>();
    private Entity equippedWeapon;

    public void setEquippedWeapon(Entity weapon) {
        this.equippedWeapon = weapon;
    }

    public Entity getEquippedWeapon() {
        return equippedWeapon;
    }

    public void addEffect(Effect effect) {
        if (effect instanceof RapidFireEffect rapidfire && equippedWeapon != null) {
            if (rapidfire.apply(equippedWeapon)) {
                activeEffects.add(rapidfire);
            }
        }
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();

        for (int i = activeEffects.size() - 1; i >= 0; i--) {
            Effect effect = activeEffects.get(i);

            if (effect instanceof RapidFireEffect rapidFireEffect) {
                rapidFireEffect.update(dt);
                if (!rapidFireEffect.isActive()) {
                    activeEffects.remove(i);
                }
            }
        }
    }
}





//package com.csse3200.game.components;
//
//import com.csse3200.game.components.Component;
//import com.csse3200.game.services.ServiceLocator;
//import com.csse3200.game.components.CombatStatsComponent;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class PowerupComponent extends Component {
//
//    private WeaponsStatsComponent playerStats;
//    private float duration;
//    private float elapsed = 0f;
//    private float originalCooldown;
//    private boolean active = false;
//
//    public PowerupComponent() {
//        this.active = false;
//    }
//
//    public void applyRapidFire(WeaponsStatsComponent stats, float duration) {
//        this.playerStats = stats;
//        this.duration = duration;
//        this.elapsed = 0f;
//        this.originalCooldown = stats.getCoolDown();
//
//        stats.setCoolDown(0f);
//        this.active = true;
//    }
//
//    @Override
//    public void update() {
//        float dt = ServiceLocator.getTimeSource().getDeltaTime();
//        elapsed += dt;
//
//        if (elapsed >= duration) {
//            if (playerStats != null) {
//                playerStats.setCoolDown(originalCooldown);
//            }
//            active = false;
//        }
//    }
//}
