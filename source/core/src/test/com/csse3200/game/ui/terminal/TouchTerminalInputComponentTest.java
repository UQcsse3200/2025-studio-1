package com.csse3200.game.ui.terminal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TouchTerminalInputComponentTest {

    @Test
    void keyPressed_returnsTrue_whenTerminalIsOpen() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);

        TouchTerminalInputComponent comp = new TouchTerminalInputComponent(terminal);

        assertTrue(comp.keyPressed(0));        // keycode not used by implementation
        verify(terminal, times(1)).isOpen();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyPressed_returnsFalse_whenTerminalIsClosed() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(false);

        TouchTerminalInputComponent comp = new TouchTerminalInputComponent(terminal);

        assertFalse(comp.keyPressed(0));
        verify(terminal, times(1)).isOpen();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void scrolled_negativeY_opensTerminal_andReturnsTrue() {
        Terminal terminal = mock(Terminal.class);
        TouchTerminalInputComponent comp = new TouchTerminalInputComponent(terminal);

        boolean handled = comp.scrolled(0f, -0.5f);

        assertTrue(handled);
        verify(terminal, times(1)).setOpen();
        verify(terminal, never()).setClosed();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void scrolled_positiveY_closesTerminal_andReturnsTrue() {
        Terminal terminal = mock(Terminal.class);
        TouchTerminalInputComponent comp = new TouchTerminalInputComponent(terminal);

        boolean handled = comp.scrolled(0f, 1.0f);

        assertTrue(handled);
        verify(terminal, times(1)).setClosed();
        verify(terminal, never()).setOpen();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void scrolled_zeroY_noStateChange_butReturnsTrue() {
        Terminal terminal = mock(Terminal.class);
        TouchTerminalInputComponent comp = new TouchTerminalInputComponent(terminal);

        boolean handled = comp.scrolled(123f, 0f); // amountX ignored

        assertTrue(handled);
        verify(terminal, never()).setOpen();
        verify(terminal, never()).setClosed();
        verifyNoMoreInteractions(terminal);
    }
}
