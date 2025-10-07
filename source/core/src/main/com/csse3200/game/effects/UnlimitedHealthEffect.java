package com.csse3200.game.effects;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;

public class UnlimitedHealthEffect extends Effect {
    private final float duration;
    private float elapsed = 0f;
    private CombatStatsComponent combatStats;
    private boolean active = false;
    private int maxHealth;

    public UnlimitedHealthEffect(float duration) {
        this.duration = duration;
    }

    public boolean apply(Entity player) {
        combatStats = player.getComponent(CombatStatsComponent.class);
        if (combatStats == null || active) return false;

        maxHealth = combatStats.getMaxHealth();
        active = true;
        elapsed = 0f;
        return true;
    }

    public void update(float dt) {
        if (!active) return;

        // Keep health at maximum constantly
        if (combatStats != null) {
            combatStats.setHealth(maxHealth);
        }

        elapsed += dt;
        if (elapsed >= duration) {
            remove();
        }
    }

    private void remove() {
        active = false;
        // Health naturally stays where it is when effect ends
    }

    public boolean isActive() {
        return active;
    }
}
