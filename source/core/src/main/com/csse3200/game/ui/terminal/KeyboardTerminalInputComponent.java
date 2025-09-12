package com.csse3200.game.ui.terminal;

/**
 * Input handler for the debug terminal for keyboard and touch (mouse) input.
 * This input handler only uses keyboard input.
 *
 * <p>The debug terminal can be opened and closed by pressing 'F1' and a message can be entered via
 * the keyboard.
 */
public class KeyboardTerminalInputComponent extends BaseTerminalInputComponent {
  public KeyboardTerminalInputComponent() { super(new KeyboardToggleStrategy()); }

  public KeyboardTerminalInputComponent(TerminalToggleStrategy strategy) { super(strategy); }
}
