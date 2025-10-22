package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.Input;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KeyboardTerminalInputComponent.
 */
class KeyboardTerminalInputComponentTest {

    @Test
    void keyPressed_F1_callsToggle_andReturnsTrue() {
        // Arrange
        Terminal terminal = mock(Terminal.class);
        KeyboardTerminalInputComponent comp = new KeyboardTerminalInputComponent(terminal);

        // Act
        boolean handled = comp.keyPressed(Input.Keys.F1);

        // Assert
        assertTrue(handled);
        verify(terminal, times(1)).toggleIsOpen();
        // No other interactions expected
        verify(terminal, never()).isOpen();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyPressed_nonF1_whenTerminalOpen_returnsTrue_withoutToggling() {
        // Arrange
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);
        KeyboardTerminalInputComponent comp = new KeyboardTerminalInputComponent(terminal);

        // Act (use any non-F1 key, e.g., A)
        boolean handled = comp.keyPressed(Input.Keys.A);

        // Assert
        assertTrue(handled);
        verify(terminal, times(1)).isOpen();
        verify(terminal, never()).toggleIsOpen();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyPressed_nonF1_whenTerminalClosed_returnsFalse_withoutToggling() {
        // Arrange
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(false);
        KeyboardTerminalInputComponent comp = new KeyboardTerminalInputComponent(terminal);

        // Act (use any non-F1 key, e.g., SPACE)
        boolean handled = comp.keyPressed(Input.Keys.SPACE);

        // Assert
        assertFalse(handled);
        verify(terminal, times(1)).isOpen();
        verify(terminal, never()).toggleIsOpen();
        verifyNoMoreInteractions(terminal);
    }
}
