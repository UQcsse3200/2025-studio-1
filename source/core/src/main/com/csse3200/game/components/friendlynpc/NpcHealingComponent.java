package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/**
 * NPC Healing Component
 * Restores player health when dialogue ends.
 * If player is at full health, grants a temporary protection shield instead.
 */
public class NpcHealingComponent extends Component {
    private final Entity player;
    private final int healAmount;
    private final int shieldAmount;
    private final long shieldDurationMillis;

    // Frequency / cooldown
    private long cooldownMillis = 0; // 0 = no cooldown
    private long lastTriggerTime = 0L;

    private static final String HEAL_PATH = "sounds/healing-magic.mp3";

    /**
     * Creates an NPC healing component (healing only, no shield)
     *
     * @param player     the player entity to heal
     * @param healAmount the amount of health to restore
     */
    public NpcHealingComponent(Entity player, int healAmount) {
        this(player, healAmount, 0, 0);
    }

    /**
     * Creates an NPC healing component with shield option
     *
     * @param player the player entity to heal
     * @param healAmount the amount of health to restore
     * @param shieldAmount the amount of protection to grant when at full health
     * @param shieldDurationMillis how long the shield lasts (in milliseconds)
     */
    public NpcHealingComponent(Entity player, int healAmount, int shieldAmount, long shieldDurationMillis) {
        this.player = player;
        this.healAmount = Math.max(0, healAmount);
        this.shieldAmount = Math.max(0, shieldAmount);
        this.shieldDurationMillis = Math.max(0, shieldDurationMillis);
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
     * Heals the player or grants shield when dialogue ends
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

        // Check if player is at full health
        if (combatStats.getHealth() >= combatStats.getMaxHealth()) {
            // Player is at full health - grant shield instead
            if (shieldAmount > 0) {
                grantShield(combatStats);
                playHealSound();
            }
        } else {
            // Player is not at full health - heal
            combatStats.addHealth(healAmount);
            playHealSound();
        }

        lastTriggerTime = now;
    }

    /**
     * Grants a temporary protection shield to the player
     *
     * @param combatStats the player's combat stats component
     */
    private void grantShield(CombatStatsComponent combatStats) {
        combatStats.addProtection(shieldAmount);

        if (entity != null) {
            entity.getEvents().trigger("shieldStart");
        }

        // Schedule shield removal after duration
        if (shieldDurationMillis > 0) {
            scheduleShieldRemoval(combatStats, shieldAmount);
        }
    }

    /**
     * Schedules the removal of the shield after the duration expires
     *
     * @param combatStats the player's combat stats component
     * @param amount the amount of protection to remove
     */
    private void scheduleShieldRemoval(CombatStatsComponent combatStats, int amount) {
        new Thread(() -> {
            try {
                Thread.sleep(shieldDurationMillis);
                if (combatStats != null && combatStats.getEntity() != null) {
                    combatStats.addProtection(-amount);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (entity != null) {
                    entity.getEvents().trigger("shieldEnd");
                }
            }
        }).start();
    }

    private void playHealSound() {
        try {
            ResourceService rs = ServiceLocator.getResourceService();
            Sound sfx = rs.getAsset(HEAL_PATH, Sound.class);
            if (sfx != null) {
                sfx.play(0.7f);
            }
        } catch (Exception ignored) {
        }
    }
}