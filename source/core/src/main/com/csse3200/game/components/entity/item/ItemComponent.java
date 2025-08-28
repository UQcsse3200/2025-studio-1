package com.csse3200.game.components.entity.item;

import com.csse3200.game.components.entity.EntityComponent;

public class ItemComponent extends EntityComponent {
    private int count;
    private String description; //describes the item


    /**
     *
     * @param count is the present count for the item
     * @param description gives the description for the item
     */
    public ItemComponent(int count, String description) {
        super();
        this.count = count;  //initialised at 0 for all items
        this.description = description;
    }

    /**
     * default constructor gets called if no value is passed
     */
    public ItemComponent() {}


    /**
     *
     * @return the count for the item
     */
    public int getCount(){
        return this.count;
    }

    /**
     *
     * @param count sets the item count
     */
    public void setCount(int count){  //to set item_count at a given value
        this.count = count;
    }

    //public void updateCount() -- to increment/decrement count


    /**
     *
     * @return the description for the
     */
    public void setDescription(){

    }

    /**
     *
     * @return
     */
    public String getDescription(){
        return ("Item : " + this.getName()
            + "\nID : " + this.getId()
            + "\nType : " + this.getType()
            + "\nCount : " + this.getCount());;
    }

}



