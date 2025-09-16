package com.csse3200.game.components.player;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.utils.math.Vector2Utils;

/**
 * Keyboard input using ARROW KEYS for movement while preserving all gameplay actions
 * (jump, crouch, sprint, dash, inventory, mouse attack/shoot).
 */
public class ArrowKeysPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private int focusedItem = 0;
  private long timeSinceKeyPress = 0;
  private int doublePressKeyCode = -1;

  public ArrowKeysPlayerInputComponent() {
    super(5);
  }

  @Override
  public boolean keyPressed(int keycode) {
    switch (keycode) {
      case Input.Keys.LEFT:
        walkDirection.add(Vector2Utils.LEFT);
        triggerWalkEvent();
        checkForDashInput(keycode);
        return true;
      case Input.Keys.RIGHT:
        walkDirection.add(Vector2Utils.RIGHT);
        triggerWalkEvent();
        checkForDashInput(keycode);
        return true;
      case Input.Keys.DOWN:
        triggerCrouchEvent();
        return true;
      case Input.Keys.SHIFT_LEFT:
      case Input.Keys.SHIFT_RIGHT:
        triggerSprintEvent();
        return true;
      case Input.Keys.UP:
      case Input.Keys.SPACE:
        triggerJumpEvent();
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean keyReleased(int keycode) {
    switch (keycode) {
      case Input.Keys.LEFT:
        walkDirection.sub(Vector2Utils.LEFT);
        triggerWalkEvent();
        return true;
      case Input.Keys.RIGHT:
        walkDirection.sub(Vector2Utils.RIGHT);
        triggerWalkEvent();
        return true;
      case Input.Keys.SHIFT_LEFT:
      case Input.Keys.SHIFT_RIGHT:
        triggerStopSprintingEvent();
        return true;
      case Input.Keys.DOWN:
        triggerStopCrouchingEvent();
        return true;
      case Input.Keys.Q:
        triggerRemoveItem();
        return true;
      case Input.Keys.NUM_1:
        focusedItem = 0; triggerSelectItem(); return true;
      case Input.Keys.NUM_2:
        focusedItem = 1; triggerSelectItem(); return true;
      case Input.Keys.NUM_3:
        focusedItem = 2; triggerSelectItem(); return true;
      case Input.Keys.NUM_4:
        focusedItem = 3; triggerSelectItem(); return true;
      case Input.Keys.NUM_5:
        focusedItem = 4; triggerSelectItem(); return true;
      case Input.Keys.E:
        triggerAddItem();
        return true;
      case Input.Keys.R:
        triggerDropFocused();
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button == Input.Buttons.LEFT) {
      InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
      Entity item = inventory.get(focusedItem);
      if (item == null) {
        return false;
      }
      ItemComponent itemInfo = item.getComponent(ItemComponent.class);
      if (itemInfo.getType() == ItemTypes.RANGED) {
        entity.getEvents().trigger("shoot");
      } else if (itemInfo.getType() == ItemTypes.MELEE) {
        entity.getEvents().trigger("attack");
      }
      return true;
    }
    return false;
  }

  private void checkForDashInput(int keycode) {
    if (isDoubleKeyPress(keycode)) {
      entity.getEvents().trigger("dashAttempt");
    }
  }

  private boolean isDoubleKeyPress(int keycode) {
    boolean valid = false;
    long dt = System.currentTimeMillis() - timeSinceKeyPress;
    long DOUBLE_KEY_INTERVAL = 300;
    if (keycode == doublePressKeyCode || dt < DOUBLE_KEY_INTERVAL) {
      valid = true;
    }
    timeSinceKeyPress = System.currentTimeMillis();
    doublePressKeyCode = keycode;
    return valid;
  }

  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection);
    }
  }
  private void triggerCrouchEvent() { entity.getEvents().trigger("crouchAttempt"); }
  private void triggerStopCrouchingEvent() { entity.getEvents().trigger("crouchStop"); }
  private void triggerSprintEvent() { entity.getEvents().trigger("sprintAttempt"); }
  private void triggerStopSprintingEvent() { entity.getEvents().trigger("sprintStop"); }
  private void triggerJumpEvent() { entity.getEvents().trigger("jumpAttempt"); }

  private void triggerRemoveItem() { entity.getEvents().trigger("remove item", focusedItem); }
  private void triggerSelectItem() { entity.getEvents().trigger("select item", focusedItem); }
  private void triggerAddItem() { entity.getEvents().trigger("add item"); }
  private void triggerDropFocused() { entity.getEvents().trigger("drop focused"); }
}


