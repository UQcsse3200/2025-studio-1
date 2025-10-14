package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
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
    private MockedStatic<ServiceLocator> sl;

    @BeforeEach
    void setUp() {
        npc = new Entity();
        player = new Entity();
        combatStats = mock(CombatStatsComponent.class);
        lenient().when(combatStats.isDead()).thenReturn(false);
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

    @Test
    void playsSound_whenHealSucceeds() {
        player.addComponent(combatStats);
        ResourceService rs = mock(ResourceService.class);
        Sound heal = mock(Sound.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getResourceService).thenReturn(rs);
            when(rs.getAsset("sounds/healing-magic.mp3", Sound.class)).thenReturn(heal);

            NpcHealingComponent healing = new NpcHealingComponent(player, 25);
            npc.addComponent(healing);
            npc.create();

            npc.getEvents().trigger("npcDialogueEnd");

            verify(combatStats).addHealth(25);
            verify(heal).play(1.0f);
        }
    }

    @Test
    void doesNotPlaySound_whenWithinCooldown() {
        player.addComponent(combatStats);
        ResourceService rs = mock(ResourceService.class);
        Sound heal = mock(Sound.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getResourceService).thenReturn(rs);
            when(rs.getAsset("sounds/healing-magic.mp3", Sound.class)).thenReturn(heal);

            NpcHealingComponent c = new NpcHealingComponent(player, 10).setCooldownMillis(1000);
            npc.addComponent(c);
            npc.create();

            npc.getEvents().trigger("npcDialogueEnd");
            npc.getEvents().trigger("npcDialogueEnd");

            verify(heal, times(1)).play(1.0f);
            verify(combatStats, times(1)).addHealth(10);
        }
    }

    @Test
    void doesNotPlaySound_whenPlayerDead() {
        player.addComponent(combatStats);
        when(combatStats.isDead()).thenReturn(true);

        ResourceService rs = mock(ResourceService.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getResourceService).thenReturn(rs);

            NpcHealingComponent c = new NpcHealingComponent(player, 30);
            npc.addComponent(c);
            npc.create();

            npc.getEvents().trigger("npcDialogueEnd");

            verify(combatStats, never()).addHealth(anyInt());
            verify(rs, never()).getAsset(anyString(), eq(Sound.class));
        }
    }

    @Test
    void doesNotPlaySound_whenNoCombatStats() {
        Entity noStatsPlayer = new Entity();

        ResourceService rs = mock(ResourceService.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getResourceService).thenReturn(rs);

            NpcHealingComponent c = new NpcHealingComponent(noStatsPlayer, 20);
            npc.addComponent(c);
            npc.create();

            npc.getEvents().trigger("npcDialogueEnd");

            verify(rs, never()).getAsset(anyString(), eq(Sound.class));
        }
    }

    @Test
    void safeWhenResourceServiceIsNull() {
        player.addComponent(combatStats);
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getResourceService).thenReturn(null);

            NpcHealingComponent c = new NpcHealingComponent(player, 15);
            npc.addComponent(c);
            npc.create();

            npc.getEvents().trigger("npcDialogueEnd");

            verify(combatStats, times(1)).addHealth(15);
        }
    }

    @Test
    void safeWhenSoundAssetMissing() {
        player.addComponent(combatStats);
        ResourceService rs = mock(ResourceService.class);
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getResourceService).thenReturn(rs);
            when(rs.getAsset("sounds/healing-magic.mp3", Sound.class)).thenReturn(null);

            NpcHealingComponent c = new NpcHealingComponent(player, 12);
            npc.addComponent(c);
            npc.create();

            npc.getEvents().trigger("npcDialogueEnd");

            verify(combatStats).addHealth(12);
        }
    }
}