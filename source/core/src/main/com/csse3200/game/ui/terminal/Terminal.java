package com.csse3200.game.ui.terminal;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.CountdownTimerService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.autocomplete.BKTree;
import com.csse3200.game.ui.terminal.autocomplete.RadixTrie;
import com.csse3200.game.ui.terminal.commands.*;
import com.csse3200.game.areas.StaticBossRoom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Terminal extends Component {
    private static final Logger logger = LoggerFactory.getLogger(Terminal.class);

    // --- Autocomplete tuning ---
    private static final int SUGGESTION_LIMIT = 5;
    private static final int FUZZY_DISTANCE = 1;
    private static final long DEBOUNCE_NS = 20_000_000L; // ~20ms

    private final Map<String, Command> commands;

    private String enteredMessage = "";
    private boolean isOpen = false;

    // Autocomplete state
    // Recreated on full rebuild for a true "clear and reindex".
    private RadixTrie trie = new RadixTrie();
    private BKTree bkTree = new BKTree();
    private volatile boolean indexDirty = false;

    private long lastKeystrokeNs = 0L;
    private String lastPrefix = "";
    private List<String> lastSuggestions = Collections.emptyList();

    public Terminal(Map<String, Command> commands, GdxGame game, CountdownTimerService timer) {
        this.commands = (commands != null) ? commands : new LinkedHashMap<>();

        addCommand("damageMultiplier", new DamageMultiplierCommand());
        addCommand("debug", new DebugCommand());
        addCommand("deathScreen", new EndScreenCommand(game, GdxGame.ScreenType.DEATH_SCREEN, timer, false));
        addCommand("disableDamage", new DisableDamageCommand());
        addCommand("doorOverride", new DoorOverrideCommand());
        addCommand("infiniteStamina", new InfiniteStaminaCommand());
        addCommand("infiniteDash", new InfiniteDashCommand());
        addCommand("infiniteJumps", new InfiniteJumpsCommand());
        addCommand("doorOverride", new DoorOverrideCommand());
//        addCommand("teleport", new TeleportCommand());

        // Initial index build
        addCommand("kill", new KillCommand());
        addCommand("pickupAll", new PickupAllCommand());
        addCommand("spawn", new SpawnCommand());
        addCommand("teleport", new TeleportCommand());
        addCommand("travel", new TravelCommand());
        addCommand("waves", new WavesCommand());
        addCommand("winScreen", new EndScreenCommand(game, GdxGame.ScreenType.WIN_SCREEN, timer, true));

        rebuildAutocompleteIndex();
    }

    private static String extractFirstToken(String s) {
        if (s == null) return "";
        int i = 0;
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
        return s.substring(i); // retains whitespace before args, OK for your parser
    }

    // --- Public getters ---
    public String getEnteredMessage() {
        return enteredMessage;
    }

    public void setEnteredMessage(String text) {
        enteredMessage = Objects.requireNonNullElse(text, "");
        touchKeystroke();
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
        Objects.requireNonNull(name, "command name");
        Objects.requireNonNull(command, "command");
        logger.debug("Adding command: {}", name);

        if (commands.containsKey(name)) {
            logger.error("Command {} is already registered", name);
        }
        commands.put(name, command);

        // incremental index update
        trie.insert(name);
        bkTree.insert(name);
        indexDirty = true; // signal cache invalidation for suggestions
    }

    /**
     * Full reindex: rebuild tries from the current command set.
     */
    public void rebuildAutocompleteIndex() {
        // recreate structures for a real "clear + build"
        trie = new RadixTrie();
        bkTree = new BKTree();

        // to keep things deterministic, iterate keys in sorted order
        var names = new ArrayList<>(commands.keySet());
        Collections.sort(names);
        for (var name : names) {
            trie.insert(name);
            bkTree.insert(name);
        }
        indexDirty = false;
        lastPrefix = "";            // invalidate cached prefix
        lastSuggestions = List.of();// and cached suggestions
    }

    // --- Processing ---
    public boolean processMessage() {
        logger.debug("Processing message");
        var message = enteredMessage.strip();
        if (message.isEmpty()) return false;


        // When the player types a password into the terminal, this method checks if it
        // matches the required code. If the password is correct (in this case "0000"):
        if (message.equals("0000")) {
            if (StaticBossRoom.exposedRightDoor != null) {
                StaticBossRoom.exposedRightDoor.setLocked(false);
                setEnteredMessage("");
                ServiceLocator.getGlobalEvents().trigger("unlock");
                return true;
            }
        }

        var parts = message.split("\\s+");
        var commandName = parts[0];
        var args = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length)));

        var cmd = commands.get(commandName);
        if (cmd == null) return false;

        setEnteredMessage("");
        return cmd.action(args);
    }

    // --- Autocomplete surface API for UI layer (TerminalDisplay) ---

    // --- Typing handlers (update debounce + invalidate suggestion cache) ---
    public void appendToMessage(char character) {
        logger.debug("Appending '{}' to message", character);
        enteredMessage = enteredMessage + character;
        touchKeystroke();
    }

    public void handleBackspace() {
        logger.debug("Handling backspace");
        var len = enteredMessage.length();
        if (len > 0) {
            enteredMessage = enteredMessage.substring(0, len - 1);
            touchKeystroke();
        }
    }

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

        var prefix = extractFirstToken(enteredMessage).trim();
        if (!Objects.equals(prefix, lastPrefix) || indexDirty) {
            List<String> hits = List.of();
            if (!prefix.isEmpty()) {
                hits = trie.suggestTopK(prefix);
                if (hits.isEmpty()) {
                    // fuzzy fallback: edit distance ≤ 1
                    hits = bkTree.searchWithin(prefix, FUZZY_DISTANCE);
                }
            }
            lastPrefix = prefix;
            lastSuggestions = hits.size() > SUGGESTION_LIMIT ? hits.subList(0, SUGGESTION_LIMIT) : hits;
            indexDirty = false;
        }
        return lastSuggestions;
    }

    /**
     * UI can call this to accept the top suggestion into the input.
     */
    public void acceptTopSuggestion() {
        var suggestions = getAutocompleteSuggestions();
        if (suggestions.isEmpty()) return;

        var rest = stripFirstToken(enteredMessage);
        setEnteredMessage(suggestions.getFirst() + rest);
    }

    // --- helpers ---
    private void touchKeystroke() {
        lastKeystrokeNs = System.nanoTime();
        lastPrefix = null; // invalidate cached prefix
    }
}
