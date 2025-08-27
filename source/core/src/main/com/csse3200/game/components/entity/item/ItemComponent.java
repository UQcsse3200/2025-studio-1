package com.csse3200.game.components.entity.item;

import com.csse3200.game.components.entity.EntityComponent;

public class ItemComponent  {

    //to keep track of the number of the specific item
    private int count;


    public ItemComponent(){
        super();
        this.count = 0;  //initialised at 0 for all items
    }

    public int getCount(int count){
        return this.count;
    }

    public void setCount(int count){  //to set item_count at a given value
        this.count = count;
    }

    //public void updateCount() -- to increment/decrement count

}



