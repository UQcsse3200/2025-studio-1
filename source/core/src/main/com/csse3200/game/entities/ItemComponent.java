package com.csse3200.game.entities;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.configs.ItemTypes;

public class ItemComponent extends Component {
    private String name;
    private ItemTypes type = ItemTypes.NONE;
    private int count = 1;
    private String texture;

    /**
     * This is the constructor for the ItemComponent class, it
     * will take in a count of current existing items of the same
     * type and store it
     * @param count is the present count for the item
     * @param texture is the texture path to the texture of the item
     */
    public ItemComponent(String name, ItemTypes type, int count, String texture) {
        this.name = name;
        this.type = type;
        this.count = count;
        this.texture = texture;
    }

    /**
     * default constructor gets called if no value is passed
     */
    public ItemComponent() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemTypes getType() {
        return type;
    }

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
     * TODO: implement functionality and add docstrings
     */
//    public void setDescription(){ }

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


