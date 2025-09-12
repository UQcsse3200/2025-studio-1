package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.InputProcessor;
import com.csse3200.game.input.InputComponent;

/**
 * Input handler for the debug terminal for keyboard and touch (mouse) input.
 * This input handler uses keyboard and touch input.
 *
 * <p>The debug terminal can be opened and closed by scrolling vertically and a message can be entered via
 * the keyboard.
 */
public class TouchTerminalInputComponent extends BaseTerminalInputComponent {
    private Terminal terminal;

    public TouchTerminalInputComponent() {
        super(new ScrollToggleStrategy());
    }

    public TouchTerminalInputComponent(TerminalToggleStrategy strategy) { super(strategy); }
}