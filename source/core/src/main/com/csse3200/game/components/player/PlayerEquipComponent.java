package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * A component that makes an item follow the player at a given offset.
 */
public class PlayerEquipComponent extends Component {
    private Entity item;
    private Vector2 offset;

    public PlayerEquipComponent() {
        this.item = null;
        this.offset = new Vector2();
    }

    /**
     * Updates the item's position so it follows the player
     */
    @Override
    public void update() {
        if (item != null)
            item.setPosition(entity.getPosition().cpy().add(offset));
    }

    /**
     * This function replaces the currently equipped item with a new one
     *
     * @param item   The entity created of the item that needs to be equipped
     * @param offset Where from the players current position to draw the weapon
     */
    public void setItem(Entity item, Vector2 offset) {
        if (this.item != null)
            this.item.dispose();

        this.item = item;
        this.offset = offset;
    }
}