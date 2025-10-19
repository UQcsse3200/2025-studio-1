package com.csse3200.game.components.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Armour;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.PowerupsFactory;
import com.csse3200.game.entities.factories.items.ArmourFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.entities.factories.items.WorldPickUpFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Component that allows an entity to pick up items when in proximity.
 * Listens for collision events with items and adds them to the inventory on request.
 */

public class ItemPickUpComponent extends Component {
    // Constructs a private Logger for this class.
    private static final Logger logger = LoggerFactory.getLogger(ItemPickUpComponent.class);
    // Reference to the player's inventory used to store picked up items.
    private final InventoryComponent inventory;
    // The currently focused inventory slot (set via number key events).
    private int focusedIndex = -1;

    // Constructs an ItemPickUpComponent with a reference to the player's inventory.
    public ItemPickUpComponent(InventoryComponent inventory) {
        this.inventory = inventory;
    }

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
        entity.getEvents().addListener("player:interact", this::onPickupRequest);
        entity.getEvents().addListener("focus item", this::onFocusItem);
        entity.getEvents().addListener("drop focused", this::onDropFocused);
        entity.getEvents().addListener("pickupAll", this::onPickupAll);
        logger.debug("ItemPickUpComponent listeners registered");
    }

    /**
     * Handles a pickup request (triggered by pressing the pickup key).
     * If there is a valid target item in range, attempts to add it to the inventory.
     */
    private void onPickupRequest(Entity targetItem) {
        logger.debug("pick up event: current target={}", targetItem);
        if (targetItem != null) {
            pickUpItem(targetItem);
        }
    }

    /**
     * Attempts to convert the specified world item into an inventory item
     * and add it to the player's inventory.
     * The method checks for an {@link ItemComponent} and its texture path,
     * maps the texture to a known weapon type weaponFromTexture(String),
     * and creates a new weapon entity for storage in the inventory.
     * - If the item is successfully added, the original world entity is disposed
     * and the active targetItem reference is cleared.
     * - If the inventory is full, the newly created weapon entity is disposed,
     * and the world item remains untouched.
     * - If the texture is missing or unknown, the pickup attempt is ignored.
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
        Entity armour = null;
        if (weapon == null) {
            armour = armourFromTexture(tex);
            if (armour == null) {
                logger.warn("Unknown pickup texture {}, ignoring", ic.getTexture());
                return;
            }
        }

        if (weapon != null) {
            weapon.create();
            // The two following lines of code were generate by ChatGPT
            MagazineComponent mag = weapon.getComponent(MagazineComponent.class);
            if (mag != null)  {
                mag.setTimeSinceLastReload(999f);
                //copies across magazine data from dropped item
                if (item.hasComponent(MagazineComponent.class)) {
                    mag.setCurrentAmmo(item.getComponent(MagazineComponent.class).getCurrentAmmo());
                }

            }


            boolean added = inventory.addItem(weapon);
            if (added) {
                item.dispose();
                logger.info("Picked up item and added to inventory");
            } else {
                weapon.dispose();
                logger.info("Inventory full. Cannot pick up item");
            }
            return;
        }

        // if we are here, that means item is armour
        // armour is not going to part of the inventory, gets immediately equipped
        armour.create();
        ArmourEquipComponent armourEquipper = entity.getComponent(ArmourEquipComponent.class);
        item.dispose();
        armourEquipper.setItem(armour);
    }

    /**
     * Resolves a texture path string into a corresponding weapon entity.
     * This method compares the given texture path against the Weapons
     * enum configuration and, if matched, creates the appropriate weapon
     * using the {@link WeaponsFactory}.
     *
     * @param texture The texture file path associated with the world item.
     * @return A new {@link Entity} representing the weapon, or null if the
     * texture does not correspond to any known weapon type.
     */
    private Entity weaponFromTexture(String texture) {
        for (Weapons w : Weapons.values()) {
            if (texture.equals(w.getConfig().texturePath)) {
                return WeaponsFactory.createWeapon(w);
            }
        }
        return null;
    }

    /**
     * Resolves a texture path string into a corresponding armour entity.
     * This method compares the given texture path against the Armour
     * enum configuration and, if matched, creates the appropriate armour
     * using the {@link ArmourFactory}.
     *
     * @param texture The texture file path associated with the world item.
     * @return A new {@link Entity} representing the weapon, or null if the
     * texture does not correspond to any known weapon type.
     */
    private Entity armourFromTexture(String texture) {
        for (Armour a : Armour.values()) {
            if (texture.equals(a.getConfig().texturePath)) {
                return ArmourFactory.createArmour(a);
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
        //Gets old magazine data
        MagazineComponent mag = null;
        if (item.hasComponent(MagazineComponent.class)) {
            mag = new MagazineComponent(item.
                    getComponent(MagazineComponent.class).getMaxAmmo());
            mag.setCurrentAmmo(item.
                    getComponent(MagazineComponent.class).getCurrentAmmo());
        }

        // remove the item from being equipped
        entity.getComponent(PlayerEquipComponent.class).setItem(null, null);

        focusedIndex = -1;
        // If no texture info was stored, skip respawning to the world
        if (tex == null) {
            logger.debug("Drop: no texture info on item; skipping world respawn");
            return;
        }
        // Attempt to recreate a new world-pickable item entity from the stored texture
        Entity newItem = WorldPickUpFactory.createPickupFromTexture(tex);
        if (newItem == null) {
            return;
        }
        //adds mag to the item, if it has it
        if (mag != null) {
            newItem.addComponent(mag);
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
        if (texture.endsWith("rocketlauncher.png")) return WeaponsFactory.createWeapon(Weapons.LAUNCHER);
        if (texture.endsWith("lightsaberSingle.png")) return WeaponsFactory.createWeapon(Weapons.LIGHTSABER);
        if (texture.endsWith("rapidfirepowerup.png")) return PowerupsFactory.createRapidFire();
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
