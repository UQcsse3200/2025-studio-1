package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.Effect;

import java.util.ArrayList;

public abstract class ConsumableConfig {

    public ArrayList<Effect> effects;
    public int duration = 0;
    public boolean isProjectile = false;
    public String texturePath;
    public int maxStack = 1;  // 1 for no stack

    public ConsumableConfig() {
    }
}
