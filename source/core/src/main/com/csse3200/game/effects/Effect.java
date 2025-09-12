package com.csse3200.game.effects;

import com.csse3200.game.entities.Entity;

public abstract class Effect {
    public Effect() {}

    public abstract boolean apply(Entity entity);
}
