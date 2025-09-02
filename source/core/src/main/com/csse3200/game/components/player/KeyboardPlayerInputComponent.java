package com.csse3200.game.components.player;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.TagComponent;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.utils.math.Vector2Utils;

/**
 * Input handler for the player for keyboard and touch (mouse) input.
 * This input handler only uses keyboard input.
 */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();

  private int focusedItem = -1;

  private long timeSinceKeyPress = 0;
  private int doublePressKeyCode = -1;

  public KeyboardPlayerInputComponent() {
    super(5);
  }

  /**
   * Triggers player events on specific keycodes.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyDown(int)
   */
  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Keys.A:
        walkDirection.add(Vector2Utils.LEFT);
        triggerWalkEvent();
        checkForDashInput(keycode);
        return true;

      case Keys.D:
        walkDirection.add(Vector2Utils.RIGHT);
        triggerWalkEvent();
        checkForDashInput(keycode);
        return true;

      case Keys.S:
        triggerCrouchEvent();
        return true;

      case Keys.SHIFT_LEFT:
        triggerSprintEvent();
        return true;

      case Keys.SPACE:
        triggerJumpEvent();
        entity.getEvents().trigger("attack");
        entity.getEvents().trigger("anim");
        return true;

      default:
        return false;
    }
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button == Input.Buttons.LEFT) {

      if (entity.getCurrItem() == null){

        return true;
      }

      if (entity.getCurrItem().getComponent(TagComponent.class).getTag().equals("ranged")){

        entity.getEvents().trigger("shoot");
      }
      else {


        entity.getEvents().trigger("attack");
      }
        return true;
    }
    return false;
  }

  /**
   * Triggers player events on specific keycodes.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyUp(int)
   */
  @Override
  public boolean keyUp(int keycode) {
    switch (keycode) {
      case Keys.A:
        walkDirection.sub(Vector2Utils.LEFT);
        triggerWalkEvent();
        return true;

      case Keys.D:
        walkDirection.sub(Vector2Utils.RIGHT);
        triggerWalkEvent();
        return true;

      case Keys.SHIFT_LEFT:
        triggerStopSprintingEvent();
        return true;

      case Keys.S:
        triggerStopCrouchingEvent();
        return true;

      case Keys.Q:
        triggerRemoveItem();
        return true;
      case Keys.NUM_1:
        focusedItem = 0;
        triggerSelectItem();
        return true;
      case Keys.NUM_2:
        focusedItem = 1;
        triggerSelectItem();
        return true;
      case Keys.NUM_3:
        focusedItem = 2;
        triggerSelectItem();
        return true;
      case Keys.NUM_4:
        focusedItem = 3;
        triggerSelectItem();
        return true;
      case Keys.NUM_5:
        triggerSelectItem();
        focusedItem = 4;
        return true;
      case Keys.P:
        triggerAddItem();
        return true;
      default:
        return false;
    }
  }

  private void checkForDashInput(int keycode) {
    if (isDoubleKeyPress(keycode)) {
      entity.getEvents().trigger("dashAttempt");
    }
  }

  private boolean isDoubleKeyPress(int keycode) {
    boolean validDoubleKey = false;
    long timeDif = System.currentTimeMillis() - timeSinceKeyPress;
    long DOUBLE_KEY_INTERVAL = 300;
    if (keycode == doublePressKeyCode || timeDif < DOUBLE_KEY_INTERVAL) {
      validDoubleKey = true;
    }
    updateDoubleKeyPress(keycode);
    return validDoubleKey;
  }

  private void updateDoubleKeyPress(int keycode) {
    timeSinceKeyPress = System.currentTimeMillis();
    doublePressKeyCode = keycode;
  }

  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection);
    }
  }

  private void triggerCrouchEvent() {
    entity.getEvents().trigger("crouchAttempt");
  }

  private void triggerStopCrouchingEvent() {
    entity.getEvents().trigger("crouchStop");
  }

  private void triggerSprintEvent() {
    entity.getEvents().trigger("sprintAttempt");
  }

  private void triggerStopSprintingEvent() {
    entity.getEvents().trigger("sprintStop");
  }

  private void triggerJumpEvent() {
    entity.getEvents().trigger("jumpAttempt");
  }

  private void triggerRemoveItem() {
    entity.getEvents().trigger("remove item", focusedItem);
  }

  private void triggerAddItem() {
    entity.getEvents().trigger("add item", "images/mud.png");
  }

  private void triggerSelectItem() {
    entity.getEvents().trigger("focus item", focusedItem);
  }
}
