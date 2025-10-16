package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * These tests verify that the NPC healing system correctly restores player health
 * when the "npcDialogueEnd" event is triggered, and grants a protection shield
 * when the player is at full health. Tests also cover edge cases such as cooldowns,
 * null players, missing components, and dead players.
 */
@ExtendWith(MockitoExtension.class)
class NpcHealingComponentTest {

    private Entity npc;
    private Entity player;
    private CombatStatsComponent combatStats;

    @BeforeEach
    void setUp() {
        npc = new Entity();
        player = new Entity();
        combatStats = mock(CombatStatsComponent.class);
    }

    /**
     * Test that the component heals the player when the dialogue ends and player is not at full health.
     * Verifies that the correct amount of health is added.
     */
    @Test
    void healsPlayer_whenNotAtFullHealth() {
        int healAmount = 25;
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(50);
        when(combatStats.getMaxHealth()).thenReturn(100);

        NpcHealingComponent healing = new NpcHealingComponent(player, healAmount);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // The player's health should increase by the heal amount
        verify(combatStats, times(1)).addHealth(healAmount);
        verify(combatStats, never()).addProtection(anyInt());
    }

    /**
     * Test that the component grants shield when player is at full health.
     * Verifies that protection is added instead of health.
     */
    @Test
    void grantsShield_whenAtFullHealth() {
        int healAmount = 25;
        int shieldAmount = 10;
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(100);
        when(combatStats.getMaxHealth()).thenReturn(100);

        NpcHealingComponent healing = new NpcHealingComponent(player, healAmount, shieldAmount, 0);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Should grant shield, not heal
        verify(combatStats, never()).addHealth(anyInt());
        verify(combatStats, times(1)).addProtection(shieldAmount);
    }

    /**
     * Test that shield is granted and removed after duration expires.
     */
    @Test
    void grantsAndRemovesShield_afterDuration() throws InterruptedException {
        int healAmount = 25;
        int shieldAmount = 15;
        long duration = 100; // 100ms for quick test

        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(100);
        when(combatStats.getMaxHealth()).thenReturn(100);
        when(combatStats.getEntity()).thenReturn(player);

        NpcHealingComponent healing = new NpcHealingComponent(player, healAmount, shieldAmount, duration);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Shield should be granted
        verify(combatStats, times(1)).addProtection(shieldAmount);

        // Wait for shield removal
        Thread.sleep(duration + 200);

        // Shield should be removed (negative value)
        verify(combatStats, times(1)).addProtection(-shieldAmount);
    }

    /**
     * Test that no shield is granted when shield amount is 0, even at full health.
     */
    @Test
    void doesNotGrantShield_whenShieldAmountIsZero() {
        int healAmount = 25;
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(100);
        when(combatStats.getMaxHealth()).thenReturn(100);

        NpcHealingComponent healing = new NpcHealingComponent(player, healAmount, 0, 0);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // No shield or healing should occur
        verify(combatStats, never()).addHealth(anyInt());
        verify(combatStats, never()).addProtection(anyInt());
    }

    /**
     * Test that shield with no duration is granted permanently (not removed).
     */
    @Test
    void grantsShield_withoutRemoval_whenDurationIsZero() throws InterruptedException {
        int healAmount = 25;
        int shieldAmount = 20;

        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(100);
        when(combatStats.getMaxHealth()).thenReturn(100);

        NpcHealingComponent healing = new NpcHealingComponent(player, healAmount, shieldAmount, 0);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Shield should be granted
        verify(combatStats, times(1)).addProtection(shieldAmount);

        // Wait a bit
        Thread.sleep(200);

        // Shield should NOT be removed (only called once with positive value)
        verify(combatStats, times(1)).addProtection(anyInt());
    }

    /**
     * Test that cooldown prevents repeated healing/shielding within the cooldown period.
     */
    @Test
    void respectsCooldown_onlyTriggersOnceWithinCooldown() {
        int healAmount = 10;
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(50);
        when(combatStats.getMaxHealth()).thenReturn(100);

        // Set a 1-second cooldown
        NpcHealingComponent healing = new NpcHealingComponent(player, healAmount)
                .setCooldownMillis(1000);
        npc.addComponent(healing);
        npc.create();

        // Trigger the event twice in quick succession
        npc.getEvents().trigger("npcDialogueEnd");
        npc.getEvents().trigger("npcDialogueEnd");

        // Should only heal once because cooldown not expired
        verify(combatStats, times(1)).addHealth(healAmount);
    }

