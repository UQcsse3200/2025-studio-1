package com.csse3200.game.components.tasks;


import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(GameExtension.class)
class MagazineComponentTest {

    Entity player;
    Entity weapon;
    CombatStatsComponent combat;
    MagazineComponent magazine;

    @BeforeEach
    void setup() {

        player = new Entity();
        weapon = new Entity();

        combat = new CombatStatsComponent(100, 20);
        magazine = new MagazineComponent(12);

        player.addComponent(combat);
        weapon.addComponent(magazine);
        player.setCurrItem(weapon);
    }

    @Test
    void fullByDefault() {

        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

    @Test
    void fullMagazineReloadFails() {

        Assert.assertEquals(false, magazine.reload(player));
        Assert.assertEquals(1000, combat.getAmmo());
    }

    @Test
    void emptyReserveReloadFails() {

        combat.setAmmo(0);
        magazine.setCurrentAmmo(4);
        Assert.assertEquals(false, magazine.reload(player));
        Assert.assertEquals(0, combat.getAmmo());
    }

    @Test
    void PartialReload() {

        combat.setAmmo(5);
        magazine.setCurrentAmmo(7);
        Assert.assertEquals(true, magazine.reload(player));
        Assert.assertEquals(0, combat.getAmmo());
        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

    @Test
    void FullReload() {


        magazine.setCurrentAmmo(9);
        Assert.assertEquals(true, magazine.reload(player));
        Assert.assertEquals(997, combat.getAmmo());
        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

}
