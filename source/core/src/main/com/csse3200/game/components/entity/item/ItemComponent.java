package com.csse3200.game.components.entity.item;

import com.csse3200.game.components.entity.EntityComponent;

public class ItemComponent extends EntityComponent {
    private int count;
    private String texture;

    /**
     * This is the constructor for the ItemComponent class, it
     * will take in a count of current existing items of the same
     * type and store it
     * @param count is the present count for the item
     * @param texture is the texture path to the texture of the item
     */
    public ItemComponent(int count, String texture) {
        super();
        this.count = count;
        this.texture = texture;
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
<<<<<<< HEAD
     * Outputs the description of the provided item with id
     * @param name The name of the item that you want the description of
     * @param id The id of the item that you want the description of
     */
    public void description(String name, int id){
        System.out.println("This is a/an " + name + ". " +
            "\nPresent count : " + count +
            "\nType :  " + getType());
    }

    //public void updateCount() -- to increment/decrement count

    /**
     * Sets the texture path of the current item
=======
     *
>>>>>>> 660727b6d09bc1e1ff075b62432dd4aae92dad77
     * @param texture sets the texture path of the current item
     */
    public void setTexture(String texture){this.texture = texture;}

    /**
     * Returns the texture path of the current item
     */
    public String getTexture() { return this.texture; }

    /**
     * TODO: implement functionality and add docstrings
     */
    public void setDescription(){ }

    /**
     * Gets the description for the item
     * @return A formatted description of the item
     * with its; name, id, type, and current count.
     */
    public String getDescription(){
        return ("Item : " + this.getName()
                + "\nID : " + this.getId()
                + "\nType : " + this.getType()
                + "\nCount : " + this.getCount());
    }
}


