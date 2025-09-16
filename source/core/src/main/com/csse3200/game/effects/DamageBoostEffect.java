package com.csse3200.game.effects;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;

public class DamageBoostEffect extends Effect {
    private final float duration;
    private final int boostAmount;
    private float elapsed = 0f;
    private int originalAttack = -1;
    private WeaponsStatsComponent weaponStats;
    private boolean active = false;

    public DamageBoostEffect(float duration, int boostAmount) {
        System.out.println("Creating DamageBoostEffect with duration=" + duration + ", boost=" + boostAmount);
        this.duration = duration;
        this.boostAmount = boostAmount;
        System.out.println("DamageBoostEffect created successfully");
    }


    @Override
    public boolean apply(Entity weapon) {
        weaponStats = weapon.getComponent(WeaponsStatsComponent.class);
        if (weaponStats == null || active) return false;

        originalAttack = weaponStats.getBaseAttack();
        weaponStats.setBaseAttack(originalAttack + boostAmount);
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
        if (weaponStats != null && originalAttack >= 0) {
            weaponStats.setBaseAttack(originalAttack);
        }
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
