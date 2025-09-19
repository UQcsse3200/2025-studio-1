package com.csse3200.game.components.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.entities.factories.items.WorldPickUpFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Component that allows an entity to pick up items when in proximity.
 * Listens for collision events with items and adds them to the inventory on request.
 */

public class ItemPickUpComponent extends Component {
    // Reference to the player's inventory used to store picked up items.
    private final InventoryComponent inventory;
    // The item entity currently in collision range and eligible to be picked up.
    private Entity targetItem;
    // The currently focused inventory slot (set via number key events).
    private int focusedIndex = -1;

    // Constructs an ItemPickUpComponent with a reference to the player's inventory.
    public ItemPickUpComponent(InventoryComponent inventory) {
        this.inventory = inventory;
    }

    // Constructs a private Logger for this class.
    private static final Logger logger = LoggerFactory.getLogger(ItemPickUpComponent.class);

    /**
     * Called when the component is created. Registers listeners for relevant player events:
     * - collisionStart – detects items in proximity
     * - collisionEnd – clears the current target item when leaving range
     * - pick up – attempts to add the current target item to the inventory
     * - focus item – updates the focused inventory slot
     * - drop focused – attempts to drop the currently focused item
     */
    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
        entity.getEvents().addListener("pick up", this::onPickupRequest);
        entity.getEvents().addListener("focus item", this::onFocusItem);
        entity.getEvents().addListener("drop focused", this::onDropFocused);
        entity.getEvents().addListener("pickupAll", this::onPickupAll);
        logger.debug("ItemPickUpComponent listeners registered");
    }

    /**
     * Handles the start of a collision. If the colliding entity is an item, it is set as
     * the current target for potential pickup.
     *
     * @param me    Fixture to retrieve entity data
     * @param other Fixture to retrieve entity data
     */
    private void onCollisionStart(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) {
            logger.trace("collisionStart: no BodyUserData on other fixture");
            return;
        }

        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(ItemComponent.class) != null) {
            targetItem = otherEntity;
            logger.trace("collisionStart: collided with item {}", targetItem);
        }
    }

    /**
     * Handles the end of a collision. If the player stops colliding with the currently
     * targeted item, the target is cleared.
     *
     * @param me    Fixture to retrieve entity data
     * @param other Fixture to retrieve entity data
     */
    private void onCollisionEnd(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) {
            logger.trace("collisionEnd: no BodyUserData on other fixture");
            return;
        }

        Entity otherEntity = userData.entity;
        if (targetItem == otherEntity) {
            targetItem = null;
            logger.debug("collisionEnd: cleared target for {}", otherEntity);
        }
    }

    /**
     * Handles a pickup request (triggered by pressing the pickup key).
     * If there is a valid target item in range, attempts to add it to the inventory.
     */
    private void onPickupRequest() {
        logger.debug("pick up event: current target={}", targetItem);
        if (targetItem != null) {
            pickUpItem(targetItem);
        }
    }

    /**
     * Attempts to add an item to the player's inventory.
     * Clears the target item reference if successful.
     *
     * @param item that player is currently touching
     */
    private void pickUpItem(Entity item) {
        if (item == null) {
            logger.warn("pickUpItem called with null item");
            return;
        }
        ItemComponent ic = item.getComponent(ItemComponent.class);
        String tex = (ic != null) ? ic.getTexture() : null;
        if (tex == null) {
            logger.warn("Pickup has no ItemComponent/texture");
            return;
        }

        Entity weapon = weaponFromTexture(tex);
        if (weapon == null) {
            logger.warn("Unknown pickup texture {}, ignoring", ic.getTexture());
            return;
        }

        boolean added = inventory.addItem(weapon);
        if (added) {
            item.dispose();
            targetItem = null;
            logger.info("Picked up item and added to inventory");
        } else {
            weapon.dispose();
            logger.info("Inventory full. Cannot pick up item");
        }
    }

    private Entity weaponFromTexture(String texture) {
        for (Weapons w : Weapons.values()) {
            if (texture.equals(w.getConfig().texturePath)) {
                return WeaponsFactory.createWeapon(w);
            }
        }
        return null;
    }

    /**
     * Updates which inventory slot is currently focused to properly drop
     * items for the inventory bar.
     * Only valid indices (0–4 inclusive) are accepted;
     * otherwise, the focus is cleared.
     *
     * @param index the index of the focused slot in the inventory bar
     */
    private void onFocusItem(int index) {
        if (index >= 0 && index < 5) {
            focusedIndex = index;
            logger.debug("Focus set to slot {}", focusedIndex);
        } else {
            focusedIndex = -1;
        }
    }

    /**
     * Handles the drop action for the currently focused inventory slot.
     * When the player presses the drop key ('R'), this method:
     * - Checks whether a valid slot is focused (0–4).
     * - If the slot contains an item, removes it from the inventory.
     * - Clears the focused index so another accidental drop does not occur.
     * - Attempts to respawn the dropped item back into the game world near the player,
     * using the texture to reconstruct the item entity.
     *
     */
    private void onDropFocused() {
        // do nothing if no valid slot is currently focused
        if (focusedIndex < 0 || focusedIndex >= 5) {
            return;
        }
        // Get the entity stored in the currently focused inventory slot
        Entity item = inventory.get(focusedIndex);
        if (item == null) {
            logger.debug("Drop ignored: focused slot {} empty", focusedIndex);
            return;
        }
        // Extract the texture path from the item
        ItemComponent ic = item.getComponent(ItemComponent.class);
        String tex = (ic != null) ? ic.getTexture() : null;
        // Remove the item from the inventory
        boolean removed = inventory.remove(focusedIndex);
        if (!removed) {
            logger.warn("Drop failed: could not remove item at index {}", focusedIndex);
            return;
        }
        focusedIndex = -1;
        // If no texture info was stored, skip respawning to the world
        if (tex == null) {
            logger.debug("Drop: no texture info on item; skipping world respawn");
            return;
        }
        // Attempt to recreate a new item entity from the stored texture
        //Entity newItem = createItemFromTexture(tex);
        Entity newItem = WorldPickUpFactory.createPickupFromTexture(tex);
        if (newItem == null) {
            return;
        }
        // Make dropped items static so they behave like map-placed items
        PhysicsComponent phys = newItem.getComponent(PhysicsComponent.class);
        if (phys != null) phys.setBodyType(BodyDef.BodyType.StaticBody);

        // Copy the player's current center position
        Vector2 playerPos = entity.getCenterPosition().cpy();
        // Define a vertical offset so the item spawns slightly below the player
        float dropOffsetY = -1.2f;
        float shiftX = MathUtils.random(-0.65f, 0.65f);
        // Calculate the final drop position by applying the offset
        Vector2 dropPos = new Vector2(playerPos.x + shiftX, playerPos.y + dropOffsetY);
        newItem.setPosition(dropPos);

        // Get the current active GameArea
        GameArea area = ServiceLocator.getGameArea();
        if (area != null) {
            area.spawnEntity(newItem);
            logger.debug("Respawned dropped item at world pos {}", dropPos);
        } else {
            logger.error("Drop: no active GameArea; cannot spawn item");
        }
    }

    /**
     * The method recreates an item entity based on its texture path.
     * This method is used when dropping items from the inventory back into the world.
     * Since inventory only stores the texture reference the texture string is used here
     * as a lookup key to recreate the appropriate entity using the relevant factory class.
     *
     * @param texture the texture file path associated with the item in inventory
     * @return a new {@link Entity} matching the texture, or {@code null} if the
     * texture does not correspond to a known item type.
     */
    public Entity createItemFromTexture(String texture) {
        logger.trace("createItemFromTexture({})", texture);
        if (texture.endsWith("dagger.png")) return WeaponsFactory.createWeapon(Weapons.DAGGER);
        if (texture.endsWith("pistol.png")) return WeaponsFactory.createWeapon(Weapons.PISTOL);
        if (texture.endsWith("rifle.png")) return WeaponsFactory.createWeapon(Weapons.RIFLE);
        if (texture.endsWith("lightsaberSingle.png")) return WeaponsFactory.createWeapon(Weapons.LIGHTSABER);
        if (texture.endsWith("tree.png")) return ObstacleFactory.createTree();
        return null;
    }

    /**
     * Picks up all available items in the world and adds them to the player's inventory.
     * <p>
     * This is a cheat feature triggered by the terminal command "pickupAll".
     * Unlike normal pickups (which require collision), this method scans all entities
     * in the game world and attempts to add every entity with an itemComponent
     * into the inventory until it is full.
     */
    private void onPickupAll() {
        if (inventory == null) {
            logger.warn("pickupAll: inventory not found on player");
            return;
        }
        //pickup all entities at once
        var entities = ServiceLocator.getEntityService().getEntities();
        int picked = 0;

        for (Entity candidate : entities) {
            if (candidate == entity) continue;
            //only pickup ItemComponet
            if (candidate.getComponent(ItemComponent.class) == null) continue;

            // If the bag is full, then return false
            boolean added = inventory.addItem(candidate);
            if (added) {
                candidate.dispose();
                picked++;
            } else {
                logger.info("pickupAll: inventory full after picking {} item(s)", picked);
                break;
            }
        }

        logger.info("pickupAll: finished, picked {} item(s)", picked);
    }
}
