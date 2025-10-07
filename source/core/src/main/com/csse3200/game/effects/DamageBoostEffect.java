package com.csse3200.game.effects;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;

public class DamageBoostEffect extends Effect {
    private final float duration;
    private final float multiplier;
    private float elapsed = 0f;
    private float originalMultiplier = -1f;
    private WeaponsStatsComponent weaponStats;
    private boolean active = false;

    public DamageBoostEffect(float duration, float multiplier) {
        this.duration = duration;
        this.multiplier = multiplier;
    }

    public boolean apply(Entity weapon) {
        weaponStats = weapon.getComponent(WeaponsStatsComponent.class);
        if (weaponStats == null || active) return false;

        originalMultiplier = weaponStats.getDamageMultiplier();
        System.out.println("DEBUG: Original damage multiplier: " + originalMultiplier);

        weaponStats.setDamageMultiplier(originalMultiplier * multiplier);
        System.out.println("DEBUG: New damage multiplier: " + weaponStats.getDamageMultiplier());

        active = true;
        elapsed = 0f;
        return true;
    }


    public void update(float dt) {
        if (!active) return;

        elapsed += dt;
        if (elapsed >= duration) {
            remove();
        }
    }

    private void remove() {
        if (weaponStats != null && originalMultiplier >= 0f) {
            weaponStats.setDamageMultiplier(originalMultiplier);
        }
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
