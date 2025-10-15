package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NpcInteractionComponentTest {
    private Entity npc;
    private DialogueDisplay ui;
    private NpcDialogueDataComponent data;

    @BeforeEach
    void setup() {
        npc = new Entity();

        // Create mocks/spies
        ui = mock(DialogueDisplay.class);
        data = mock(NpcDialogueDataComponent.class);
        NpcInteractionComponent interactComp = new NpcInteractionComponent();

        // Attach components to entity
        npc.addComponent(ui);
        npc.addComponent(data);
        npc.addComponent(interactComp);

        // Let them reference the entity
        ui.setEntity(npc);
        interactComp.setEntity(npc);

        // Initialize (calls addListener)
        interactComp.create();
    }

    @Test
    void hides_whenOutOfRange() {
        npc.getEvents().trigger("interact");
        npc.getEvents().trigger("exitedInteractRadius");
        verify(ui).hide();
    }

    @Test
    void hidesAndTriggersEnd_whenInteractedTwice() {
        npc.getEvents().trigger("interact"); // first opens dialogue
        npc.getEvents().trigger("interact"); // second should close
        verify(ui).hide();
    }

    @Test
    void bindsAndShowsFirstTime_whenInteracted() {
        npc.getEvents().trigger("interact");
        verify(ui).bindData(data);
        verify(ui).showFirst();
    }
}
