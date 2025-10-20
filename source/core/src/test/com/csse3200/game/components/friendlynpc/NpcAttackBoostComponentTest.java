package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * These tests verify that the NPC attack boost system correctly increases player attack power
 * when the "npcDialogueEnd" event is triggered, and properly handles edge cases
 * such as cooldowns, null players, missing components, and duration-based restoration.
 */
@ExtendWith(MockitoExtension.class)
class NpcAttackBoostComponentTest {

    private Entity npc;
    private Entity player;
    private WeaponsStatsComponent weaponStats;

    @BeforeEach
    void setUp() {
        npc = new Entity();
        player = new Entity();
        weaponStats = mock(WeaponsStatsComponent.class);
    }

    /**
     * Test that the component boosts the player's attack when the dialogue ends.
     * Verifies that the attack is increased by the correct amount.
     */
    @Test
    void boostsPlayerAttack_onDialogueEnd() {
        int boostAmount = 10;
        int currentAttack = 20;
        player.addComponent(weaponStats);
        when(weaponStats.getBaseAttack()).thenReturn(currentAttack);

        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(player, boostAmount, 0);
        npc.addComponent(attackBoost);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // The player's attack should increase by the boost amount
        verify(weaponStats, times(1)).getBaseAttack();
        verify(weaponStats, times(1)).setBaseAttack(currentAttack + boostAmount);
    }

    /**
     * Test that cooldown prevents repeated boosting within the cooldown period.
     */
    @Test
    void respectsCooldown_onlyBoostsOnceWithinCooldown() {
        int boostAmount = 5;
        int currentAttack = 15;
        player.addComponent(weaponStats);
        when(weaponStats.getBaseAttack()).thenReturn(currentAttack);

        // Set a 1-second cooldown
        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(player, boostAmount, 0)
                .setCooldownMillis(1000);
        npc.addComponent(attackBoost);
        npc.create();

        // Trigger the event twice in quick succession
        npc.getEvents().trigger("npcDialogueEnd");
        npc.getEvents().trigger("npcDialogueEnd");

        // Should only boost once because cooldown not expired
        verify(weaponStats, times(1)).setBaseAttack(currentAttack + boostAmount);
    }

    /**
     * Test that no boost occurs when the player entity is null.
     * Ensures the component handles null safely without throwing exceptions.
     */
    @Test
    void doesNothing_whenPlayerIsNull() {
        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(null, 10, 0);
        npc.addComponent(attackBoost);
        npc.create();

        // Should not crash or attempt boosting
        npc.getEvents().trigger("npcDialogueEnd");
    }

    /**
     * Test that no boost occurs if the player does not have a WeaponsStatsComponent.
     */
    @Test
    void doesNothing_whenPlayerHasNoWeaponStats() {
        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(player, 15, 0);
        npc.addComponent(attackBoost);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Since player has no weapon stats, no attack changes should occur
        verifyNoInteractions(weaponStats);
    }

    /**
     * Test that the attack boost is applied with a duration.
     * Verifies that setBaseAttack is called to apply the boost.
     */
    @Test
    void appliesBoost_withDuration() {
        int boostAmount = 8;
        int currentAttack = 12;
        long duration = 100; // 100ms for quick test

        player.addComponent(weaponStats);
        when(weaponStats.getBaseAttack()).thenReturn(currentAttack);

        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(player, boostAmount, duration);
        npc.addComponent(attackBoost);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Should boost the attack initially
        verify(weaponStats, times(1)).setBaseAttack(currentAttack + boostAmount);
    }

    /**
     * Test that attack boost restoration is scheduled when duration is set.
     * Note: This test verifies the boost is applied and then restored after the duration.
     */
    @Test
    void schedulesRestoration_whenDurationIsPositive() throws InterruptedException {
        int boostAmount = 10;
        int currentAttack = 20;
        long duration = 100; // 100ms for quick test

        player.addComponent(weaponStats);
        when(weaponStats.getBaseAttack()).thenReturn(currentAttack);
        when(weaponStats.getEntity()).thenReturn(player);

        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(player, boostAmount, duration);
        npc.addComponent(attackBoost);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Initial boost should be applied
        verify(weaponStats, times(1)).getBaseAttack();
        verify(weaponStats, times(1)).setBaseAttack(currentAttack + boostAmount);

        // Wait for restoration to occur (with some buffer time)
        Thread.sleep(duration + 200);

        // Restoration should set attack back to original value
        verify(weaponStats, times(2)).setBaseAttack(anyInt());
        verify(weaponStats, times(1)).setBaseAttack(currentAttack);
    }

    /**
     * Test that no boost occurs multiple times when duration is set and
     * cooldown has expired, ensuring each boost creates a new restoration timer.
     */
    @Test
    void allowsMultipleBoosts_afterCooldownExpires() throws InterruptedException {
        int boostAmount = 5;
        int currentAttack = 10;
        long cooldown = 50; // 50ms cooldown

        player.addComponent(weaponStats);
        when(weaponStats.getBaseAttack()).thenReturn(currentAttack);

        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(player, boostAmount, 0)
                .setCooldownMillis(cooldown);
        npc.addComponent(attackBoost);
        npc.create();

        // First boost
        npc.getEvents().trigger("npcDialogueEnd");

        // Wait for cooldown to expire
        Thread.sleep(cooldown + 50);

        // Second boost after cooldown
        npc.getEvents().trigger("npcDialogueEnd");

        // Should boost twice
        verify(weaponStats, times(2)).setBaseAttack(currentAttack + boostAmount);
    }

    /**
     * Test that negative boost amounts are clamped to zero.
     */
    @Test
    void clampsNegativeBoost_toZero() {
        int currentAttack = 15;
        player.addComponent(weaponStats);
        when(weaponStats.getBaseAttack()).thenReturn(currentAttack);

        // Negative boost should be clamped to 0
        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(player, -10, 0);
        npc.addComponent(attackBoost);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Attack should remain unchanged (boost of 0)
        verify(weaponStats, times(1)).setBaseAttack(currentAttack);
    }

    /**
     * Test that negative duration is clamped to zero (no restoration scheduled).
     */
    @Test
    void clampsNegativeDuration_toZero() {
        int boostAmount = 10;
        int currentAttack = 20;
        player.addComponent(weaponStats);
        when(weaponStats.getBaseAttack()).thenReturn(currentAttack);

        // Negative duration should be clamped to 0 (no restoration)
        NpcAttackBoostComponent attackBoost = new NpcAttackBoostComponent(player, boostAmount, -1000);
        npc.addComponent(attackBoost);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Should boost once
        verify(weaponStats, times(1)).setBaseAttack(currentAttack + boostAmount);
    }
}