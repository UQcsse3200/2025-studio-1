package com.csse3200.game.ui.terminal;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.ui.terminal.autocomplete.BKTree;
import com.csse3200.game.ui.terminal.autocomplete.RadixTrie;
import com.csse3200.game.ui.terminal.commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Terminal extends Component {
    private static final Logger logger = LoggerFactory.getLogger(Terminal.class);

    private final Map<String, Command> commands;
    private String enteredMessage = "";
    private boolean isOpen = false;

    // --- Autocomplete state ---
    private final RadixTrie trie = new RadixTrie();
    private final BKTree bkTree = new BKTree();
    private volatile boolean indexDirty = false;

    // debounce ~20ms
    private static final long DEBOUNCE_NS = 20_000_000L;
    private long lastKeystrokeNs = 0L;

    // cache last suggestions for the current prefix
    private String lastPrefix = "";
    private List<String> lastSuggestions = Collections.emptyList();

    public Terminal() {
        this(new HashMap<>(), null);
    }

    public Terminal(GdxGame game) {
        this(new HashMap<>(), game);
    }

    public Terminal(Map<String, Command> commands) {
        this(commands, null);
    }

    public Terminal(Map<String, Command> commands, GdxGame game) {
        this.commands = commands;

        addCommand("debug", new DebugCommand());
        addCommand("winscreen", new EndScreenCommand(game, GdxGame.ScreenType.WIN_SCREEN));
        addCommand("deathscreen", new EndScreenCommand(game, GdxGame.ScreenType.DEATH_SCREEN));
        addCommand("disableDamage", new DisableDamageCommand());
        addCommand("waves", new WavesCommand());
        addCommand("damageMultiplier", new DamageMultiplierCommand());
        addCommand("pickupAll", new PickupAllCommand());
        addCommand("infiniteStamina", new InfiniteStaminaCommand());
        addCommand("infiniteDash", new InfiniteDashCommand());
        addCommand("infiniteJumps", new InfiniteJumpsCommand());
        addCommand("spawn", new SpawnCommand());
        addCommand("doorOverride", new DoorOverrideCommand());
        addCommand("teleport", new TeleportCommand());
        addCommand("travel", new TravelCommand());

        // Initial index build
        rebuildAutocompleteIndex();
    }

    // --- Public getters ---
    public String getEnteredMessage() {
        return enteredMessage;
    }

    public boolean isOpen() {
        return isOpen;
    }

    // --- Open/close ---
    public void toggleIsOpen() {
        if (isOpen) setClosed();
        else setOpen();
    }

    public void setOpen() {
        logger.debug("Opening terminal");
        isOpen = true;
    }

    public void setClosed() {
        logger.debug("Closing terminal");
        isOpen = false;
        setEnteredMessage("");
    }

    // --- Commands registry ---
    public void addCommand(String name, Command command) {
        logger.debug("Adding command: {}", name);
        if (commands.containsKey(name)) {
            logger.error("Command {} is already registered", name);
        }
        commands.put(name, command);
        // update index incrementally
        trie.insert(name);
        bkTree.insert(name);
    }

    // Optional: if you bulk-load or mutate names externally
    public void rebuildAutocompleteIndex() {
        // Rebuild from scratch
        // (RadixTrie/BKTree here have no explicit clear; relying on GC if you replace instances)
        // For clarity we just populate into the existing structures on first call,
        // then mark indexDirty=false.
        // If you truly need full rebuild, create new instances and replace references.
        for (String name : commands.keySet()) {
            trie.insert(name);
            bkTree.insert(name);
        }
        indexDirty = false;
    }

    // --- Processing ---
    public boolean processMessage() {
        logger.debug("Processing message");
        String message = enteredMessage.trim();
        String[] sections = message.split(" ");
        String command = sections[0];
        ArrayList<String> args = new ArrayList<>(Arrays.asList(sections).subList(1, sections.length));

        if (commands.containsKey(command)) {
            setEnteredMessage("");
            return commands.get(command).action(args);
        }
        return false;
    }

    // --- Typing handlers (update debounce + invalidate suggestion cache) ---
    public void appendToMessage(char character) {
        logger.debug("Appending '{}' to message", character);
        enteredMessage = enteredMessage + character;
        touchKeystroke();
    }

    public void handleBackspace() {
        logger.debug("Handling backspace");
        int messageLength = enteredMessage.length();
        if (messageLength != 0) {
            enteredMessage = enteredMessage.substring(0, messageLength - 1);
            touchKeystroke();
        }
    }

    public void setEnteredMessage(String text) {
        enteredMessage = text != null ? text : "";
        touchKeystroke();
    }

    // --- Autocomplete surface API for UI layer (TerminalDisplay) ---

    /**
     * Returns up to 5 suggestions based on the current prefix (first token).
     * Debounced (~20ms). If there are no trie hits and prefix non-empty, falls back to BK-tree with
     * edit distance ≤ 1. For empty prefix, returns empty.
     */
    public List<String> getAutocompleteSuggestions() {
        final long now = System.nanoTime();
        if (now - lastKeystrokeNs < DEBOUNCE_NS) {
            return lastSuggestions; // still within debounce window; reuse last
        }

        String prefix = extractFirstToken(enteredMessage).trim();
        if (!Objects.equals(prefix, lastPrefix) || indexDirty) {
            List<String> hits = Collections.emptyList();
            if (!prefix.isEmpty()) {
                hits = trie.suggestTopK(prefix);
                if (hits.isEmpty()) {
                    // fuzzy fallback: edit distance ≤ 1
                    hits = bkTree.searchWithin(prefix, 1);
                }
            }
            lastPrefix = prefix;
            lastSuggestions = hits.size() > 5 ? hits.subList(0, 5) : hits;
            indexDirty = false;
        }
        return lastSuggestions;
    }

    /**
     * UI can call this to accept the top suggestion into the input.
     */
    public void acceptTopSuggestion() {
        List<String> s = getAutocompleteSuggestions();
        if (!s.isEmpty()) {
            // replace first token with suggestion
            String rest = stripFirstToken(enteredMessage);
            setEnteredMessage(s.get(0) + rest);
        }
    }

    // --- helpers ---
    private void touchKeystroke() {
        lastKeystrokeNs = System.nanoTime();
        // invalidate cache for prefix
        lastPrefix = null;
    }

    private static String extractFirstToken(String s) {
        if (s == null) return "";
        int i = 0;
        // skip leading spaces
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        int start = i;
        while (i < s.length() && !Character.isWhitespace(s.charAt(i))) i++;
        return s.substring(start, i);
    }

    private static String stripFirstToken(String s) {
        if (s == null) return "";
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        while (i < s.length() && !Character.isWhitespace(s.charAt(i))) i++;
        return s.substring(i); // retains the whitespace before args, OK for your parser
    }
}
