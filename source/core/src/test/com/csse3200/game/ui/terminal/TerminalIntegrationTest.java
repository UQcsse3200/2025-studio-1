// src/test/java/com/csse3200/game/ui/terminal/TerminalIntegrationTest.java
package com.csse3200.game.ui.terminal;

import com.csse3200.game.ui.terminal.commands.FakeCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerminalIntegrationTest {

    /** Move the last-keystroke timestamp far into the past so debounce passes. */
    private static void bypassDebounce(Terminal t) {
        try {
            var f = Terminal.class.getDeclaredField("lastKeystrokeNs");
            f.setAccessible(true);
            f.setLong(t, System.nanoTime() - 50_000_000L); // 50ms > 20ms debounce
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void typing_suggests_then_accepts_top() {
        Terminal t = new Terminal();
        FakeCommand debug = new FakeCommand();
        FakeCommand death = new FakeCommand();
        t.addCommand("debug", debug);
        t.addCommand("deathscreen", death);

        t.setEnteredMessage("de");
        bypassDebounce(t);
        List<String> s = t.getAutocompleteSuggestions();
        assertEquals(List.of("deathscreen","debug"), s.subList(0, Math.min(2, s.size())));

        // Accept top suggestion "deathscreen", args preserved
        t.setEnteredMessage("de 42 hard");
        bypassDebounce(t);
        t.acceptTopSuggestion();
        assertEquals("deathscreen 42 hard", t.getEnteredMessage().trim());

        // Execute
        assertTrue(t.processMessage());
        assertEquals(1, death.invocations.size());
        assertEquals(List.of("42","hard"), death.invocations.get(0));
    }
}
