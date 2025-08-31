package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;

public class ItemHoldComponent extends Component {
    private Entity player;

    public ItemHoldComponent(Entity player) {
        this.player = player;
    }

    @Override
    public void update() {
        entity.setPosition(player.getPosition().add(new Vector2(0.65f, -0.1f)));
    }
}
