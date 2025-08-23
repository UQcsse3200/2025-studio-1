package com.csse3200.game.components.player;


import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.components.player.InventoryComponent;


// Create function to determine if the player is close enough to the item to pick it up

// Create a function to actually pick up

// Create a function to check if inventory is full, call a function from inventoryComponent

public class ItemPickUpComponent extends Component {
    private InventoryComponent inventory;
    private Entity nearbyItem;

    public ItemPickUpComponent(InventoryComponent inventory) {

    }

    public void create() {

    }

    //detects when the player touches an item entity
    private void onCollisionStart() {

    }

    private void onCollisionEnd() {

    }

    //checks if a nearby item is present and checks if player presses the E key then pickup nearbyItem
    public void update() {

    }

    //to add item to inventory and remove it from the map/world
    private void pickUpItem(Entity item) {

    }

}



