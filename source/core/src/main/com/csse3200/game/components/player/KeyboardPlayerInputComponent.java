package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.utils.math.Vector2Utils;

/**
 * Input handler for the player for keyboard and touch (mouse) input.
 * This input handler only uses keyboard input.
 */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private int DASH_COOLDOWN = 3;
  private int focusedItem = -1;

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
        return true;
      case Keys.D:
        walkDirection.add(Vector2Utils.RIGHT);
        triggerWalkEvent();
        return true;
      case Keys.S:
        entity.getEvents().trigger("crouchAttempt");
        return true;
      case Keys.SHIFT_LEFT: // sprint start (left shift down)
        entity.getEvents().trigger("sprintAttempt");
        triggerWalkEvent();
        return true;
      case Keys.SPACE:
        jump();
        return true;
      case Keys.Q:
        entity.getEvents().trigger("dashAttempt");
        return true;
      // TODO: add in item/weapon usage
      default:
        return false;
    }
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
      case Keys.SHIFT_LEFT: // sprint stop (left shift up)
        entity.getEvents().trigger("walkStop");
        entity.getEvents().trigger("sprintStop");
        triggerWalkEvent();
        return true;
      case Keys.S:
        entity.getEvents().trigger("crouchStop");
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

  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection);
    }
  }

  private void jump() {
    entity.getEvents().trigger("jump");
  }

  /**
   * Cheatcode: infinite dashes
   */
  public void infDash() {
    this.DASH_COOLDOWN = 0;
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
