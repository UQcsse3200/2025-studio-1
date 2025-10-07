package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * Minimal test: only verifies the early-return branches of attach(...) when given null arguments.
 * It avoids creating any UI/Skin/Texture so the test is stable and headless-friendly.
 */
public class CompanionControlPanelTest {

    @Test
    void attach_returnsNull_whenStageIsNull() {
        Entity comp = mock(Entity.class);

        // Optional: static mock to ensure ServiceLocator is not accidentally used
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            assertNull(CompanionControlPanel.attach(null, comp));
            assertNull(CompanionControlPanel.attach(null, comp, 0.44f));
            sl.verifyNoInteractions(); // attach returns early; ServiceLocator must not be touched
        }
    }

    @Test
    void attach_returnsNull_whenCompIsNull() {
        Stage stage = mock(Stage.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            assertNull(CompanionControlPanel.attach(stage, null));
            assertNull(CompanionControlPanel.attach(stage, null, 200f));
            sl.verifyNoInteractions();
        }
    }
}
