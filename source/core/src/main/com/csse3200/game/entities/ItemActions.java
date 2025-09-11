package com.csse3200.game.entities;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.ItemComponent;
import com.csse3200.game.entities.configs.ItemTypes;

public class ItemActions extends Component {
    public void create() {
        entity.getEvents().addListener("useItem", this::useItem);
    }

    public boolean useItem(Entity item) {
        ItemTypes itemType = item.getComponent(ItemComponent.class).getType();
        switch (itemType) {
            case CONSUMABLE: return useConsumable(item);
            default: return false;
        }
    }

    public boolean useConsumable(Entity item) {
        return false;
    }
}
