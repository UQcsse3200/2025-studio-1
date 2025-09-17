// src/test/java/com/csse3200/game/ui/terminal/TerminalProcessMessageTest.java
package com.csse3200.game.ui.terminal;

import com.csse3200.game.ui.terminal.commands.FakeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalProcessMessageTest {

    Terminal terminal;
    FakeCommand teleport, god, give;

    @BeforeEach
    void setUp() {
        terminal = new Terminal();
        teleport = new FakeCommand();
        god = new FakeCommand();
        give = new FakeCommand();

        terminal.addCommand("teleport", teleport);
        terminal.addCommand("god", god);
        terminal.addCommand("give", give);
    }

    @Test
    void executesCommand_noArgs() {
        terminal.setEnteredMessage("god");
        boolean handled = terminal.processMessage();
        assertTrue(handled);
        assertEquals(1, god.invocations.size());
        assertTrue(god.invocations.get(0).isEmpty());
        assertEquals("", terminal.getEnteredMessage());
    }

    @Test
    void executesCommand_withArgs() {
        terminal.setEnteredMessage("give sword 3");
        boolean handled = terminal.processMessage();
        assertTrue(handled);
        assertEquals(1, give.invocations.size());
        assertEquals(2, give.invocations.get(0).size());
        assertEquals("sword", give.invocations.get(0).get(0));
        assertEquals("3", give.invocations.get(0).get(1));
    }

    @Test
    void unknownCommand_returnsFalse() {
        terminal.setEnteredMessage("unknown thing");
        assertFalse(terminal.processMessage());
        // message is not cleared on unknown
        assertEquals("unknown thing", terminal.getEnteredMessage());
    }
}
