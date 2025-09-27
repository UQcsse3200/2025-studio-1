package com.csse3200.game.components.items;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.DaggerConfig;
import com.csse3200.game.entities.configs.weapons.LightsaberConfig;
import com.csse3200.game.entities.configs.weapons.PistolConfig;
import com.csse3200.game.entities.configs.weapons.RifleConfig;

import static com.csse3200.game.entities.configs.Weapons.*;
import static com.csse3200.game.entities.configs.projectiles.ProjectileConfig.itemType;

public class ItemComponent extends Component {
    private String name;
    private ItemTypes type;
    private int count = 1;
    private String texture;
//    private ItemTypes weapons;

    public ItemComponent() {
        this.type = ItemTypes.NONE;
    }

    /**
     * Returns the name of this item.
     *
     * @return the item's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this item.
     *
     * @param name the new name for the item
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of this item.
     *
     * @return the item's {@link ItemTypes} type
     */
    public ItemTypes getType() {
        return type;
    }

    /**
     * Sets the type of this item.
     *
     * @param type the {@link ItemTypes} to assign to this item
     */
    public void setType(ItemTypes type) {
        this.type = type;
    }

    /**
     * Gets the current number of existing items that are the same within
     * the game
     *
     * @return the count for the item
     */
    public int getCount() {
        return this.count;
    }

    /**
     * setCount to set item_count at a given value
     *
     * @param count sets the item count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Sets the texture path of the current item
     *
     * @param texture sets the texture path of the current item
     */
    public void setTexture(String texture) {
        this.texture = texture;
    }

    /**
     * Returns the texture path of the current item
     */
    public String getTexture() {
        return this.texture;
    }

    /**
     * Gets the description for the item
     *
     * @return A formatted description of the item
     * with its; name, id, type, and current count.
     */
    public String getDescription() {
        return ("Item : " + this.getName()
                + "\nId : " + entity.getId()
                + "\nType : " + this.getType()
                + "\nCount : " + this.getCount());
    }

    public Vector2 getEquipOffset() {
        if ("rifle" == type.getTypeName()) return new Vector2(0.8f, 0.15f);
        if ("lightsaber" == type.getTypeName()) return new Vector2(0.7f, -0.2f);
        if ("dagger" == type.getTypeName()) return new Vector2(1.0f, 0.3f);
        if ("pistol" == type.getTypeName()) return new Vector2(0.75f, -0.1f);
        return new Vector2(0.7f, 0.3f);

//        switch (weapons) {
//            case LIGHTSABER:
//                return new Vector2(0.7f, -0.2f);  //works while facing left
//            case RIFLE:
//                return new Vector2(0.8f, 0.15f);
//            case DAGGER:
//                return new Vector2(1.0f, 0.3f);
//            case PISTOL:
//                return new Vector2(0.75f, -0.1f);
//            default:
//                return new Vector2(0.7f, 0.3f);
//        }
    }

}


