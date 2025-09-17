package com.csse3200.game.components.attachments;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.Assert.assertEquals;

@ExtendWith(GameExtension.class)
public class BulletEnhancerComponentTest {

    @Test
    void bulletEnhancerTriplesDamage() {
        Entity gun = new Entity();
        WeaponConfig config = Weapons.RIFLE.getConfig();
        gun.addComponent(new WeaponsStatsComponent(config));
        gun.addComponent(new BulletEnhancerComponent());
        ItemComponent itemComp = new ItemComponent();
        itemComp.setType(ItemTypes.RANGED);
        int prevDamage = gun.getComponent(WeaponsStatsComponent.class).getBaseAttack();
        gun.addComponent(itemComp);
        gun.getComponent(BulletEnhancerComponent.class).create();
        int newDamage = gun.getComponent(WeaponsStatsComponent.class).getBaseAttack();
        assertEquals(prevDamage * 3, newDamage);
    }

    @Test
    void bulletEnhancerDoesntWorkOnMelee() {
        Entity melee = new Entity();
        WeaponConfig meleeConfig = Weapons.LIGHTSABER.getConfig();
        melee.addComponent(new WeaponsStatsComponent(meleeConfig));
        melee.addComponent(new BulletEnhancerComponent());
        ItemComponent meleeItemComp = new ItemComponent();
        meleeItemComp.setType(ItemTypes.MELEE);
        int prevMeleeDamage = melee.getComponent(WeaponsStatsComponent.class).getBaseAttack();
        melee.addComponent(meleeItemComp);
        melee.getComponent(BulletEnhancerComponent.class).create();
        int newDamage = melee.getComponent(WeaponsStatsComponent.class).getBaseAttack();
        assertEquals(prevMeleeDamage, newDamage);
    }
}
