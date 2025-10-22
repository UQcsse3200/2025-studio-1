package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * NPC Attack Boost Component
 * Temporarily increases player's attack power when dialogue ends
 */
public class NpcAttackBoostComponent extends Component {
    private final Entity player;
    private final int boostAmount;
    private final long durationMillis;
    private long cooldownMillis = 0;
    private long lastTriggerTime = 0L;

    /**
     * Creates an NPC attack boost component
     *
     * @param player the player entity to boost
     * @param boostAmount the amount of attack to add
     * @param durationMillis how long the boost lasts (in milliseconds)
     */
    public NpcAttackBoostComponent(Entity player, int boostAmount, long durationMillis) {
        this.player = player;
        this.boostAmount = Math.max(0, boostAmount);
        this.durationMillis = Math.max(0, durationMillis);
    }

    /**
     * Sets a cooldown between consecutive boosts.
     *
     * @param ms cooldown in milliseconds (0 = disabled)
     * @return this component for chaining
     */
    public NpcAttackBoostComponent setCooldownMillis(long ms) {
        this.cooldownMillis = Math.max(0, ms);
        return this;
    }

    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("npcDialogueEnd", this::onDialogueEnd);
    }

    /**
     * Boosts the player's attack when dialogue ends
     */
    private void onDialogueEnd() {
        if (player == null) return;

        WeaponsStatsComponent weaponStats = player.getComponent(WeaponsStatsComponent.class);
        if (weaponStats == null) return;

        long now = System.currentTimeMillis();
        if (cooldownMillis > 0 && (now - lastTriggerTime) < cooldownMillis) {
            return;
        }

        // Apply attack boost
        int currentAttack = weaponStats.getBaseAttack();
        weaponStats.setBaseAttack(currentAttack + boostAmount);
        lastTriggerTime = now;

        // Schedule attack restoration after duration expires
        if (durationMillis > 0) {
            scheduleAttackRestore(weaponStats, currentAttack);
        }
    }

    /**
     * Schedules the restoration of the original attack value after the duration expires
     *
     * @param weaponStats the player's weapon stats component
     * @param originalAttack the original attack value to restore
     */
    private void scheduleAttackRestore(WeaponsStatsComponent weaponStats, int originalAttack) {
        new Thread(() -> {
            try {
                Thread.sleep(durationMillis);
                if (weaponStats != null && weaponStats.getEntity() != null) {
                    weaponStats.setBaseAttack(originalAttack);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}