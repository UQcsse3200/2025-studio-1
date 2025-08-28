package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.physics.PhysicsLayer;

/**
 * A simple sensor trigger that calls a callback when the player touches it.
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
    if (triggered) {
      return;
    }
    short otherLayer = otherFixture.getFilterData().categoryBits;
    if (PhysicsLayer.contains(otherLayer, PhysicsLayer.PLAYER)) {
      triggered = true;
      if (onEntered != null) {
        // Defer to next frame to avoid disposing bodies/entities during a physics callback
        Gdx.app.postRunnable(onEntered);
      }
    }
  }
}


