package com.csse3200.game.components.entity.item;

import com.csse3200.game.components.entity.EntityComponent;

public class ItemComponent extends EntityComponent {
    private int count;

    /**
     * This is the constructor for the ItemComponent class, it
     * will take in a count of current existing items of the same
     * type and store it
     * @param count is the present count for the item
     *
     */
    public ItemComponent(int count) {
        super();
        this.count = count;
    }

    /**
     * default constructor gets called if no value is passed
     */
    public ItemComponent() {}

    /**
     * Gets the current number of existing items that are the same within
     * the game
     * @return the count for the item
     */
    public int getCount(){return this.count;}

    /**
     * setCount to set item_count at a given value
     * @param count sets the item count
     */
    public void setCount(int count){this.count = count;}

    /**
     * Outputs the description of the provided item with id
     * @param name The name of the item that you want the description of
     * @param id The id of the item that you want the description of
     */
    public void description(String name, int id){
        System.out.println("This is a/an " + name + ". " +
            "\nPresnt count : " + count +
            "\nType :  " + getType());
    }
}



