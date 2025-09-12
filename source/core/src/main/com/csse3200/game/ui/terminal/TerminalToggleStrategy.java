package com.csse3200.game.ui.terminal;

public interface TerminalToggleStrategy {
    /** Return true if handled (i.e., you toggled or changed state). */
    boolean onKeyDown(Terminal terminal, int keycode);
    /** Return true if handled. Default: false (keyboard won’t use scrolling). */
    default boolean onScrolled(Terminal terminal, float amountX, float amountY) { return false; }
}
