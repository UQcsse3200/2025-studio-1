package com.csse3200.game.components.items;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.configs.ItemTypes;

public class ItemComponent extends Component {
    private String name;
    private ItemTypes type;
    private int count = 1;
    private String texture;

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
     * Gets the current number of existing items that are the same within
     * the game
     * @return the count for the item
     */
    public int getCount() {return this.count;}

    /**
     * setCount to set item_count at a given value
     * @param count sets the item count
     */
    public void setCount(int count){this.count = count;}

    /**
     * Sets the texture path of the current item
     * @param texture sets the texture path of the current item
     */
    public void setTexture(String texture) {this.texture = texture;}

    /**
     * Returns the texture path of the current item
     */
    public String getTexture() { return this.texture; }

    /**
     * Gets the description for the item
     * @return A formatted description of the item
     * with its; name, id, type, and current count.
     */
    public String getDescription(){
        return ("Item : " + this.getName()
                + "\nId : " + entity.getId()
                + "\nType : " + this.getType()
                + "\nCount : " + this.getCount());
    }
}


