package com.csse3200.game.ui.terminal;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BaseTerminalInputComponent.
 */
class BaseTerminalInputComponentTest {

    @Test
    void keyTyped_returnsFalse_whenTerminalClosed() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(false);

        TestInput comp = new TestInput(terminal);

        assertFalse(comp.keyTyped('a'));
        verify(terminal, times(1)).isOpen();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyTyped_backspace_callsHandleBackspace_andReturnsTrue() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);

        TestInput comp = new TestInput(terminal);

        assertTrue(comp.keyTyped('\b'));
        verify(terminal).isOpen();
        verify(terminal).handleBackspace();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyTyped_newline_processMessageTrue_togglesAndClears_andReturnsTrue() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.processMessage()).thenReturn(true);

        TestInput comp = new TestInput(terminal);

        assertTrue(comp.keyTyped('\n'));
        verify(terminal).isOpen();
        verify(terminal).processMessage();
        verify(terminal).toggleIsOpen();
        verify(terminal).setEnteredMessage("");
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyTyped_carriageReturn_processMessageFalse_clearsOnly_andReturnsTrue() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.processMessage()).thenReturn(false);

        TestInput comp = new TestInput(terminal);

        assertTrue(comp.keyTyped('\r'));
        verify(terminal).isOpen();
        verify(terminal).processMessage();
        verify(terminal, never()).toggleIsOpen();
        verify(terminal).setEnteredMessage("");
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyTyped_printable_space_and_at_sign_append_andReturnTrue() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);

        TestInput comp = new TestInput(terminal);

        assertTrue(comp.keyTyped(' ')); // ASCII 32
        assertTrue(comp.keyTyped('@')); // printable

        verify(terminal, times(2)).isOpen();
        verify(terminal).appendToMessage(' ');
        verify(terminal).appendToMessage('@');
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyTyped_nonPrintable_DEL_returnsFalse_noSideEffects() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);

        TestInput comp = new TestInput(terminal);

        assertFalse(comp.keyTyped((char) 127)); // DEL
        verify(terminal).isOpen();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyReleased_returns_isOpen_value() {
        Terminal terminal = mock(Terminal.class);
        TestInput comp = new TestInput(terminal);

        when(terminal.isOpen()).thenReturn(true);
        assertTrue(comp.keyReleased(0));
        when(terminal.isOpen()).thenReturn(false);
        assertFalse(comp.keyReleased(0));

        verify(terminal, times(2)).isOpen();
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void isPauseable_isAlwaysFalse() {
        TestInput comp = new TestInput(mock(Terminal.class));
        assertFalse(comp.pauseable());
    }

    @Test
    void create_setsTerminalFromEntityComponent() throws Exception {
        // Arrange entity with Terminal and the input component
        Terminal terminal = mock(Terminal.class);
        Entity entity = new Entity();
        BaseTerminalInputComponentTest.TestInput comp = new BaseTerminalInputComponentTest.TestInput(null); // your seam subclass
        entity.addComponent(terminal);
        entity.addComponent(comp);

        // Mock ServiceLocator.getInputService() so InputComponent#create() can register safely
        try (MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {
            InputService inputSvc = mock(InputService.class);
            // register(...) is void; no-op is fine
            doNothing().when(inputSvc).register(any());

            svc.when(ServiceLocator::getInputService).thenReturn(inputSvc);

            // Act
            assertDoesNotThrow(comp::create);

            // Assert: registration happened and terminal field was set from entity
            verify(inputSvc).register(comp);

            var f = BaseTerminalInputComponent.class.getDeclaredField("terminal");
            f.setAccessible(true);
            assertSame(terminal, f.get(comp));
        }
    }

    @Test
    void keyTyped_printable_boundaries_andExtended_append_andReturnTrue() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);

        BaseTerminalInputComponentTest.TestInput comp = new BaseTerminalInputComponentTest.TestInput(terminal);

        // low boundary (32) space – if you already test space elsewhere, you can keep or drop this call
        assertTrue(comp.keyTyped(' '));
        // high ASCII boundary (126) tilde
        assertTrue(comp.keyTyped('~'));
        // extended ASCII/Unicode >= 128
        assertTrue(comp.keyTyped((char) 128));
        // explicit Unicode example (Euro sign)
        assertTrue(comp.keyTyped('\u20AC'));

        // verify appends for each printable char
        verify(terminal, times(4)).isOpen();
        verify(terminal).appendToMessage(' ');
        verify(terminal).appendToMessage('~');
        verify(terminal).appendToMessage((char) 128);
        verify(terminal).appendToMessage('\u20AC');
        verifyNoMoreInteractions(terminal);
    }

    @Test
    void keyTyped_nonPrintable_belowSpace_returnsFalse_andNoAppend() {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);

        BaseTerminalInputComponentTest.TestInput comp = new BaseTerminalInputComponentTest.TestInput(terminal);

        // 31 is just below space -> not printable
        assertFalse(comp.keyTyped((char) 31));

        verify(terminal).isOpen();
        verify(terminal, never()).appendToMessage(anyChar());
        verify(terminal, never()).handleBackspace();
        verify(terminal, never()).processMessage();
        verify(terminal, never()).toggleIsOpen();
        verify(terminal, never()).setEnteredMessage(anyString());
        verifyNoMoreInteractions(terminal);
    }

    /**
     * Minimal concrete seam for testing protected/abstract bits.
     */
    static class TestInput extends BaseTerminalInputComponent {
        TestInput(Terminal terminal) {
            super(terminal);
        }

        @Override
        public boolean keyPressed(int keycode) {
            return false;
        }

        // Expose protected method for test
        boolean pauseable() {
            return isPauseable();
        }
    }
}
