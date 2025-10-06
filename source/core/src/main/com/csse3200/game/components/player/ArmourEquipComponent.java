package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Used specifically for equipping armour since armour and weapons can be equipped together,
 * and multiple pieces of armour can be equipped together.
 */
public class ArmourEquipComponent extends PlayerEquipComponent {
    // hashmap storing armour and corresponding offsets
    Map<Entity, Vector2> currentlyEquippedArmour = new HashMap<>();

    /**
     *
     * @param item   The entity created of the item that needs to be equipped
     * @param offset Where from the players current position to draw the weapon
     */
    @Override
    public void setItem(Entity item, Vector2 offset) {
        if (item == null) {
            System.out.println("Trying to equip null armour");
        }
        currentlyEquippedArmour.put(item, offset);
    }

    /**
     * Updates all currently equipped items' position, so they follow the player.
     */
    @Override
    public void update() {
        for (Entity item: currentlyEquippedArmour.keySet())
            item.setPosition(entity.getPosition().cpy().add(currentlyEquippedArmour.get(item)));
    }
}
