package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class NpcHealingComponentTest {
    private Entity npc;
    private Entity player;
    private CombatStatsComponent combatStats;
    private NpcHealingComponent healingComponent;

    @BeforeEach
    void setup() {
        npc = new Entity();
        player = new Entity();
        combatStats = mock(CombatStatsComponent.class);
        player.addComponent(combatStats);
    }

    @Test
    void healsPlayer_whenDialogueEnds() {
        when(combatStats.getHealth()).thenReturn(50);

        healingComponent = new NpcHealingComponent(player, 25);
        npc.addComponent(healingComponent);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        verify(combatStats).getHealth();
        verify(combatStats).setHealth(75);
    }

    @Test
    void healsCorrectAmount() {
        when(combatStats.getHealth()).thenReturn(30);

        healingComponent = new NpcHealingComponent(player, 50);
        npc.addComponent(healingComponent);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        verify(combatStats).setHealth(80);
    }

    @Test
    void multipleHeals_work() {
        when(combatStats.getHealth()).thenReturn(20, 45);

        healingComponent = new NpcHealingComponent(player, 25);
        npc.addComponent(healingComponent);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");
        npc.getEvents().trigger("npcDialogueEnd");

        verify(combatStats, times(2)).getHealth();
        verify(combatStats).setHealth(45);
        verify(combatStats).setHealth(70);
    }

    @Test
    void differentHealAmounts_work() {
        when(combatStats.getHealth()).thenReturn(10);

        healingComponent = new NpcHealingComponent(player, 100);
        npc.addComponent(healingComponent);
        npc.create();

        npc.getEvents().trigger("npcDialogueEnd");

        verify(combatStats).setHealth(110);
    }
}