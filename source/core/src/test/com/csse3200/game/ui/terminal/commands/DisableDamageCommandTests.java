package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DisableDamageCommandTests {
    private static ArrayList<String> args(String s) {
        return new ArrayList<>(List.of(s));
    }

    @Test
    void turnsOffDamage_onlyForPlayerWithWeapons() {
        EntityService es = mock(EntityService.class);
        Entity player = mock(Entity.class);
        Entity npc = mock(Entity.class);

        WeaponsStatsComponent w = mock(WeaponsStatsComponent.class);
        when(player.getComponent(PlayerActions.class)).thenReturn(mock(PlayerActions.class));
        when(player.getComponent(WeaponsStatsComponent.class)).thenReturn(w);
        when(npc.getComponent(PlayerActions.class)).thenReturn(null); // not a player

        Array<Entity> list = new Array<>();
        list.addAll(player, npc);
        when(es.getEntities()).thenReturn(list);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            DisableDamageCommand cmd = new DisableDamageCommand();
            assertTrue(cmd.action(args("off")));
            verify(w).setDisableDamage(false);
            // Ensure we didn't try to set on npc
            verify(npc, times(1)).getComponent(PlayerActions.class);
            verify(npc, atMostOnce()).getComponent(WeaponsStatsComponent.class);
        }
    }

    @Test
    void rejectsInvalidArg() {
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(mock(EntityService.class));
            DisableDamageCommand cmd = new DisableDamageCommand();
            assertFalse(cmd.action(args("maybe")));
            assertFalse(cmd.action(args("")));
        }
    }
}
