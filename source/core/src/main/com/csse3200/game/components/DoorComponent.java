package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;

/**
 * A door trigger that only activates if the player has the required keycard level.
 */
public class DoorComponent extends Component {
  private final Runnable onEntered;
  private final int requiredKeycardLevel;
  private boolean triggered = false;

  public DoorComponent(Runnable onEntered, int requiredKeycardLevel) {
    this.onEntered = onEntered;
    this.requiredKeycardLevel = requiredKeycardLevel;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  private void onCollisionStart(Fixture ownFixture, Fixture otherFixture) {
    if (triggered) {
      return;
    }
    short otherLayer = otherFixture.getFilterData().categoryBits;
    if (PhysicsLayer.contains(otherLayer, PhysicsLayer.PLAYER)) {
      // Get the player's inventory
      InventoryComponent inventory = otherFixture.getBody()
              .getUserData() instanceof Entity
              ? ((Entity) otherFixture.getBody().getUserData()).getComponent(InventoryComponent.class)
              : null;

      if (inventory != null && inventory.getKeycardLevel() >= requiredKeycardLevel) {
        triggered = true;
        if (onEntered != null) {
          // Defer to next frame to avoid disposing bodies/entities during a physics callback
          Gdx.app.postRunnable(onEntered);
        }
      } else {
        // Optional: feedback for locked door
        Gdx.app.log("DoorComponent", "Door locked. Requires keycard level " + requiredKeycardLevel);
      }
    }
  }
}