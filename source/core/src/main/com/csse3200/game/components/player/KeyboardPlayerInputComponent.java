package com.csse3200.game.components.player;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.ItemComponent;
import com.csse3200.game.entities.configs.ItemTypes;
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
//        entity.getEvents().trigger("attack");
//        entity.getEvents().trigger("anim");
        return true;

      default:
        return false;
    }
  }

  /**
   * Handles mouse button presses.
   * If the player clicks the left mouse button, this
   * triggers a ranged or melee attack depending on
   * the currently equipped item.
   *
   * @param screenX the x-coordinate of the touch in screen space
   * @param screenY the y-coordinate of the touch in screen space
   * @param pointer the pointer index for the event
   * @param button  the mouse button pressed
   * @return true if the input was handled, false otherwise
   */
  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button == Input.Buttons.LEFT) {
      InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
      Entity item = inventory.get(focusedItem);
      if (item == null){
        return false;
      }

      ItemComponent itemInfo = item.getComponent(ItemComponent.class);
      if (itemInfo.getType() == ItemTypes.RANGED) {
        entity.getEvents().trigger("shoot");

      } else if (itemInfo.getType() != ItemTypes.MELEE) {
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
        focusedItem = 4;
        triggerSelectItem();
        return true;
      case Keys.E:
        triggerAddItem();
        return true;
      case Keys.R:
        triggerDropFocused();
        return true;
      default:
        return false;
    }
  }

  /**
   * Checks if the current key press should trigger a dash action.
   * Uses timing between consecutive presses of the same key.
   *
   * @param keycode the code of the key that was pressed
   */
  private void checkForDashInput(int keycode) {
    if (isDoubleKeyPress(keycode)) {
      entity.getEvents().trigger("dashAttempt");
    }
  }

  /**
   * Determines if a key press is a valid double press
   * based on timing and key code.
   *
   * @param keycode the code of the key being checked
   * @return true if the key press qualifies as a double press
   */
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

  /**
   * Updates tracking data for double key press detection.
   *
   * @param keycode the key code that was just pressed
   */
  private void updateDoubleKeyPress(int keycode) {
    timeSinceKeyPress = System.currentTimeMillis();
    doublePressKeyCode = keycode;
  }

  /**
   * Triggers either a walk or stop walking event based
   * on the current walking direction.
   */
  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection);
    }
  }

  /**
   * Triggers crouch attempt event.
   */
  private void triggerCrouchEvent() {
    entity.getEvents().trigger("crouchAttempt");
  }

  /**
   * Triggers stop crouching event.
   */
  private void triggerStopCrouchingEvent() {
    entity.getEvents().trigger("crouchStop");
  }

  /**
   * Triggers sprint attempt event.
   */
  private void triggerSprintEvent() {
    entity.getEvents().trigger("sprintAttempt");
  }

  /**
   * Triggers sprint stop event.
   */
  private void triggerStopSprintingEvent() {
    entity.getEvents().trigger("sprintStop");
  }

  /**
   * Triggers jump attempt event.
   */
  private void triggerJumpEvent() {
    entity.getEvents().trigger("jumpAttempt");
  }

   /** Triggers an inventory removal request for the currently focused slot. */
  private void triggerRemoveItem() {
    entity.getEvents().trigger("remove item", focusedItem);
  }

  /** Triggers an item pickup request. */
  private void triggerAddItem() {
    System.out.println("Pick up event triggered");
    entity.getEvents().trigger("pick up");

  }

  /** Triggers a change in the currently focused inventory slot. */
  private void triggerSelectItem() {
    entity.getEvents().trigger("focus item", focusedItem);
  }

  /** Triggers a drop request for the currently focused inventory slot. */
  private void triggerDropFocused() {
    entity.getEvents().trigger("drop focused");
  }
}

