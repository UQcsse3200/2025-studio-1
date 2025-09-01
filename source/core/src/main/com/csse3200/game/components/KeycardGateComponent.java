package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.physics.BodyUserData;

public class KeycardGateComponent extends Component {
    private final int requiredLevel;
    private boolean unlocked = false;

    public KeycardGateComponent(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public void create() {
        // Listen for physics collisions (fixture-based)
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        // Listen for global unlock event from the keycard pickup
        ServiceLocator.getGlobalEvents()
                .addListener("keycard_lvl" + requiredLevel + "_collected", this::unlock);
    }

    // Fixture-based handler: convert fixtures to Entities safely
    private void onCollisionStart(Fixture me, Fixture other) {
        // Ensure 'me' is actually this gate's fixture
        Object meUd = me.getBody().getUserData();
        Object otherUd = other.getBody().getUserData();
        if (!(meUd instanceof BodyUserData) || !(otherUd instanceof BodyUserData)) {
            Gdx.app.error("KeycardGate", "Missing BodyUserData on collision bodies");
            return;
        }

        Entity meEntity = ((BodyUserData) meUd).entity;
        Entity otherEntity = ((BodyUserData) otherUd).entity;
        if (meEntity == null || otherEntity == null) return;

        // Only react if the collision involves this gate entity
        if (meEntity != this.entity) return;

        if (!unlocked) {
            Gdx.app.log("KeycardGate", "Gate locked. Requires keycard level " + requiredLevel);
            // Optional: Show UI hint here
            return;
        }

        // Gate is unlocked: collider is a sensor, so player should pass through.
        Gdx.app.log("KeycardGate", "Gate unlocked. Allowing passage.");
        // Optional: trigger transition or animation here
    }

    public void unlock() {
        Gdx.app.log("KeycardGate", "Unlock triggered for level " + requiredLevel);

        // Mark unlocked first so future collisions read correct state
        unlocked = true;

        if (entity == null) {
            Gdx.app.error("KeycardGate", "Entity is null");
            return;
        }

        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            Gdx.app.error("KeycardGate", "Missing PhysicsComponent on gate entity");
            return;
        }

        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        if (collider != null) {
            // Make gate non-blocking
            collider.setSensor(true);
            Gdx.app.log("KeycardGate", "Gate collider set to sensor");
        } else {
            Gdx.app.error("KeycardGate", "Missing ColliderComponent on gate entity");
        }

        // Optional: visual/audio effects for unlocking
    }
}