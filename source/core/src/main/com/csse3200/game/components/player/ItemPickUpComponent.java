package com.csse3200.game.components.player;




import com.csse3200.game.components.Component;
import com.csse3200.game.components.entity.item.ItemComponent;
//import com.csse3200.game.components.player.InventoryComponent;  //delete if not being used
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
//import com.csse3200.game.physics.components.HitboxComponent;    //might be needed later or delete if not used

/**
 * Component that allows an entity to pick up items when in proximity.
 * Listens for collision events with items and adds them to the inventory on request.
 */

public class ItemPickUpComponent extends Component {
    /** Reference to the player's inventory used to store picked up items. */
    private InventoryComponent inventory;
    /** The item entity currently in collision range and eligible to be picked up. */
    private Entity targetItem;
    /** The currently focused inventory slot (set via number key events). */
    private int focusedIndex = -1;
    /** Constructs an ItemPickUpComponent with a reference to the player's inventory. */
    public ItemPickUpComponent(InventoryComponent inventory) {
        this.inventory = inventory;
    }

/**
 * Called when the component is created. Registers listeners for relevant player events:
 *    - collisionStart – detects items in proximity
 *    - collisionEnd – clears the current target item when leaving range
 *    - pick up – attempts to add the current target item to the inventory
 *    - focus item – updates the focused inventory slot
 *    - drop focused – attempts to drop the currently focused item
 */
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

/**
 * Handles the start of a collision. If the colliding entity is an item, it is set as
 * the current target for potential pickup.
 * @param me give small description of what this is
 * @param other same as above
 */
private void onCollisionStart(Fixture me, Fixture other) {
    Object data = other.getBody().getUserData();
    if (!(data instanceof BodyUserData userData)) return;

    Entity otherEntity = userData.entity;
    if (otherEntity.getComponent(ItemComponent.class) != null) {
        targetItem = otherEntity;
        System.out.println("Collision with item: " + targetItem);
    }
}

/**
 * Handles the end of a collision. If the player stops colliding with the currently
 * targeted item, the target is cleared.
 * @param me give small description of what this is
 * @param other same as above
 */
private void onCollisionEnd(Fixture me, Fixture other) {
    Object data = other.getBody().getUserData();
    if (!(data instanceof BodyUserData userData)) return;

    Entity otherEntity = userData.entity;
    if (targetItem == otherEntity) {
        targetItem = null;
        System.out.println("Collision ended with item: " + otherEntity);
    }
}

/**
 * Handles a pickup request (triggered by pressing the pickup key).
 * If there is a valid target item in range, attempts to add it to the inventory.
 */
private void onPickupRequest() {
    System.out.println("Pick up event received. targetItem = " + targetItem);
    if (targetItem != null) {
        pickUpItem(targetItem);
    }
}

/**
 * Attempts to add an item to the player's inventory.
 * Clears the target item reference if successful.
 * @param item that player is currently touching
 */
private void pickUpItem(Entity item) {
        if (item == null) return;

        boolean added = inventory.addItem(item);
        if (added) {
            item.dispose();
            targetItem = null;
            System.out.println("Item picked up and added to inventory!");
        } else {
            System.out.println("Inventory full. Cannot pick up item.");
        }
}

/**
 * Updates which inventory slot is currently focused to properly drop
 * items for the inventory bar.
 * Only valid indices (0–4 inclusive) are accepted;
 * otherwise, the focus is cleared.
 */
private void onFocusItem(int index) {
    if (index >= 0 && index < 5) {
        focusedIndex = index;
    } else {
        focusedIndex = -1;
    }
    System.out.println("Focused slot: " + focusedIndex);
}

/**
 * Handles a drop request (triggered by pressing the drop key).
 * If a valid slot is focused and contains an item, removes the item from
 * the inventory and (eventually) spawns it back into the world.
 */
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
        // need to spawn the item to the world eventually here
        System.out.println("Dropped item from slot " + focusedIndex);
    }
}

}

