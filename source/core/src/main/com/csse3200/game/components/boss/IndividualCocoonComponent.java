package com.csse3200.game.components.boss;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;

/**
 * Individual Cocoon Component - Handles behavior for individual cocoon entities
 * Cocoons are regular enemies that can be damaged and destroyed normally
 * When destroyed, they notify the Boss defense system via death events
 */
public class IndividualCocoonComponent extends Component {

    private boolean isDestroyed = false;

    @Override
    public void create() {
        super.create();

        // Listen for cocoon death events
        entity.getEvents().addListener("death", this::onCocoonDeath);

        System.out.println("White cocoon created and ready!");
    }

    @Override
    public void update() {
        super.update();

        // Check cocoon's health
        CombatStatsComponent combatStats = entity.getComponent(CombatStatsComponent.class);
        if (combatStats != null && combatStats.getHealth() <= 0 && !isDestroyed) {
            onCocoonDeath();
        }
    }

    /**
     * Handler for when cocoon dies
     */
    private void onCocoonDeath() {
        if (isDestroyed) return;

        isDestroyed = true;

        System.out.println("White cocoon destroyed!");

        // The death event is automatically triggered by CombatStatsComponent
        // and caught by the CocoonSpawnerComponent through the death listener
    }

    /**
     * Check if cocoon has been destroyed
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }
}