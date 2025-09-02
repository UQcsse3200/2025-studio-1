package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * A gate that requires a specific keycard level to pass through.
 * The gate will remain locked until the player collides with it AND
 * has the required keycard level in their InventoryComponent.
 */
public class KeycardGateComponent extends Component {
    // Initializes the gate with a required keycard level and an optional unlock callback.
    private final int requiredLevel;
    private final Runnable onUnlock;
    private boolean unlocked = false;

    public KeycardGateComponent(int requiredLevel, Runnable onUnlock) {
        this.requiredLevel = requiredLevel;
        this.onUnlock = onUnlock;
    }

    @Override
    public void create() {
        // Registers a listener for collision events when the component is created.
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        // Handles collision logic: checks if the player has the required keycard level and unlocks the gate if true.
        Object meUd = me.getBody().getUserData();
        Object otherUd = other.getBody().getUserData();

        if (!(meUd instanceof BodyUserData) || !(otherUd instanceof BodyUserData)) {
            return;
        }

        Entity meEntity = ((BodyUserData) meUd).entity;
        Entity otherEntity = ((BodyUserData) otherUd).entity;
        if (meEntity == null || otherEntity == null) return;
        if (meEntity != this.entity) return;

        InventoryComponent inventory = otherEntity.getComponent(InventoryComponent.class);
        if (inventory != null) {
            Gdx.app.log("KeycardGate", "Gate sees keycard level: " + inventory.getKeycardLevel());

            if (inventory.getKeycardLevel() >= requiredLevel) {
                if (!unlocked) {
                    unlock();
                    Gdx.app.log("KeycardGate", "Gate unlocked. Allowing passage.");
                    if (onUnlock != null) {
                        Gdx.app.postRunnable(onUnlock);
                    }
                }
            } else {
                Gdx.app.log("KeycardGate", "Gate locked. Requires keycard level " + requiredLevel);
            }
        }
    }

    public void unlock() {
        // Sets the gate to unlocked and makes its collider non-blocking (sensor mode).
        unlocked = true;
        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        if (collider != null) {
            collider.setSensor(true);
        }
    }

    public boolean isUnlocked() {
        // Returns whether the gate has already been unlocked.
        return unlocked;
    }
}
