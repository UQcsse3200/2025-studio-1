package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.physics.BodyUserData;

public class KeycardPickupComponent extends Component {
    private final int level;
    private boolean collected = false;

    public KeycardPickupComponent(int level) {
        this.level = level;
    }

    @Override
    public void create() {
        // Delay to ensure physics setup is complete
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                entity.getEvents().addListener("collisionStart", KeycardPickupComponent.this::onCollisionStart);
            }
        }, 0.2f);
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        if (collected) return;

        Object meUd = me.getBody().getUserData();
        Object otherUd = other.getBody().getUserData();
        if (!(meUd instanceof BodyUserData) || !(otherUd instanceof BodyUserData)) {
            Gdx.app.error("KeycardPickup", "Missing BodyUserData on collision bodies");
            return;
        }

        Entity meEntity = ((BodyUserData) meUd).entity;
        Entity otherEntity = ((BodyUserData) otherUd).entity;

        if (meEntity == null || otherEntity == null) return;
        if (otherEntity == meEntity) return; // self-collision guard
        if (meEntity != entity) return;      // ensure 'me' is the keycard entity

        PlayerActions player = otherEntity.getComponent(PlayerActions.class);
        if (player == null) return;

        InventoryComponent inventory = otherEntity.getComponent(InventoryComponent.class);
        if (inventory == null) {
            Gdx.app.error("KeycardPickup", "Player missing InventoryComponent, entityId=" + otherEntity.getId());
            return;
        }

        collected = true;

        // Set the player's keycard level

        Gdx.app.log("KeycardPickup", "Keycard level " + level + " collected by player");

        // Remove the keycard from the world
        meEntity.dispose();
    }
}