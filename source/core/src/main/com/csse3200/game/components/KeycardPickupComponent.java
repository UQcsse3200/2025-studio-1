package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.physics.BodyUserData;

public class KeycardPickupComponent extends Component {
    private final int level;
    private boolean collected = false;

    public KeycardPickupComponent(int level) {
        this.level = level;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        if (collected) return;

        Object otherUd = other.getBody().getUserData();
        if (!(otherUd instanceof BodyUserData)) return;

        Entity otherEntity = ((BodyUserData) otherUd).entity;
        if (otherEntity == null) return;

        InventoryComponent inventory = otherEntity.getComponent(InventoryComponent.class);
        if (inventory != null) {
            inventory.setKeycardLevel(level);
            Gdx.app.log("KeycardPickup", "Keycard level " + level + " collected by player");
            collected = true;

            Gdx.app.postRunnable(() -> entity.dispose());
        }
    }
}
