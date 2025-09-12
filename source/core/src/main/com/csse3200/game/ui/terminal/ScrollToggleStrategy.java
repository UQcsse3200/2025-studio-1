package com.csse3200.game.ui.terminal;

public final class ScrollToggleStrategy implements TerminalToggleStrategy {
    @Override
    public boolean onKeyDown(Terminal terminal, int keycode) {
        // Touch/scroll driven; keyboard not used for toggling here
        return false;
    }

    @Override
    public boolean onScrolled(Terminal terminal, float amountX, float amountY) {
        if (amountY < 0) {
            terminal.setOpen();
            return true;
        } else if (amountY > 0) {
            terminal.setClosed();
            return true;
        }
        return false;
    }
}
