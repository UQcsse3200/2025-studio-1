package com.csse3200.game.components.player;



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;




/** this component allows player to pickup items from world and put it into
 * their inventory.
 */

//To Do: Create function to determine if the player is close enough to the item to pick it up

//To Do: Create a function to actually pick up

//To Do: Create a function to check if inventory is full, call a function from inventoryComponent

public class ItemPickUpComponent extends Component {
    private InventoryComponent inventory;
    private Entity targetItem;

    public ItemPickUpComponent(InventoryComponent inventory) {
        this.inventory = inventory;
    }

    /**
     *  creates hitbox on player checking for item collision
     */
    @Override
    public void create() {
        HitboxComponent hitbox = entity.getComponent(HitboxComponent.class);
        if (hitbox != null) {
            hitbox.setLayer(PhysicsLayer.PLAYER); //this makes sure hitbox lets only Player interact with items
        }
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        //entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);

        //entity.getEvents().addListener("pick up", this::onPickupRequest);
    }

    /**
     * Sets item on a collision or clears it once collision stops
     * @param item Entity
     */
    private void onCollisionStart(Entity item) {
        if (item.getComponent(ItemComponent.class) != null) {
            targetItem = item;
        } else if (targetItem == item) {
            targetItem = null;
        }
    }

    /**
     * Checks if Player has pressed 'E' and an item is in proximity then picks
     * item up
     */
    public void update() {
        if (targetItem != null && Gdx.input.isKeyPressed(Input.Keys.E)) {
        //if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            System.out.println("E is pressed");
            pickUpItem(targetItem);
        }
    }

        //to add item to inventory and remove it from the map/world
        private void pickUpItem (Entity item){
            if (item == null) {
                return;
            }
            boolean itemAdded = inventory.addItem(item);   //this is checking if inventory is full

        boolean added = inventory.addItem(targetItem);
        if (added) {
            //targetItem.dispose(); // remove from world
            targetItem = null;
        }  // Inventory full – optionally trigger a UI toast/hint event here
        // entity.getEvents().trigger("uiToast", "Inventory full");


    }
}










