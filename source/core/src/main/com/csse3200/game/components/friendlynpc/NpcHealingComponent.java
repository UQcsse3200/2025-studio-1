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
    // Frequency / cooldown
    private long cooldownMillis = 0; // 0 = no cooldown
    private long lastTriggerTime = 0L;

    /**
     * Creates an NPC healing component
     *
     * @param player the player entity to heal
     * @param healAmount the amount of health to restore
     */
    public NpcHealingComponent(Entity player, int healAmount) {
        this.player = player;
        this.healAmount = Math.max(0, healAmount);
    }

    /**
     * Sets a cooldown between consecutive heals.
     *
     * @param ms cooldown in milliseconds (0 = disabled)
     * @return this component for chaining
     */
    public NpcHealingComponent setCooldownMillis(long ms) {
        this.cooldownMillis = Math.max(0, ms);
        return this;
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
        if (player == null) return;

        CombatStatsComponent combatStats = player.getComponent(CombatStatsComponent.class);
        if (combatStats == null) return;

        long now = System.currentTimeMillis();
        if (cooldownMillis > 0 && (now - lastTriggerTime) < cooldownMillis) {
            return;
        }

        if (combatStats.isDead()) {
            return;
        }
        combatStats.addHealth(healAmount);
        lastTriggerTime = now;
    }
}