package com.csse3200.game.components;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.CombatStatsComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PowerupComponent extends Component {

    private WeaponsStatsComponent playerStats;
    private float duration;
    private float elapsed = 0f;

    // Rapid Fire variables
    private float originalCooldown;
    private boolean rapidFireActive = false;

    // Damage Boost variables
    private int originalAttack;
    private boolean damageBoostActive = false;

    public PowerupComponent() {
        this.rapidFireActive = false;
        this.damageBoostActive = false;
    }

    // Existing rapid fire method
    public void applyRapidFire(WeaponsStatsComponent stats, float duration) {
        this.playerStats = stats;
        this.duration = duration;
        this.elapsed = 0f;
        this.originalCooldown = stats.getCoolDown();

        stats.setCoolDown(0f);
        this.rapidFireActive = true;
    }

    // NEW: Damage boost method
    public void applyDamageBoost(WeaponsStatsComponent stats, int boostAmount, float duration) {
        this.playerStats = stats;
        this.duration = duration;
        this.elapsed = 0f;
        this.originalAttack = stats.getBaseAttack();

        // Apply the damage boost
        stats.setBaseAttack(originalAttack + boostAmount);
        this.damageBoostActive = true;
    }

    @Override
    public void update() {
        if (!rapidFireActive && !damageBoostActive) {
            return; // No active powerups
        }

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        elapsed += dt;

        // Check if duration expired
        if (elapsed >= duration) {
            if (playerStats != null) {
                // Restore rapid fire
                if (rapidFireActive) {
                    playerStats.setCoolDown(originalCooldown);
                    rapidFireActive = false;
                }

                // Restore damage boost
                if (damageBoostActive) {
                    playerStats.setBaseAttack(originalAttack);
                    damageBoostActive = false;
                }
            }
        }
    }

    // Helper methods for UI or checking status
    public boolean isDamageBoostActive() {
        return damageBoostActive;
    }

    public boolean isRapidFireActive() {
        return rapidFireActive;
    }

    public float getRemainingDuration() {
        return Math.max(0, duration - elapsed);
    }
}
