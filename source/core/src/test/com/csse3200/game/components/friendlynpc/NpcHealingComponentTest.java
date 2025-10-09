package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * These tests verify that the NPC healing system correctly restores player health
 * when the "npcDialogueEnd" event is triggered, and properly handles edge cases
 * such as cooldowns, null players, missing components, and dead players.
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
     * Test that the component heals the player when the dialogue ends.
     * Verifies that the correct amount of health is added.
     */
    @Test
    void healsPlayer_onDialogueEnd() {
        int healAmount = 25;
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);

        NpcHealingComponent healing = new NpcHealingComponent(player, healAmount);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // The player's health should increase by the heal amount
        verify(combatStats, times(1)).addHealth(healAmount);
    }

    /**
     * Test that cooldown prevents repeated healing within the cooldown period.
     */
    @Test
    void respectsCooldown_onlyHealsOnceWithinCooldown() {
        int healAmount = 10;
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(false);

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
     * Test that no healing occurs when the player entity is null.
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
     * Test that no healing occurs if the player does not have a CombatStatsComponent.
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
     * Test that no healing happens if the player is dead.
     */
    @Test
    void doesNothing_whenPlayerIsDead() {
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(true);

        NpcHealingComponent healing = new NpcHealingComponent(player, 30);
        npc.addComponent(healing);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        // Dead players should not receive healing
        verify(combatStats, never()).addHealth(anyInt());
    }
}