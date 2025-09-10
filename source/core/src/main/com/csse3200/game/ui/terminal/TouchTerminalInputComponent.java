package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.InputProcessor;

/**
 * Input handler for the debug terminal for keyboard and touch (mouse) input.
 * This input handler uses keyboard and touch input.
 *
 * <p>The debug terminal can be opened and closed by scrolling vertically and a message can be entered via
 * the keyboard.
 */
public class TouchTerminalInputComponent extends BaseTerminalInputComponent {

  public TouchTerminalInputComponent(Terminal terminal) {
      super(terminal);
  }

  /**
   * Handles input if the terminal is open. This is because keyDown events are
   * triggered alongside keyTyped events. If the user is typing in the terminal, the input shouldn't
   * trigger any other input handlers.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyDown(int)
   */
  @Override
  public boolean keyPressed(int keycode) {
    return terminal.isOpen();
  }

  /**
   * Scrolling up will open the terminal and scrolling down will close the terminal.
   *
   * @return whether the input was processed
   * @see InputProcessor#scrolled(float, float)
   */
  @Override
  public boolean scrolled(float amountX, float amountY) {
    if (amountY < 0) {
      terminal.setOpen();
    } else if (amountY > 0) {
      terminal.setClosed();
    }
    return true;
  }
}