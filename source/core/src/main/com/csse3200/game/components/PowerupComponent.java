package com.csse3200.game.components;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.CombatStatsComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PowerupComponent extends Component {

    private CombatStatsComponent playerStats;
    private float duration;
    private float elapsed = 0f;
    private float originalCooldown;
    private boolean active = false;

    public PowerupComponent() {
        this.active = false;
    }

    public void applyRapidFire(CombatStatsComponent stats, float duration) {
        this.playerStats = stats;
        this.duration = duration;
        this.elapsed = 0f;
        this.originalCooldown = stats.getCoolDown();

        stats.setCoolDown(0f);
        this.active = true;
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        elapsed += dt;

        if (elapsed >= duration) {
            if (playerStats != null) {
                playerStats.setCoolDown(originalCooldown);
            }
            active = false;
        }
    }
}
