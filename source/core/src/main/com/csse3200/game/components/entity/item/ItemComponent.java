package com.csse3200.game.components.entity.item;

import com.csse3200.game.components.entity.EntityComponent;

public class ItemComponent extends EntityComponent {

    //to keep track of the number of the specific item
    private int count;
    private String description; //describes the item


    public ItemComponent(int count, String description) {
        super();
        this.count = 0;  //initialised at 0 for all items
        this.description = description;
    }

    public int getCount(){
        return this.count;
    }

    public void setCount(int count){  //to set item_count at a given value
        this.count = count;
    }

    //public void updateCount() -- to increment/decrement count

    public String setDescription(){
        return ("Item : " + this.getName()
            + "\nID : " + this.getId()
            + "\nType : " + this.getType()
            + "\nCount : " + this.getCount());
    }

    public String getDescription(){
        return this.description;
    }

}



