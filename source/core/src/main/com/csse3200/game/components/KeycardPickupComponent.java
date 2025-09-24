package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.PhysicsComponent;

public class KeycardPickupComponent extends Component {

    private final int level;
    private boolean collected = false;

    private final String KEYCARD_PICKUP = "KeycardPickup";

    public KeycardPickupComponent(int level) {
        /** Initializes the component with a specific keycard level to grant on pickup. */
        this.level = level;
    }

    @Override
    public void create() {
        Gdx.app.log(KEYCARD_PICKUP, "Listener registered for collisionStart");
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);

        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            BodyUserData userData = new BodyUserData();
            userData.entity = entity;
            physics.getBody().setUserData(userData);

        }
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        Gdx.app.log(KEYCARD_PICKUP, "Collision started with fixture: " + other);

        if (collected) return;
        Object otherUd = other.getBody().getUserData();
        if (!(otherUd instanceof BodyUserData)) return;

        Entity otherEntity = ((BodyUserData) otherUd).entity;
        if (otherEntity == null) return;


        Gdx.app.log(KEYCARD_PICKUP, "Collided with entity: " + otherEntity);
        Gdx.app.log(KEYCARD_PICKUP, "InventoryComponent found? " + (otherEntity.getComponent(InventoryComponent.class) != null));

        InventoryComponent inventory = otherEntity.getComponent(InventoryComponent.class);
        if (inventory != null) {
            inventory.setKeycardLevel(level);
            Gdx.app.log(KEYCARD_PICKUP, "Keycard level " + level + " collected by player");
            collected = true;

            Gdx.app.postRunnable(() -> entity.dispose());
        }
    }
}
