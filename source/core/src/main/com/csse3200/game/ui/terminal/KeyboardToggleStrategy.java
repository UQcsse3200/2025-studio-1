package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.Input;

public final class KeyboardToggleStrategy implements TerminalToggleStrategy {
    private final int toggleKey;

    public KeyboardToggleStrategy() {
        this(Input.Keys.F1);
    }

    public KeyboardToggleStrategy(int toggleKey) {
        this.toggleKey = toggleKey;
    }

    @Override
    public boolean onKeyDown(Terminal terminal, int keycode) {
        if (keycode == toggleKey) {
            terminal.toggleIsOpen();
            return true;
        }
        return false;
    }
}
