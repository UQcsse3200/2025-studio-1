package com.csse3200.game.components.tasks;


import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(GameExtension.class)
class MagazineComponentTest {

    Entity entity;
    CombatStatsComponent combat;
    MagazineComponent magazine;

    @BeforeEach
    void setup() {
        entity = new Entity();
        combat = new CombatStatsComponent(100, 20);
        magazine = new MagazineComponent(12);
        entity.addComponent(combat);
        entity.addComponent(magazine);
    }

    @Test
    void fullByDefault() {

        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

    @Test
    void fullMagazineReloadFails() {

        Assert.assertEquals(false, magazine.reload());
        Assert.assertEquals(1000, combat.getAmmo());
    }

    @Test
    void emptyReserveReloadFails() {

        combat.setAmmo(0);
        magazine.setCurrentAmmo(4);
        Assert.assertEquals(false, magazine.reload());
        Assert.assertEquals(0, combat.getAmmo());
    }

    @Test
    void PartialReload() {

        combat.setAmmo(5);
        magazine.setCurrentAmmo(7);
        Assert.assertEquals(true, magazine.reload());
        Assert.assertEquals(0, combat.getAmmo());
        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

    @Test
    void FullReload() {


        magazine.setCurrentAmmo(9);
        Assert.assertEquals(true, magazine.reload());
        Assert.assertEquals(997, combat.getAmmo());
        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

}
