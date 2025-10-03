package com.csse3200.game.ui.terminal;

import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.CountdownTimerService;
import com.csse3200.game.ui.terminal.autocomplete.RadixTrie;
import com.csse3200.game.ui.terminal.commands.Command;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TerminalTest {
    // Debounce in production code is 20ms; give ourselves a tiny buffer
    private static final Duration DEBOUNCE = Duration.ofMillis(20);
    private static final Duration DEBOUNCE_BUFFER = Duration.ofMillis(10);
    private static final Duration AT_MOST = Duration.ofMillis(500);

    /**
     * Wait until the terminal has produced a non-empty suggestions list.
     */
    private static void awaitSuggestionsReady(Terminal terminal) {
        await().atMost(AT_MOST)
                .until(() -> !terminal.getAutocompleteSuggestions().isEmpty());
    }

    /**
     * Wait just long enough to cross the debounce boundary (without Thread.sleep).
     */
    private static void waitPastDebounce() {
        await().pollDelay(DEBOUNCE.plus(DEBOUNCE_BUFFER))
                .atMost(AT_MOST)
                .until(() -> true); // evaluate once after the delay
    }

    private static String callExtractFirstToken(String in) throws Exception {
        Method m = Terminal.class.getDeclaredMethod("extractFirstToken", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, in);
    }

    private static String callStripFirstToken(String in) throws Exception {
        Method m = Terminal.class.getDeclaredMethod("stripFirstToken", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, in);
    }

    @Test
    void shouldSetOpenClosedAndToggle() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setClosed();
        assertFalse(terminal.isOpen());

        terminal.setOpen();
        assertTrue(terminal.isOpen());

        terminal.setClosed();
        assertFalse(terminal.isOpen());

        terminal.toggleIsOpen();
        assertTrue(terminal.isOpen());
        terminal.toggleIsOpen();
        assertFalse(terminal.isOpen());
    }

    @Test
    void setEnteredMessageHandlesNullAndClearsOnClose() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setEnteredMessage(null);
        assertEquals("", terminal.getEnteredMessage());

        terminal.setEnteredMessage("abc");
        assertEquals("abc", terminal.getEnteredMessage());

        terminal.setClosed(); // should also clear message
        assertEquals("", terminal.getEnteredMessage());
    }

    @Test
    void addCommandAddsAndDuplicateDoesNotGrowMap() {
        Map<String, Command> backing = new LinkedHashMap<>();
        Terminal terminal = new Terminal(backing, null, null);

        int startSize = backing.size();

        Command c = mock(Command.class);
        terminal.addCommand("test1", c);
        assertEquals(startSize + 1, backing.size());

        terminal.addCommand("test2", c);
        assertEquals(startSize + 2, backing.size());

        // Duplicate key should replace, not grow
        Command c2 = mock(Command.class);
        terminal.addCommand("test2", c2);
        assertEquals(startSize + 2, backing.size());
        assertSame(c2, backing.get("test2"));
    }

    @Test
    void processMessageReturnsFalseOnEmptyOrUnknown() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setEnteredMessage("   ");
        assertFalse(terminal.processMessage());

        terminal.setEnteredMessage("doesNotExist arg1");
        assertFalse(terminal.processMessage());
    }

    @Test
    void processMessageParsesArgsResetsInputAndReturnsActionResult() {
        Terminal terminal = new Terminal(null, null, null);
        Command command = mock(Command.class);
        when(command.action(any())).thenReturn(true);

        terminal.addCommand("go", command);

        @SuppressWarnings("unchecked")
        Class<ArrayList<String>> captorClass = (Class<ArrayList<String>>) (Class<?>) ArrayList.class;
        var argsCaptor = ArgumentCaptor.forClass(captorClass);

        terminal.setEnteredMessage("go  1   2 3");
        boolean result = terminal.processMessage();
        assertTrue(result);

        verify(command).action(argsCaptor.capture());
        ArrayList<String> args = argsCaptor.getValue();
        assertEquals(List.of("1", "2", "3"), args);

        assertEquals("", terminal.getEnteredMessage());

        reset(command);
        when(command.action(any())).thenReturn(false);
        terminal.setEnteredMessage("go");
        assertFalse(terminal.processMessage());
    }

    @Test
    void appendAndBackspaceModifyMessage() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.appendToMessage('a');
        terminal.appendToMessage('b');
        assertEquals("ab", terminal.getEnteredMessage());

        terminal.handleBackspace();
        assertEquals("a", terminal.getEnteredMessage());

        terminal.handleBackspace();
        terminal.handleBackspace();
        assertEquals("", terminal.getEnteredMessage());
    }

    @Test
    void autocompleteSuggestsByPrefix() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setEnteredMessage("te"); // "teleport" is built-in
        waitPastDebounce();
        List<String> suggestions = terminal.getAutocompleteSuggestions();
        assertTrue(suggestions.contains("teleport"));

        terminal.addCommand("testcmd", args -> true);
        terminal.setEnteredMessage("tes");
        waitPastDebounce();
        suggestions = terminal.getAutocompleteSuggestions();
        assertTrue(suggestions.contains("testcmd"));
    }

    @Test
    void autocompleteDebounceCachesWithinWindow() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setEnteredMessage("kil");
        waitPastDebounce();
        List<String> s1 = terminal.getAutocompleteSuggestions();
        assertFalse(s1.isEmpty());

        // Change input but query immediately (inside debounce): should reuse cached results
        terminal.setEnteredMessage("debug");
        List<String> sImmediate = terminal.getAutocompleteSuggestions();
        assertEquals(s1, sImmediate, "Suggestions should be cached within debounce window");

        // After debounce, it should recompute and differ
        waitPastDebounce();
        List<String> s2 = terminal.getAutocompleteSuggestions();
        assertNotEquals(s1, s2, "Suggestions should refresh after debounce expires");
        assertTrue(s2.contains("debug"));
    }

    @Test
    void autocompleteFallsBackToFuzzyBKTreeWhenNoPrefixMatch() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setEnteredMessage("koll"); // edit distance 1 from "kill"
        waitPastDebounce();
        List<String> suggestions = terminal.getAutocompleteSuggestions();
        assertTrue(suggestions.contains("kill"), "Fuzzy search should suggest 'kill' for 'koll'");
    }

    @Test
    void acceptTopSuggestionReplacesOnlyFirstTokenAndPreservesRestSpacing() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setEnteredMessage("koll   1  2");
        waitPastDebounce();
        var suggestions = terminal.getAutocompleteSuggestions();
        assertFalse(suggestions.isEmpty());
        assertEquals("kill", suggestions.getFirst());

        terminal.acceptTopSuggestion();
        assertEquals("kill   1  2", terminal.getEnteredMessage());
    }

    @Test
    void rebuildAutocompleteIndexClearsCachesAndReindexesDeterministically() {
        Map<String, Command> map = new LinkedHashMap<>();
        Terminal terminal = new Terminal(map, mock(GdxGame.class), mock(CountdownTimerService.class));

        map.clear();
        terminal.rebuildAutocompleteIndex(); // clears structures + caches

        terminal.addCommand("alpha", a -> true);
        terminal.addCommand("beta", a -> true);
        terminal.addCommand("gamma", a -> true);

        terminal.rebuildAutocompleteIndex();

        terminal.setEnteredMessage("be");
        waitPastDebounce();
        List<String> suggestions = terminal.getAutocompleteSuggestions();
        assertEquals(List.of("beta"), suggestions);
    }

    @Test
    void extractFirstToken_handlesNullWhitespaceTabsAndWords() throws Exception {
        assertEquals("", callExtractFirstToken(null));
        assertEquals("", callExtractFirstToken(""));
        assertEquals("", callExtractFirstToken("     "));
        assertEquals("", callExtractFirstToken("\t\t   "));

        assertEquals("cmd", callExtractFirstToken("cmd"));
        assertEquals("cmd", callExtractFirstToken("cmd   "));
        assertEquals("cmd", callExtractFirstToken("   cmd   arg1 arg2"));
        assertEquals("hello", callExtractFirstToken("\t  hello\tworld"));
    }

    @Test
    void stripFirstToken_preservesSpacingAfterToken() throws Exception {
        assertEquals("", callStripFirstToken(null));
        assertEquals("", callStripFirstToken(""));
        assertEquals("", callStripFirstToken("     "));
        assertEquals("", callStripFirstToken("cmd"));
        assertEquals("   arg1  arg2", callStripFirstToken("cmd   arg1  arg2"));
        assertEquals("\targ", callStripFirstToken("cmd\targ"));
        assertEquals("  1 2", callStripFirstToken("go  1 2"));
    }

    @Test
    void acceptTopSuggestion_noSuggestions_isNoOp() {
        Map<String, Command> map = new LinkedHashMap<>();
        Terminal terminal = new Terminal(map, null, null);

        terminal.setEnteredMessage("whatever");
        waitPastDebounce(); // allow debounce to elapse so computation occurs
        assertTrue(terminal.getAutocompleteSuggestions().isEmpty());

        terminal.acceptTopSuggestion(); // should do nothing
        assertEquals("whatever", terminal.getEnteredMessage());
    }

    @Test
    void getAutocompleteSuggestions_respectsSuggestionLimit() {
        Map<String, Command> map = new LinkedHashMap<>();
        Terminal terminal = new Terminal(map, null, null);

        for (int i = 0; i < 10; i++) {
            terminal.addCommand("c" + i, args -> true);
        }

        terminal.setEnteredMessage("c");
        awaitSuggestionsReady(terminal);

        List<String> suggestions = terminal.getAutocompleteSuggestions();
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.size() <= 5, "Suggestions should be capped at 5");
    }

    @Test
    void autocompleteEmptyPrefixReturnsEmptyList() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setEnteredMessage(""); // empty -> should not query trie/bktree
        waitPastDebounce();
        List<String> suggestions = terminal.getAutocompleteSuggestions();
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty(), "Empty prefix should return empty suggestions");
    }

    @Test
    void autocompleteSamePrefixAfterDebounceReturnsCachedInstance() {
        Terminal terminal = new Terminal(null, null, null);
        terminal.setEnteredMessage("deb");

        await().atMost(AT_MOST)
                .until(() -> !terminal.getAutocompleteSuggestions().isEmpty());

        List<String> s1 = terminal.getAutocompleteSuggestions();
        assertFalse(s1.isEmpty());

        // Wait past debounce then assert same instance (no recompute if not dirty)
        await().pollDelay(DEBOUNCE.plus(DEBOUNCE_BUFFER))
                .atMost(AT_MOST)
                .untilAsserted(() -> {
                    List<String> s2 = terminal.getAutocompleteSuggestions();
                    assertSame(s1, s2,
                            "Same prefix after debounce and not dirty should reuse cached list instance");
                });
    }

    @Test
    void autocompleteRecomputesWhenIndexDirtyEvenIfPrefixUnchanged() {
        Map<String, Command> map = new LinkedHashMap<>();
        Terminal terminal = new Terminal(map, null, null);

        terminal.addCommand("teleport", a -> true);
        terminal.addCommand("test", a -> true);

        terminal.setEnteredMessage("te");
        waitPastDebounce();
        List<String> before = terminal.getAutocompleteSuggestions();
        assertTrue(before.contains("teleport"));
        assertFalse(before.contains("tele"));

        // Adding a new matching command marks indexDirty = true
        terminal.addCommand("tele", a -> true);

        // Same prefix, after debounce it should recompute and include "tele"
        waitPastDebounce();
        List<String> after = terminal.getAutocompleteSuggestions();
        assertTrue(after.contains("tele"), "Newly added command should appear after dirty reindex");
        assertNotSame(before, after, "Dirty flag should trigger a fresh suggestions list");
    }

    @Test
    void autocompleteWithinDebounceReturnsExactlySameListEvenIfInputChanged() {
        Terminal terminal = new Terminal(null, null, null);

        terminal.setEnteredMessage("kil"); // "kill" exists
        waitPastDebounce();
        List<String> baseline = terminal.getAutocompleteSuggestions();
        assertFalse(baseline.isEmpty());

        // Change input but immediately query (inside debounce window)
        terminal.setEnteredMessage("debug");
        List<String> stillBaseline = terminal.getAutocompleteSuggestions();

        assertSame(baseline, stillBaseline, "Within debounce window, suggestions must be reused");
    }

    @Test
    void autocompleteSuggestionsAreCappedAtSuggestionLimit() {
        Map<String, Command> map = new LinkedHashMap<>();
        Terminal terminal = new Terminal(map, null, null);

        for (int i = 0; i < 7; i++) {
            terminal.addCommand("xCommand" + i, args -> true);
        }

        terminal.setEnteredMessage("x");
        waitPastDebounce();
        List<String> suggestions = terminal.getAutocompleteSuggestions();

        assertTrue(suggestions.size() <= 5,
                "Suggestions should be capped at 5 when more are available");
        suggestions.forEach(s -> assertTrue(s.startsWith("x")));
    }

    @Test
    void autocompleteLimitBranch_viaBKTreeFallback_returnsSingleBest() {
        Map<String, Command> map = new LinkedHashMap<>();
        Terminal terminal = new Terminal(map, null, null);

        map.clear();
        terminal.rebuildAutocompleteIndex();

        // All within distance 1 of "aa"
        for (String name : List.of("a", "aaa", "ba", "ca", "ab", "za")) {
            terminal.addCommand(name, args -> true);
        }

        terminal.setEnteredMessage("aa");
        await().atMost(AT_MOST)
                .until(() -> !terminal.getAutocompleteSuggestions().isEmpty());

        List<String> suggestions = terminal.getAutocompleteSuggestions();
        assertEquals(1, suggestions.size(),
                "BK-tree fallback should return a single best suggestion");
    }

    @Test
    void suggestionListIsCappedToFive_viaStubbedTrie() throws Exception {
        Map<String, Command> map = new LinkedHashMap<>();
        Terminal terminal = new Terminal(map, null, null);

        // Inject stub trie
        Field trieField = Terminal.class.getDeclaredField("trie");
        trieField.setAccessible(true);
        trieField.set(terminal, new StubRadixTrie());

        // Mark index dirty so Terminal recomputes with the stubbed trie
        Field indexDirtyField = Terminal.class.getDeclaredField("indexDirty");
        indexDirtyField.setAccessible(true);
        indexDirtyField.setBoolean(terminal, true);

        terminal.setEnteredMessage("anything");
        waitPastDebounce();

        List<String> suggestions = terminal.getAutocompleteSuggestions();

        assertEquals(5, suggestions.size(), "Should cap suggestions to 5 when hits > 5");
        assertEquals(new ArrayList<>(List.of("hit0", "hit1", "hit2", "hit3", "hit4")),
                new ArrayList<>(suggestions));
    }

    static class StubRadixTrie extends RadixTrie {
        @Override
        public List<String> suggestTopK(String prefix) {
            if (prefix == null || prefix.isBlank()) return List.of();
            return List.of("hit0", "hit1", "hit2", "hit3", "hit4", "hit5", "hit6");
        }
    }
}
