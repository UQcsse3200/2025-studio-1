// src/test/java/com/csse3200/game/ui/terminal/TerminalAutocompleteTest.java
package com.csse3200.game.ui.terminal;

import com.csse3200.game.ui.terminal.commands.FakeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerminalAutocompleteTest {

    Terminal terminal;

    /**
     * Test-only helper: move the last-keystroke timestamp far into the past so debounce passes.
     */
    private static void bypassDebounce(Terminal t) {
        try {
            var f = Terminal.class.getDeclaredField("lastKeystrokeNs");
            f.setAccessible(true);
            f.setLong(t, System.nanoTime() - 50_000_000L); // 50ms > 20ms debounce
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        terminal = new Terminal();
        terminal.rebuildAutocompleteIndex();
    }

    @Test
    void emptyPrefix_yieldsNoSuggestions() {
        terminal.setEnteredMessage("");
        bypassDebounce(terminal);
        List<String> s = terminal.getAutocompleteSuggestions();
        assertTrue(s.isEmpty());
    }

    @Test
    void exactPrefix_returnsTopKLexicographic() {
        terminal.setEnteredMessage("d");
        bypassDebounce(terminal);
        List<String> s = terminal.getAutocompleteSuggestions()
                .stream()
                .map(String::valueOf)
                .toList();

        assertEquals(
                List.of("damageMultiplier", "deathScreen", "debug", "disableDamage", "doorOverride"),
                s
        );
    }

    @Test
    void insideWordPrefix_stillWorks() {
        terminal.setEnteredMessage("dis");
        bypassDebounce(terminal);
        List<String> s = terminal.getAutocompleteSuggestions();
        assertEquals(List.of("disableDamage"), s);
    }

    @Test
    void debounce_reusesResultsWithinWindow() {
        terminal.setEnteredMessage("de");
        List<String> s1 = terminal.getAutocompleteSuggestions();
        terminal.setEnteredMessage("de");
        List<String> s2 = terminal.getAutocompleteSuggestions();
        assertSame(s1, s2);
    }

    @Test
    void bkTreeFallback_ed1_worksWhenTrieEmpty() {
        terminal.setEnteredMessage("debub");
        bypassDebounce(terminal);
        List<String> s = terminal.getAutocompleteSuggestions();
        assertTrue(s.contains("debug"));
    }

    @Test
    void capToFiveSuggestions_whenManyMatch() {
        terminal.addCommand("delta", new FakeCommand());
        terminal.addCommand("dive", new FakeCommand());
        terminal.addCommand("dock", new FakeCommand());
        terminal.addCommand("dodge", new FakeCommand());
        terminal.addCommand("daring", new FakeCommand());

        terminal.setEnteredMessage("d");
        bypassDebounce(terminal);
        List<String> s = terminal.getAutocompleteSuggestions();
        assertTrue(s.size() <= 5);
    }
}
