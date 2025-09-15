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
 * Represents a purchasable catalog entry in the in-game shop.
 * <p>
 * A catalog entry stores metadata about a specific {@link Entity} item,
 * including its price, whether it is currently enabled for purchase,
 * the maximum stack size in the player's inventory, and how many units
 * of the item are sold in one bundle.
 * </p>
 */
public record CatalogEntry(
        Entity item,
        int price,
        boolean enabled,
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
     * @param maxStack The max number of this item that can be stored in one inventory
     *                 slot (1 if stackable is false)
     * @param bundleQuantity How many units of the item sold per purchase.
     * @throws IllegalArgumentException if any of the arguments are invalid
     *                                  (e.g., null item, non-positive price, invalid stack/bundle size).
     */
    public CatalogEntry {
        checkValidEntry(item, price, maxStack, bundleQuantity);
    }


    /**
     * Creates an icon actor representing this catalog entry's item.
     * <p>
     * This is typically used for rendering the item in the shop UI.
     * </p>
     *
     * @param skin The game skin to style UI elements (currently unused).
     * @return An {@link Actor} displaying the item's icon.
     */
    public Actor getIconActor(Skin skin) {
        Texture texture = new Texture(item.getComponent(ItemComponent.class).getTexture());
        TextureRegionDrawable icon  = new TextureRegionDrawable(texture);
        return new ImageButton(icon);
    }

    /**
     * Gets the item entity associated with this entry.
     *
     * @return The item entity.
     */
    public Entity getItem() {
        return item;
    }


    /**
     * Gets the display name of the item.
     *
     * @return The item's name as defined in its {@link ItemComponent}.
     */
    public String getItemName() {
        return item.getComponent(ItemComponent.class).getName();
    }

    private void checkValidEntry(Entity item, int price,
                                 int maxStack,
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

        if (maxStack < 1) {
            throw new IllegalArgumentException("maxStack cannot be negative for stackables");
        }
    }




}