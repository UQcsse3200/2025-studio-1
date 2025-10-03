package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;

/**
 * A component that makes an item follow the player at a given offset.
 */
public class ItemHoldComponent extends Component {
    private final Entity player;
    private final Vector2 offset;


    public ItemHoldComponent(Entity player, Vector2 offset) {
        this.player = player;
        this.offset = offset;
    }

    /**
     * Updates the item's position so it follows the player
     */
    @Override
    public void update() {
        entity.setPosition(player.getPosition().add(offset));
    }
}
