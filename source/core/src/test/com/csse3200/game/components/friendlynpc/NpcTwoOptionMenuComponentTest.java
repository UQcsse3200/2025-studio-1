package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Ensures menu actions are callable; verifies "lead" triggers begin() when present.
 */
class NpcTwoOptionMenuComponentTest {

    @Test
    void openAndInstructionsAreNoOpWithoutDialogueDisplay() {
        Entity npc = new Entity();
        NpcTwoOptionMenuComponent menu = new NpcTwoOptionMenuComponent();
        npc.addComponent(menu);

        // Should not throw even if DialogueDisplay isn't attached
        assertDoesNotThrow(menu::open);
        assertDoesNotThrow(menu::chooseInstructions);
    }

    @Test
    void chooseLeadInvokesLeadBeginIfPresent() {
        Entity npc = new Entity();

        // Spy a real component so internal logic runs but we can verify the invocation
        NpcLeadComponent lead = Mockito.spy(new NpcLeadComponent(List.of(), 2.0f, 0.5f));
        npc.addComponent(lead);

        NpcTwoOptionMenuComponent menu = new NpcTwoOptionMenuComponent();
        npc.addComponent(menu);

        menu.chooseLead();

        verify(lead, times(1)).begin();
    }
}
