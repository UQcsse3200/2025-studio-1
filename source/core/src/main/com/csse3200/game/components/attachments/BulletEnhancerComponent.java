package com.csse3200.game.components.attachments;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.configs.ItemTypes;

public class BulletEnhancerComponent extends Component {

    @Override
    public void create() {
        super.create();
        WeaponsStatsComponent weaponStats = entity.getComponent(WeaponsStatsComponent.class);
        if (weaponStats != null && entity.getComponent(ItemComponent.class).getType() == ItemTypes.RANGED) {
            weaponStats.setBaseAttack(weaponStats.getBaseAttack() * 3);
        }
    }


}
