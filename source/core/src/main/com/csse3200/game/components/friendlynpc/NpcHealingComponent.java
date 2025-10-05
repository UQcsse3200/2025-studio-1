package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * NPC Healing Component
 * Restores player health when dialogue ends
 */
public class NpcHealingComponent extends Component {
    private final Entity player;
    private final int healAmount;

    /**
     * Creates an NPC healing component
     *
     * @param player the player entity to heal
     * @param healAmount the amount of health to restore
     */
    public NpcHealingComponent(Entity player, int healAmount) {
        this.player = player;
        this.healAmount = healAmount;
    }

    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("npcDialogueEnd", this::onDialogueEnd);
    }

    /**
     * Heals the player when dialogue ends
     */
    private void onDialogueEnd() {
        CombatStatsComponent combatStats = player.getComponent(CombatStatsComponent.class);
        int currentHealth = combatStats.getHealth();
        combatStats.setHealth(currentHealth + healAmount);
    }
}