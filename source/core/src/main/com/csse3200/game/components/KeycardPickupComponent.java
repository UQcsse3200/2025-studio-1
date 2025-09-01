package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;

public class KeycardPickupComponent extends Component {
    private final int level;
    private boolean collected = false;

    public KeycardPickupComponent(int level) {
        this.level = level;
    }

    @Override
    public void create() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                entity.getEvents().addListener("collisionStart", KeycardPickupComponent.this::onCollisionStart);
            }
        }, 0.2f); // delay by 0.2 seconds
    }
    private void onCollisionStart(Entity me, Entity other) {
        Gdx.app.log("KeycardPickup", "Collision with entity: " + other.getClass().getSimpleName() + ", ID: " + other.getId());
        Gdx.app.log("KeycardPickup", "Collected: " + collected);
        if (collected) return;

        if (other == entity) return; // ✅ Prevent self-collision

        if (other.getComponent(PlayerActions.class) == null) return;

        InventoryComponent inventory = other.getComponent(InventoryComponent.class);
        if (inventory != null) {
            collected = true;
            inventory.addItem(entity);
            ServiceLocator.getGlobalEvents().trigger("keycard_lvl" + level + "_collected");
            Gdx.app.log("KeycardPickup", "Keycard level " + level + " collected by player");
            entity.dispose();
        }
    }
}