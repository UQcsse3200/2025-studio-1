package com.csse3200.game.effects;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;

public class RapidFireEffect extends Effect {
    private final float duration;
    private float elapsed = 0f;
    private float originalCooldown = -1f;
    private WeaponsStatsComponent weaponStats;
    private boolean active = false;

    public RapidFireEffect(float duration) {
        this.duration = duration;
    }

    public boolean apply(Entity weapon) {
        weaponStats = weapon.getComponent(WeaponsStatsComponent.class);
        if (weaponStats == null || active) return false;

        originalCooldown = weaponStats.getCoolDown();
        weaponStats.setCoolDown(0f);
        active = true;
        elapsed = 0f;
        return true;
    }

    public void update(float dt) {
        if (!active) {
            return;
        }

        elapsed += dt;
        if (elapsed >= duration) {
            remove();
        }
    }

    private void remove() {
        if (weaponStats != null && originalCooldown >= 0f) {
            weaponStats.setCoolDown(originalCooldown);
        }
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
