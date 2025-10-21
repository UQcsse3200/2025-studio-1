package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.ArmourComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Used specifically for equipping armour since armour and weapons can be equipped together,
 * and multiple pieces of armour can be equipped together.
 */
public class ArmourEquipComponent extends Component {
    // hashmap storing armour and corresponding offsets
    public Map<Entity, Vector2[]> currentlyEquippedArmour = new HashMap<>();
    private final int MAX_ARMOUR_COUNT = 2;

    /**
     * Equips item and adds it to set of currently equipped armour.
     *
     * @param item The entity created of the item that needs to be equipped.
     */
    public void setItem(Entity item) {
        if (item == null) {
            throw new IllegalArgumentException("Trying to equip null armour");
        }

        if (item.getComponent(ArmourComponent.class) == null) {
            throw new IllegalArgumentException("Item is not an armour entity.");
        }

        if (this.checkDuplicates(item)) {
            System.out.println("Trying to equip same armour type twice - NOT ALLOWED");
            return;
        }

        if (currentlyEquippedArmour.size() >= MAX_ARMOUR_COUNT) {
            System.out.println("Reached max equip slots");
            return;
        }

        item.getComponent(TextureRenderComponent.class).setZIndex(-entity.getPosition().y + 5);
        currentlyEquippedArmour.put(item, new Vector2[]
                {
                        item.getComponent(ArmourComponent.class).rightOffset,
                        item.getComponent(ArmourComponent.class).leftOffset
                });
        entity.getComponent(CombatStatsComponent.class).addProtection(item.getComponent(ArmourComponent.class).protection);
        // initial pos setting
        boolean facingRight = entity.getComponent(PlayerActions.class).isFacingRight();
        Vector2[] armourOffsets = currentlyEquippedArmour.get(item);
        if (facingRight) {
            // add offset + flip armour in right direction
            item.setPosition(entity.getPosition().cpy().add(armourOffsets[0]));
            item.setScale(Math.abs(item.getScale().x), item.getScale().y);
        } else {
            item.setPosition(entity.getPosition().cpy().add(armourOffsets[1]));
            item.setScale(-Math.abs(item.getScale().x), item.getScale().y);
        }
    }

    /**
     * Method used to check for duplicate armour types. Ensures each armour type is equipped only once.
     *
     * @param item Armour being equipped.
     * @return True if armour is already equipped, false otherwise.
     */
    private boolean checkDuplicates(Entity item) {
        for (Entity e : currentlyEquippedArmour.keySet()) {
            if (e.getComponent(ArmourComponent.class).armourType.equals(item.getComponent(ArmourComponent.class).armourType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates all currently equipped items' position, so they follow the player.
     */
    @Override
    public void update() {
        boolean facingRight = entity.getComponent(PlayerActions.class).isFacingRight();
        for (Entity item : currentlyEquippedArmour.keySet()) {
            Vector2[] armourOffsets = currentlyEquippedArmour.get(item);
            if (facingRight) {
                item.setPosition(entity.getPosition().cpy().add(armourOffsets[0]));
            } else {
                item.setPosition(entity.getPosition().cpy().add(armourOffsets[1]));
            }
        }
    }
}
