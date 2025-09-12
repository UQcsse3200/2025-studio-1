package com.csse3200.game.components.shop;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;

/**
 * Models one purchasable catalog item that can be found in the shop.
 */
public record CatalogEntry(
        Entity item,
        int price,
        boolean enabled,
        boolean stackable,
        int maxStack,
        int bundleQuantity
) {

    /**
     * Constructs a CatalogEntry, storing the details around an item that can
     * be purchased from the shop
     *
     * @param item The unique identifiable itemKey
     * @param price The price (per unit) of the item
     * @param enabled Whether this item can currently be purchased by the player.
     *                (E.g., disabled due to max stack, not enough processors, etc.)
     * @param stackable Whether the use may have multiple of this item in an
     *                  inventory slot.
     * @param maxStack The max number of this item that can be stored in one inventory
     *                 slot (1 if stackable is false)
     * @param bundleQuantity How many units of the item sold per purchase.
     */
    public CatalogEntry {
        checkValidEntry(item, price, stackable, maxStack, bundleQuantity);
    }

    /**
     * Calculates the cost for the quantity of this CatalogEntry.
     *
     * @param quantity the number the player wants to purchase
     * @return the total cost
     */
    public int costFor(int quantity) {
        int qty = Math.max(1, quantity);
        return (price * qty);
    }

    private void checkValidEntry(Entity item, int price,
                                 boolean stackable, int maxStack,
                                 int bundleQuantity) {
        if (item == null) {
            throw new IllegalArgumentException("itemKey cannot be null or blank");
        }

        if (price <= 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }

        if (bundleQuantity <= 0) {
            throw new IllegalArgumentException("bundleQuantity cannot be negative");
        }

        if (!stackable && maxStack < 1) {
            throw new IllegalArgumentException("maxStack cannot be negative for stackables");
        }
    }

    /**
     * Get icon for entry
     *
     * @param skin game skin to use
     * @return actor of icon
     **/
    public Actor getIconActor(Skin skin) {
        Texture texture = new Texture(item.getComponent(ItemComponent.class).getTexture());
        TextureRegionDrawable icon  = new TextureRegionDrawable(texture);
        return new ImageButton(icon);
    }

    public Entity getItem() {
        return item;
    }

    public String getItemName() {
        return item.getComponent(ItemComponent.class).getName();
    }


}