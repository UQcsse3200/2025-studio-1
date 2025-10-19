package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class NpcInteractionComponentTest {
    private Entity npc;
    private DialogueDisplay ui;
    private NpcDialogueDataComponent data;
    private Label mockLabel;
    private MockedStatic<ServiceLocator> mockedServiceLocator;

    @BeforeEach
    void setup() {
        npc = new Entity();

        // Mock UI and data
        ui = mock(DialogueDisplay.class);
        data = mock(NpcDialogueDataComponent.class);

        // Mock label and ServiceLocator
        mockLabel = mock(Label.class);
        mockedServiceLocator = mockStatic(ServiceLocator.class);
        mockedServiceLocator.when(ServiceLocator::getPrompt).thenReturn(mockLabel);

        // Create and attach interaction component
        NpcInteractionComponent interactComp = new NpcInteractionComponent();
        npc.addComponent(ui);
        npc.addComponent(data);
        npc.addComponent(interactComp);

        ui.setEntity(npc);
        interactComp.setEntity(npc);

        interactComp.create();
    }

    @AfterEach
    void tearDown() {
        // Always close static mock
        mockedServiceLocator.close();
    }

    @Test
    void hides_whenOutOfRange() {
        npc.getEvents().trigger("interact");
        npc.getEvents().trigger("exitedInteractRadius");
        verify(ui).hide();
        verify(mockLabel, atLeastOnce()).setVisible(anyBoolean());
    }

    @Test
    void hidesAndTriggersEnd_whenInteractedTwice() {
        npc.getEvents().trigger("interact");
        npc.getEvents().trigger("interact");
        verify(ui).hide();
        verify(mockLabel, atLeastOnce()).setVisible(anyBoolean());
    }

    @Test
    void bindsAndShowsFirstTime_whenInteracted() {
        npc.getEvents().trigger("interact");
        verify(ui).bindData(data);
        verify(ui).showFirst();
        verify(mockLabel, atLeastOnce()).setVisible(anyBoolean());
    }
}
