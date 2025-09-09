package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A component intended to be used by the player to track their inventory.
 *
 * Currently, has functionality for indexing, getting, setting, and removing from
 * the players inventory, also stores the processor amount.
 * Can also be used as a more generic component for other entities.
 */
public class InventoryComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(InventoryComponent.class);

  private int inventoryCount = 0;
  private final int maxCapacity = 5;
  private final int minCapacity = 0;
  private final ArrayList<Entity> items = new ArrayList<Entity>(maxCapacity);
  private final ArrayList<String> itemTexs = new ArrayList<String>(maxCapacity);
  private int processor;
  private Entity currItem;

  /**
   * Constructs an inventory for the player and a beginning currency amount
   * to start with.
   * @param processor The number of processors that the inventory is starting with
   */
  public InventoryComponent(int processor) {
    setProcessor(processor);

    for (int idx = this.minCapacity; idx < this.maxCapacity; idx++) {
      this.items.add(idx, null);
      this.itemTexs.add(idx, null);
    }
  }

  /**
   * Returns a copy of the players current inventory.
   * @return An {@code ArrayList<Entity>} containing the entities in the
   * players inventory.
   * @return An ArrayList containing the players
   */
  public ArrayList<Entity> getInventory() {
    return new ArrayList<Entity>(this.items);
  }

  /**
   * Returns a copy of the current inventory textures
   * @return An {@code ArrayList<String>} containing the texture paths of
   * the players inventory
   */
  public ArrayList<String> getTextures() {
    return new ArrayList<String>(this.itemTexs);
  }

  /**
   * Returns the number of items currently in the inventory
   * @return The number of items in the inventory
   */
  public int getSize() {
    return this.inventoryCount;
  }

  /**
   * Returns the item at the given index.
   * @param index The position of the item in the players inventory (0..4)
   * @return The item at the given position, NULL if nothing there or index not in [0,4]
   */
  public Entity get(int index) {
    if (index >= this.maxCapacity || index < this.minCapacity) {
      return null;
    }
    return this.items.get(index);
  }

  /**
   * Returns the texture for the item at the given index
   * @param index The position of the item in the players inventory (0..4)
   * @return The item at the given position, NULL if nothing there or index not in [0,4]
   */
  public String getTex(int index) {
    if (index >= this.maxCapacity || index < this.minCapacity) {
      return null;
    }
    return this.itemTexs.get(index);
  }

  /**
   * Adds an item to the next free inventory position for the player to hold
   * i.e. addItem(d) [a, b, _, c] -> [a, b, _, c, d]
   * @param item An item to store in the players inventory
   * @return true if successful, false otherwise
   */
  public Boolean addItem(Entity item) {
    for (int idx = 0; idx < maxCapacity; idx++) {
      if (this.get(idx) == null) {
        return this.setItem(idx, item);
      }
    }
    return false;
  }

  /**
   * sets the provided item to the inventory in positions 0 to 4 to be the
   * given item.
   * @param index The index of the inventory 0 to 4
   * @param item An item to store in the players inventory
   * @return true if the item was successfully set, false otherwise or if
   * something is already there
   */
  public Boolean setItem(int index, Entity item) {
    if (this.inventoryCount >= this.maxCapacity)
      return false;

    if (this.get(index) == null) { // if there is something there
      this.items.set(index, item);

      String itemTex = item.getComponent(ItemComponent.class).getTexture();
      this.itemTexs.set(index, itemTex);
      entity.getEvents().trigger("add item", index, itemTex);

      this.inventoryCount++;
    } else // There is something already there
      return false;

    return true;
  }

  /**
   * Removes the item at the given index (must be between 0 and 4) and replaces it
   * with null value.
   * @param index the position of the item to be removed.
   * @return true if successful, false otherwise
   */
  public Boolean remove(int index) {
    if (this.inventoryCount == this.minCapacity ||
            (index >= this.maxCapacity || index < this.minCapacity)) {
      return false;
    }

    // set item to be empty, and then trigger display update
    this.items.set(index, null);
    this.itemTexs.set(index, null);
    entity.getEvents().trigger("remove item", index);
    this.inventoryCount--;
    return true;
  }

  /**
   * Checks if the current inventory is empty or not
   * @return true if the inventory is empty, false otherwise
   */
  public Boolean isEmpty() {
    return this.inventoryCount == this.minCapacity;
  }

  /**
   * Returns true if the players inventory is full, false otherwise
   * @return true if the inventory is full, false otherwise
   */
  public Boolean isFull() {
    return this.inventoryCount == this.maxCapacity;
  }

  /**
   * Returns the player's processor's.
   * @return entity's health
   */
  public int getProcessor() {
    return this.processor;
  }

  /**
   * Returns if the player has a certain amount of processor's.
   * @param processor required amount of processor's
   * @return player has greater than or equal to the required amount of processor's
   */
  public Boolean hasProcessor(int processor) {
    return this.processor >= processor;
  }

  /**
   * Sets the player's processor's. Processor's has a minimum bound of 0.
   * @param processor processor
   */
  public void setProcessor(int processor) {
    int prev = this.processor;
    this.processor = Math.max(processor, 0);
    // Fire event only after entity attached (not during constructor before setEntity)
    if (entity != null && prev != this.processor) {
      entity.getEvents().trigger("updateProcessor", this.processor);
    }
  }

  /**
   * Adds to the player's processors. The amount added can be negative.
   * @param processor processor to add
   */
  public void addProcessor(int processor) {
    setProcessor(this.processor + processor);
  }

  public void equipWeapon(Entity item) {
      if (items.contains(item)) {
          currItem = item;
      }
  }

  public Entity getCurrItem() {
      return currItem;
  }

}

