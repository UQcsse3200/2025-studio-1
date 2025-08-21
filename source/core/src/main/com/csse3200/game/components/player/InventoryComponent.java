package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A component intended to be used by the player to track their inventory.
 *
 * Currently only stores the gold amount but can be extended for more advanced functionality such as storing items.
 * Can also be used as a more generic component for other entities.
 */
public class InventoryComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(InventoryComponent.class);
  private int inventoryCount = 0;
  private ArrayList<Item> items = new ArrayList<Item>();
  private int gold;

  public InventoryComponent(int gold) {
    setGold(gold);
  }

  /**
   * Returns a copy of the players current inventory.
   *
   * @return An ArrayList<Item> containing the players
   */
  public ArrayList<Item> getInventory() {
    return new ArrayList<Item>(this.items);
  }

  /**
   * Returns the item at the given index
   *
   * @param index The position of the item in the players inventory (1..5)
   * @return The item at the given position
   */
  public Item getItem(int index) {
    return this.items.get(index);
  }

  /**
   * Adds an item to the inventory
   *
   * @return true if successful, false otherwise
   */
  public Boolean addItem(Item: item) {
    if (this.inventoryCount > 5) {
      return false;
    } else {
      this.items.add(item);
      this.inventoryCount++;
      return true;
    }
  }

  /**
   * Removes the item at the given index
   *
   * @return true if successful, false otherwise
   */
  public Boolean removeItem(int index) {
    if (this.inventoryCount == 0) {
      return false;
    } else {
      this.items.remove(index);
      this.inventoryCount--;
      return true;
    }
  }

  /**
   * Checks if the current inventory is empty or not
   *
   * @return true if the inventory is empty, false otherwise
   */
  public Boolean isInventoryEmpty() {
    return this.items.isEmpty();
  }

  /**
   * Returns true if the players inventory is full, false otherwise
   *
   * @return true if the inventory is full, false otherwise
   */
  public Boolean isFull() {
    return this.inventoryCount == 5;
  }

  /**
   * Returns the player's gold.
   *
   * @return entity's health
   */
  public int getGold() {
    return this.gold;
  }

  /**
   * Returns if the player has a certain amount of gold.
   * @param gold required amount of gold
   * @return player has greater than or equal to the required amount of gold
   */
  public Boolean hasGold(int gold) {
    return this.gold >= gold;
  }

  /**
   * Sets the player's gold. Gold has a minimum bound of 0.
   *
   * @param gold gold
   */
  public void setGold(int gold) {
    this.gold = Math.max(gold, 0);
    logger.debug("Setting gold to {}", this.gold);
  }

  /**
   * Adds to the player's gold. The amount added can be negative.
   * @param gold gold to add
   */
  public void addGold(int gold) {
    setGold(this.gold + gold);
  }
}
