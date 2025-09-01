package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * A gate that requires a specific keycard level to pass through.
 * The gate will remain locked until the player collides with it AND
 * has the required keycard level in their InventoryComponent.
 */
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
    }

    /**
     * Called when something collides with this gate.
     * Only unlocks if the colliding entity is the player and has the required keycard level.
     */
    private void onCollisionStart(Fixture me, Fixture other) {
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

        // Check if the other entity has an InventoryComponent
        InventoryComponent inventory = otherEntity.getComponent(InventoryComponent.class);
        if (inventory != null && inventory.getKeycardLevel() >= requiredLevel) {
            unlock();
            Gdx.app.log("KeycardGate", "Gate unlocked. Allowing passage.");
        } else {
            Gdx.app.log("KeycardGate", "Gate locked. Requires keycard level " + requiredLevel);
            // Optional: trigger UI message or sound here
        }
    }

    /**
     * Unlocks the gate by making its collider a sensor (non-blocking).
     */
    public void unlock() {
        unlocked = true;

        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        if (collider != null) {
            collider.setSensor(true); // disables physical blocking
            Gdx.app.log("KeycardGate", "Gate collider set to sensor");
        } else {
            Gdx.app.error("KeycardGate", "Missing ColliderComponent on gate entity");
        }
    }

    public boolean isUnlocked() {
        return unlocked;
    }
}