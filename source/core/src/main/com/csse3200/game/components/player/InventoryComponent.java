package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A component intended to be used by the player to track their inventory.
 *
 * Stores gold (processor), items, and the player's keycard level.
 */
public class InventoryComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(InventoryComponent.class);

    private int inventoryCount = 0;
    private final int maxCapacity = 5;
    private final int minCapacity = 0;
    private final ArrayList<Entity> items = new ArrayList<>(maxCapacity);

    private int processor;
    private int keycardLevel = 0;

    public InventoryComponent(int processor) {
        setProcessor(processor);
        for (int idx = this.minCapacity; idx < this.maxCapacity; idx++) {
            this.items.add(idx, null);
        }
    }

    /** Returns a copy of the player's current inventory. */
    public ArrayList<Entity> getInventory() {
        return new ArrayList<>(this.items);
    }

    /** Returns the number of items currently in the inventory. */
    public int getSize() {
        return this.inventoryCount;
    }

    /** Returns the item at the given index, or null if out of bounds or empty. */
    public Entity get(int index) {
        if (index >= this.maxCapacity || index < this.minCapacity) {
            return null;
        }
        return this.items.get(index);
    }

    /** Adds an item to the next available inventory slot. */
    public Boolean addItem(Entity item) {
        return this.setItem(this.inventoryCount, item);
    }

    /** Sets an item at a specific index if empty. */
    public Boolean setItem(int index, Entity item) {
        if (this.inventoryCount >= this.maxCapacity) {
            return false;
        }
        if (this.get(index) == null) {
            this.items.set(index, item);
            this.inventoryCount++;
        } else {
            return false;
        }
        return true;
    }

    /** Removes the item at the given index. */
    public Boolean remove(int index) {
        if (this.inventoryCount == this.minCapacity ||
                (index >= this.maxCapacity || index < this.minCapacity)) {
            return false;
        }
        this.items.set(index, null);
        this.inventoryCount--;
        return true;
    }

    /** Checks if the inventory is empty. */
    public Boolean isEmpty() {
        return this.inventoryCount == this.minCapacity;
    }

    /** Checks if the inventory is full. */
    public Boolean isFull() {
        return this.inventoryCount == this.maxCapacity;
    }

    /** Returns the player's gold/processor count. */
    public int getProcessor() {
        return this.processor;
    }

    /** Checks if the player has at least the given amount of gold/processors. */
    public Boolean hasProcessor(int processor) {
        return this.processor >= processor;
    }

    /** Sets the player's gold/processor count (min 0). */
    public void setProcessor(int processor) {
        this.processor = Math.max(processor, 0);
        logger.debug("Setting gold to {}", this.processor);
    }

    /** Adds to the player's gold/processor count (can be negative). */
    public void addGold(int processor) {
        setProcessor(this.processor + processor);
    }

    public int getKeycardLevel() {
        return keycardLevel;
    }

    public void setKeycardLevel(int level) {
        this.keycardLevel = level;
    }
}
