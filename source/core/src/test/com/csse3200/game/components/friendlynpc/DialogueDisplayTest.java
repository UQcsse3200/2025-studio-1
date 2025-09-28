package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DialogueDisplayTest {

    private DialogueDisplay dialogueDisplay;

    @BeforeEach
    void setUp() {
        dialogueDisplay = new DialogueDisplay();
    }

    @Test
    void testCreateDoesNotThrow() {
        // Test that create method doesn't throw exceptions
        // Even if services are not available
        assertDoesNotThrow(() -> dialogueDisplay.create());
    }

    @Test
    void testDisposeDoesNotThrow() {
        // Test dispose without creating first
        assertDoesNotThrow(() -> dialogueDisplay.dispose());
    }

    @Test
    void testCreateThenDispose() {
        // Test the full lifecycle
        assertDoesNotThrow(() -> {
            dialogueDisplay.create();
            dialogueDisplay.dispose();
        });
    }

    @Test
    void testMultipleDisposeCallsSafe() {
        // Test that multiple dispose calls are safe
        assertDoesNotThrow(() -> {
            dialogueDisplay.create();
            dialogueDisplay.dispose();
            dialogueDisplay.dispose(); // Second dispose should be safe
        });
    }

    @Test
    void testBasicComponentFunctionality() {
        // Test basic component methods inherited from Component
        assertNotNull(dialogueDisplay);

        // Test that the component can be created without exceptions
        assertDoesNotThrow(() -> dialogueDisplay.create());

        // Test early disposal (if this method exists in Component)
        // Remove this line if earlyDispose doesn't exist
        // assertDoesNotThrow(() -> dialogueDisplay.earlyDispose());
    }

    @Test
    void testServiceHandling() {
        // Test that the component handles service unavailability gracefully
        try (MockedStatic<ServiceLocator> mockedServiceLocator = mockStatic(ServiceLocator.class)) {
            // Mock ServiceLocator to throw exceptions
            mockedServiceLocator.when(ServiceLocator::getRenderService)
                    .thenThrow(new RuntimeException("Service not available"));

            // Should not crash when services are unavailable
            assertDoesNotThrow(() -> dialogueDisplay.create());
        }
    }

    @Test
    void testNullServiceHandling() {
        // Test that the component handles null services gracefully
        try (MockedStatic<ServiceLocator> mockedServiceLocator = mockStatic(ServiceLocator.class)) {
            // Mock ServiceLocator to return null
            mockedServiceLocator.when(ServiceLocator::getRenderService).thenReturn(null);

            // Should not crash when services return null
            assertDoesNotThrow(() -> dialogueDisplay.create());
        }
    }
}