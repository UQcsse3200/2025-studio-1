package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.ColliderComponent;

public class KeycardGateComponent extends Component {
    private final int requiredLevel;
    private final Runnable onUnlock;
    private boolean unlocked = false;

    public KeycardGateComponent(int requiredLevel, Runnable onUnlock) {
        this.requiredLevel = requiredLevel;
        this.onUnlock = onUnlock;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        if (unlocked) return;

        Object otherUd = other.getBody().getUserData();
        if (!(otherUd instanceof BodyUserData)) return;

        Entity otherEntity = ((BodyUserData) otherUd).entity;
        if (otherEntity == null) return;

        InventoryComponent inv = otherEntity.getComponent(InventoryComponent.class);
        if (inv != null) {
            inv.setKeycardLevel(requiredLevel);
            unlocked = true;
            Gdx.app.log("KeycardPickup", "Keycard level " + requiredLevel + " collected by player");

            // Defer removal to avoid Box2D world locked crash
            Gdx.app.postRunnable(() -> entity.dispose());
        } else {
            Gdx.app.log("KeycardPickup", "Player missing InventoryComponent, entityId=" + otherEntity.getId());
        }
    }


    public void unlock() {
        unlocked = true;
        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        if (collider != null) {
            collider.setSensor(true);
            Gdx.app.log("KeycardGate", "Gate collider set to sensor");
        } else {
            Gdx.app.error("KeycardGate", "Missing ColliderComponent on gate entity");
        }
    }

    public boolean isUnlocked() {
        return unlocked;
    }
}
