package com.csse3200.game.components.entity.item;

import com.csse3200.game.components.entity.EntityComponent;

public class ItemComponent extends EntityComponent {
    private int count;

    /**
     *
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
     *@return the count for the item
     */
    public int getCount(){return this.count;}

    /**
     * setCount to set item_count at a given value
     * @param count sets the item count
     */
    public void setCount(int count){this.count = count;}

    public void description(String name, int id){
        System.out.println("This is a/an " + name + ". " +
            "\nPresnt count : " + count +
            "\nType :  " + getType());
    }
}



