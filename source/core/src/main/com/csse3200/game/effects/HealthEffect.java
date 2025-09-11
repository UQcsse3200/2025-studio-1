package com.csse3200.game.effects;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;

public class HealthEffect extends Effect {
    private int damage;

    public HealthEffect(int damage) {
        this.damage = damage;
    }

    @Override
    public boolean apply(Entity entity) {
        if (!entity.hasComponent(CombatStatsComponent.class)) {
            return false;
        }

        CombatStatsComponent combatStats = entity.getComponent(CombatStatsComponent.class);
        combatStats.setHealth(combatStats.getHealth() + damage);
        return true;
    }
}
