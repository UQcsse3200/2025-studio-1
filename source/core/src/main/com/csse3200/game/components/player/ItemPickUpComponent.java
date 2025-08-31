package com.csse3200.game.components.player;




import com.csse3200.game.components.Component;
import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.HitboxComponent;

/**
 * Component that allows an entity to pick up items when in proximity.
 * Listens for collision events with items and adds them to the inventory on request.
 */

public class ItemPickUpComponent extends Component {
    private InventoryComponent inventory;
    private Entity targetItem;

    private int focusedIndex = -1;
    public ItemPickUpComponent(InventoryComponent inventory) {
        this.inventory = inventory;
    }


@Override
public void create() {
//    HitboxComponent hitbox = entity.getComponent(HitboxComponent.class);       //might need this later
//    if (hitbox != null) {
//        hitbox.setSensor(true);    //this is commented out for now as hitbox is already set as a sensor
//    }
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
    entity.getEvents().addListener("pick up", this::onPickupRequest);

    entity.getEvents().addListener("focus item", this::onFocusItem);
    entity.getEvents().addListener("drop focused", this::onDropFocused);
}

private void onCollisionStart(Fixture me, Fixture other) {
    Object data = other.getBody().getUserData();
    if (!(data instanceof BodyUserData userData)) return;

    Entity otherEntity = userData.entity;
    if (otherEntity.getComponent(ItemComponent.class) != null) {
        targetItem = otherEntity;
        System.out.println("Collision with item: " + targetItem);
    }
}

private void onCollisionEnd(Fixture me, Fixture other) {
    Object data = other.getBody().getUserData();
    if (!(data instanceof BodyUserData userData)) return;

    Entity otherEntity = userData.entity;
    if (targetItem == otherEntity) {
        targetItem = null;
        System.out.println("Collision ended with item: " + otherEntity);
    }
}

private void onPickupRequest() {
    System.out.println("Pick up event received. targetItem = " + targetItem);
    if (targetItem != null) {
        pickUpItem(targetItem);
    }
}
    private void pickUpItem(Entity item) {
            if (item == null) return;

            boolean added = inventory.addItem(item);
            if (added) {
                targetItem = null;
                System.out.println("Item picked up and added to inventory!");
            } else {
                System.out.println("Inventory full. Cannot pick up item.");
            }
    }

    private void onFocusItem(int index) {
        if (index >= 0 && index < 5) {
            focusedIndex = index;
        } else {
            focusedIndex = -1;
        }
        System.out.println("Focused slot: " + focusedIndex);
    }

    private void onDropFocused() {
        if (focusedIndex < 0 || focusedIndex >= 5) {
            return;
        }
        Entity item = inventory.get(focusedIndex);
        if (item == null) {
            System.out.println("Focused slot empty, nothing to drop.");
            return;
        }

        boolean removed = inventory.remove(focusedIndex);
        if (removed) {
            // need to spawn the item to the worl
            System.out.println("Dropped item from slot " + focusedIndex);
        }
    }

}

