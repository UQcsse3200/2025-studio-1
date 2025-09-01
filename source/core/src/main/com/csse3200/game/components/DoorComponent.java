package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;

/**
 * A simple door trigger that runs a callback when the player collides with it.
 * Keycard requirements are handled separately by KeycardGateComponent.
 */
public class DoorComponent extends Component {
  private final Runnable onEntered;
  private boolean triggered = false;

  public DoorComponent(Runnable onEntered) {
    this.onEntered = onEntered;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  private void onCollisionStart(Fixture ownFixture, Fixture otherFixture) {
    if (triggered) return;

    short otherLayer = otherFixture.getFilterData().categoryBits;
    if (PhysicsLayer.contains(otherLayer, PhysicsLayer.PLAYER)) {
      // Get the colliding entity
      Object userData = otherFixture.getBody().getUserData();
      Entity otherEntity = null;

      if (userData instanceof BodyUserData) {
        otherEntity = ((BodyUserData) userData).entity;
      } else if (userData instanceof Entity) {
        otherEntity = (Entity) userData;
      }

      if (otherEntity != null) {
        triggered = true;
        Gdx.app.log("DoorComponent", "Door triggered by player");
        if (onEntered != null) {
          // Defer to avoid Box2D world locked crash
          Gdx.app.postRunnable(onEntered);
        }
      }
    }
  }
}
