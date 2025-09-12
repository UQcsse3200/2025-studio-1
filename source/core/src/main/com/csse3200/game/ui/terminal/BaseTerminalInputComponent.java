package com.csse3200.game.ui.terminal;

import com.csse3200.game.input.InputComponent;
import java.util.Objects;

/**
 * Template for terminal input components
 * Holds the flow and delegates toggling to a strategy.
 */
public abstract class BaseTerminalInputComponent extends InputComponent {
    protected Terminal terminal;
    private final TerminalToggleStrategy toggleStrategy;
    private final TextPolicy textPolicy;

    protected BaseTerminalInputComponent(TerminalToggleStrategy strategy) {
        this(strategy, new DefaultTextPolicy());
    }

    protected BaseTerminalInputComponent(TerminalToggleStrategy strategy, TextPolicy textPolicy) {
        super(10);
        this.toggleStrategy = Objects.requireNonNull(strategy, "toggleStrategy");
        this.textPolicy = Objects.requireNonNull(textPolicy, "textPolicy");
    }

    @Override
    public void create() {
        super.create();
        terminal = entity.getComponent(Terminal.class);
        if (terminal == null) {
            throw new IllegalStateException("Terminal compoment not found on entity");
        }
    }

    // --- template flow ---
    @Override
    public boolean keyDown(int keycode) {
        // First let strategy handle toggling/open/close gestures
        if (toggleStrategy.onKeyDown(terminal, keycode)) return true;
        // If terminal is open, consume so other handlers don’t process
        return terminal.isOpen();
    }

    @Override
    public boolean keyTyped(char character) {
        if (!terminal.isOpen()) return false;

        if (character == '\b') {
            terminal.handleBackspace();
            return true;
        }
        if (character == '\r' || character == '\n') {
            if (terminal.processMessage()) {
                terminal.toggleIsOpen();
            }
            terminal.setEnteredMessage("");
            return true;
        }
        if (textPolicy.isAllowed(character)) {
            terminal.appendToMessage(character);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        // While open, consume keyUp to avoid leaking to other handlers
        return terminal.isOpen();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) { return toggleStrategy.onScrolled(terminal, amountX, amountY); }
}
