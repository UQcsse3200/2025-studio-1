package com.csse3200.game.components.items;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.effects.Effect;
import com.csse3200.game.entities.Entity;

/**
 * Component responsible for handling the usage of consumable items.
 * <p>
 * When used, this component applies the effects of the consumable
 * to the player entity and updates the inventory accordingly
 * (either removing the item if it has been fully consumed, or
 * decrementing its stack count).
 */
public class ConsumableUseComponent extends ItemActionsComponent {

    /**
     * Uses the consumable item on the given player.
     * <p>
     * If the consumable does not fire a projectile, all of its
     * effects are applied to the player. The item is then consumed
     * (removed or decremented) from the player's inventory.
     *
     * @param player the entity representing the player using the consumable
     */
    public void use(Entity player) {
        ConsumableComponent consumable = entity.getComponent(ConsumableComponent.class);
        if (!consumable.firesProjectile()) {
            for (Effect effect : consumable.getEffects()) {
                effect.apply(player);
            }
        }
        consumeConsumable(player);
    }

    /**
     * Consumes the current item from the player's inventory.
     * <p>
     * - If the consumable has only one left, it is removed entirely.<br>
     * - Otherwise, its stack count is decremented and the inventory is
     * notified via the {@code "update item count"} event.
     *
     * @param player the entity representing the player whose inventory will be updated
     */
    private void consumeConsumable(Entity player) {
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        boolean toBeConsumed = entity.getComponent(ItemComponent.class).getCount() <= 1;

        // Locate the index of the current item in the inventory
        int itemIdx = -1;
        for (int i = 0; i < 5; i++) {
            if (inventory.get(i) == null) {
                continue;
            }
            if (inventory.get(i).getId() == entity.getId()) {
                itemIdx = i;
                break;
            }
        }

        // Item not found in inventory
        if (itemIdx == -1) {
            return;
        }

        if (toBeConsumed) {
            // Remove the item entirely if count reaches zero
            inventory.remove(itemIdx);
        } else {
            // Decrement the stack count and trigger inventory update event
            ItemComponent itemComponent = entity.getComponent(ItemComponent.class);
            itemComponent.setCount(itemComponent.getCount() - 1);

            inventory.getEntity().getEvents()
                    .trigger("update item count", itemIdx, itemComponent.getCount());
        }
    }
}
