package com.csse3200.game.entities.configs.benches;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public abstract class BenchConfig {
    public ItemTypes benchType = ItemTypes.BENCH;
    public String texturePath = "";
    public String promptText = "";

    public abstract void upgrade(boolean playerNear, Entity player, Label buyPrompt);

}
