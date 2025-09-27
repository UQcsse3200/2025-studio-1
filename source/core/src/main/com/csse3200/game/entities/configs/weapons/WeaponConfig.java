package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public abstract class WeaponConfig {
    public ItemTypes weaponType = ItemTypes.NONE;
    public int damage = 0;
    public String texturePath = "";
    private String name = "";

    public void setName(String name) {this.name = name;}

    public String getName() {return name;}
}
