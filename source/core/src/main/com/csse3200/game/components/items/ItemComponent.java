package com.csse3200.game.components.items;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.configs.ItemTypes;

public class ItemComponent extends Component {
    private String name;
    private ItemTypes type;
    private int count = 1;
    private String texture;
    private boolean pickupable = true;

    public ItemComponent() {
        this.type = ItemTypes.NONE;
    }

    /**
     * Returns the name of this item.
     *
     * @return the item's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this item.
     *
     * @param name the new name for the item
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of this item.
     *
     * @return the item's {@link ItemTypes} type
     */
    public ItemTypes getType() {
        return type;
    }

    /**
     * Sets the type of this item.
     *
     * @param type the {@link ItemTypes} to assign to this item
     */
    public void setType(ItemTypes type) {
        this.type = type;
    }

    /**
     * This checks if the item can be picked up
     *
     * @return True if pickupable is true, false otherwise
     */
    public boolean isPickupable() {
        return pickupable;
    }

    /**
     * This changes whether the Item can be picked up by the player
     *
     * @param status True if the item can be picked up, False otherwise
     */
    public void setPickupable(boolean status) {
        this.pickupable = status;
    }

    /**
     * Gets the current number of existing items that are the same within
     * the game
     *
     * @return the count for the item
     */
    public int getCount() {
        return this.count;
    }

    /**
     * setCount to set item_count at a given value
     *
     * @param count sets the item count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Returns the texture path of the current item
     */
    public String getTexture() {
        return this.texture;
    }

    /**
     * Sets the texture path of the current item
     *
     * @param texture sets the texture path of the current item
     */
    public void setTexture(String texture) {
        this.texture = texture;
    }

    /**
     * Returns the offset of where the item should be displayed from the player
     * for it to look normal
     *
     * @return A new Vector2 containing the offset as x and y displacement.
     */
    public Vector2 getEquipOffset() {
        if ("rifle".equals(this.getName())) return new Vector2(0.8f, 0.15f);
        if ("lightsaber".equals(this.getName())) return new Vector2(0.7f, -0.2f); // works while facing left
        if ("dagger".equals(this.getName())) return new Vector2(1.0f, 0.3f);
        if ("pistol".equals(this.getName())) return new Vector2(0.75f, -0.1f);
        if ("rocketlauncher".equals(this.getName())) return new Vector2(0.75f, -0.1f);
        return new Vector2(0.7f, 0.3f);
    }
}


