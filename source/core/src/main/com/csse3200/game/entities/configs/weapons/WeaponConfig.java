package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public abstract class WeaponConfig {
    public ItemTypes weaponType = ItemTypes.NONE;
    public int damage = 0;
    public String texturePath = "";
    private String name = "";

    /**
     * sets the name of the weapon
     * @param name to be assigned to this weapon configuration
     */
    public void setName(String name) {this.name = name;}

    /**
     * returns the name of the weapon
     * @return the name of this weapon configuration
     */
    public String getName() {return name;}
}