    /**
     * Test that no healing or shielding occurs when the player entity is null.
     * Ensures the component handles null safely without throwing exceptions.
     */
    @Test
    void doesNothing_whenPlayerIsNull() {
        NpcHealingComponent healing = new NpcHealingComponent(null, 20);
        npc.addComponent(healing);
        npc.create();

        // Should not crash or attempt healing
        npc.getEvents().trigger("npcDialogueEnd");
    }

    /**
     * Test that no healing or shielding occurs if the player does not have a CombatStatsComponent.
     */
    @Test
    void doesNothing_whenPlayerHasNoCombatStats() {
        NpcHealingComponent healing = new NpcHealingComponent(player, 15);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Since player has no combat stats, no health changes should occur
        verifyNoInteractions(combatStats);
    }

    /**
     * Test that no healing or shielding happens if the player is dead.
     */
    @Test
    void doesNothing_whenPlayerIsDead() {
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(true);

        NpcHealingComponent healing = new NpcHealingComponent(player, 30);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Dead players should not receive healing or shield
        verify(combatStats, never()).addHealth(anyInt());
        verify(combatStats, never()).addProtection(anyInt());
    }

    /**
     * Test that healing works with both constructors (2 params and 4 params).
     */
    @Test
    void worksWithBothConstructors() {
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(50);
        when(combatStats.getMaxHealth()).thenReturn(100);

        // Test 2-parameter constructor
        NpcHealingComponent healing1 = new NpcHealingComponent(player, 25);
        npc.addComponent(healing1);
        npc.create();
        npc.getEvents().trigger("npcDialogueEnd");
        verify(combatStats, times(1)).addHealth(25);

        // Test 4-parameter constructor
        Entity npc2 = new Entity();
        NpcHealingComponent healing2 = new NpcHealingComponent(player, 20, 10, 1000);
        npc2.addComponent(healing2);
        npc2.create();
        npc2.getEvents().trigger("npcDialogueEnd");
        verify(combatStats, times(1)).addHealth(20);
    }

    /**
     * Test that negative shield amount is clamped to zero.
     */
    @Test
    void clampsNegativeShield_toZero() {
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(100);
        when(combatStats.getMaxHealth()).thenReturn(100);

        // Negative shield should be clamped to 0
        NpcHealingComponent healing = new NpcHealingComponent(player, 25, -10, 1000);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // No shield should be granted (amount was clamped to 0)
        verify(combatStats, never()).addProtection(anyInt());
    }

    /**
     * Fires "shieldStart" when shield is granted
     */
    @Test
    void firesShieldStart_whenShieldGranted() throws InterruptedException {
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(100);
        when(combatStats.getMaxHealth()).thenReturn(100);

        final boolean[] started = {false};
        npc.getEvents().addListener("shieldStart", () -> started[0] = true);

        NpcHealingComponent healing = new NpcHealingComponent(player, 25, 15, 100);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        Thread.sleep(80);
        assertTrue(started[0], "Expected 'shieldStart' to be fired");
        verify(combatStats, atLeastOnce()).addProtection(15);
    }



    /**
     * After duration, fires "shieldEnd" and removes protection
     */
    @Test
    void firesShieldEnd_afterDuration() throws InterruptedException {
        int shieldAmount = 10;
        long durationMs = 50;

        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);
        when(combatStats.getHealth()).thenReturn(100);
        when(combatStats.getMaxHealth()).thenReturn(100);
        when(combatStats.getEntity()).thenReturn(player);

        final boolean[] ended = {false};
        npc.getEvents().addListener("shieldEnd", () -> ended[0] = true);

        NpcHealingComponent healing = new NpcHealingComponent(player, 25, shieldAmount, durationMs);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // wait a bit longer than duration for the background thread to run
        Thread.sleep(durationMs + 200);
        assertTrue(ended[0], "Expected 'shieldEnd' to be fired after duration");
        verify(combatStats, atLeastOnce()).addProtection(-shieldAmount);
    }
}