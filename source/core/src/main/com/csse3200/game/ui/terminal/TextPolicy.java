package com.csse3200.game.ui.terminal;

/** Pluggable policy for what characters the terminal accepts. */
public interface TextPolicy {
    boolean isAllowed(char c);
}

class DefaultTextPolicy implements TextPolicy {
    @Override public boolean isAllowed(char c) {
        return Character.isLetterOrDigit(c) || c == ' ';
    }
}
