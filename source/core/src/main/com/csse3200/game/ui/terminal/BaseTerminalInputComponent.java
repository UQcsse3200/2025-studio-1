package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.InputProcessor;
import com.csse3200.game.input.InputComponent;

public abstract class BaseTerminalInputComponent extends InputComponent {
    protected Terminal terminal;

    public BaseTerminalInputComponent() {
        super(10);
    }

    public BaseTerminalInputComponent(Terminal terminal) {
        this();
        this.terminal = terminal;
    }

    @Override
    public void create() {
        super.create();
        terminal = entity.getComponent(Terminal.class);
    }

    /**
     * Returns false because this component is not one intended to
     * be paused
     * @return False, as this is a component not affected by pause
     * functionality
     */
    @Override
    protected boolean isPauseable() {
        return false;
    }

    /**
     * If the toggle key is pressed, the terminal will open / close.
     *
     * <p>Otherwise, handles input if the terminal is open. This is because keyDown events are
     * triggered alongside keyTyped events. If the user is typing in the terminal, the input shouldn't
     * trigger any other input handlers.
     *
     * @return whether the input was processed
     * @see InputProcessor#keyDown(int)
     */
    @Override
    public abstract boolean keyPressed(int keycode);

    /**
     * Handles input if the terminal is open. If 'enter' is typed, the entered message will be
     * processed, otherwise the message will be updated with the new character.
     *
     * @return whether the input was processed
     * @see InputProcessor#keyTyped(char)
     */
    @Override
    public boolean keyTyped(char character) {
        if (!terminal.isOpen()) {
            return false;
        }

        if (character == '\b') {
            terminal.handleBackspace();
            return true;
        } else if (character == '\r' || character == '\n') {
            if (terminal.processMessage()) {
                terminal.toggleIsOpen();
            }
            terminal.setEnteredMessage("");
            return true;
        } else if(Character.isLetterOrDigit(character) || character == ' ') {
            // append character to message
            terminal.appendToMessage(character);
            return true;
        }
        return false;
    }

    /**
     * Handles input if the terminal is open. This is because keyUp events are
     * triggered alongside keyTyped events. If the user is typing in the terminal, the input shouldn't
     * trigger any other input handlers.
     *
     * @return whether the input was processed
     * @see InputProcessor#keyUp(int)
     */
    @Override
    public boolean keyReleased(int keycode) {
        return terminal.isOpen();
    }
}