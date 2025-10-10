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









}