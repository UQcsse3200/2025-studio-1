package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;

public abstract class ItemActionsComponent extends Component {
    @Override
    public void create() {
        entity.getEvents().addListener("use", this::use);
    }

    public abstract void use(Entity player);
}
