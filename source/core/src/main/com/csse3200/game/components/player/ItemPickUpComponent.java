package com.csse3200.game.components.player;



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;




/* this component allows player to pickup items or weapons from the game and
 * this is done by checking for collisions with items
 * then it checks if inventory has space it moves it to inventory
 */

// Create function to determine if the player is close enough to the item to pick it up

// Create a function to actually pick up

// Create a function to check if inventory is full, call a function from inventoryComponent

public class ItemPickUpComponent extends Component {
    private InventoryComponent inventory;
    private Entity targetItem;

    public ItemPickUpComponent(InventoryComponent inventory) {
        this.inventory = inventory;
    }

    @Override
    public void create() {
        HitboxComponent hitbox = entity.getComponent(HitboxComponent.class);
        if (hitbox != null) {
            hitbox.setLayer(PhysicsLayer.PLAYER); //this makes sure hitbox lets only Player interact with items

        }
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
    }

      //detects when the player touches an item entity
    private void onCollisionStart(Entity other) {
        if (other.getComponent(ItemComponent.class) != null) {
            targetItem = other;
        }
    }

    private void onCollisionEnd(Entity other) {
        if (targetItem == other) {
            targetItem = null;
        }
    }


        //checks if a nearby item is present and checks if player presses the E key then pickup nearbyItem
        public void update () {
            //if (targetItem != null && Gdx.input.isKeyPressed(Input.Keys.E)) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                System.out.println("E is pressed");
                //pickUpItem(targetItem);
            }
        }

        //to add item to inventory and remove it from the map/world
        private void pickUpItem (Entity item){
            if (item == null) {
                return;
            }
            boolean itemadded = inventory.addItem(item);   //this is checking if inventory is full

            if (itemadded) {
                item.dispose();
                targetItem = null;
                System.out.println("Picked Up :" + item);
            } else {
                System.out.println("Cannot pick " + item + "Inventory is full");
            }
        }


    }









