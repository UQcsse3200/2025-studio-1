package com.csse3200.game.components.tasks;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.PistolConfig;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class MagazineComponentTest {

    Entity player;
    Entity weapon;
    WeaponsStatsComponent weaponStats;
    InventoryComponent inventory;
    MagazineComponent magazine;
    AmmoStatsComponent ammo;

    @Mock
    PhysicsEngine engine;
    @Mock
    GameTime timeSource;
    @Mock
    ResourceService resourceService;


    @BeforeEach
    void setup() {

        player = new Entity();

        magazine = new MagazineComponent(12);
        inventory = new InventoryComponent();
        ammo = new AmmoStatsComponent(1000);

        player.addComponent(inventory);
        player.addComponent(ammo);

        when(timeSource.getDeltaTime()).thenReturn(1f);
        ServiceLocator.registerTimeSource(timeSource);

    }

    @Test
    void fullByDefault() {

        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

    @Test
    void fullMagazineReloadFails() {

        Assert.assertEquals(false, magazine.reload(player));
        Assert.assertEquals(1000, ammo.getAmmo());
    }

    @Test
    void emptyReserveReloadFails() {

        ammo.setAmmo(0);
        magazine.setCurrentAmmo(4);
        Assert.assertEquals(false, magazine.reload(player));
        Assert.assertEquals(0, ammo.getAmmo());
    }

    @Test
    void PartialReload() {

        ammo.setAmmo(5);
        magazine.setCurrentAmmo(7);
        Assert.assertEquals(true, magazine.reload(player));
        Assert.assertEquals(0, ammo.getAmmo());
        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

    @Test
    void FullReload() {


        magazine.setCurrentAmmo(9);
        Assert.assertEquals(true, magazine.reload(player));
        Assert.assertEquals(997, ammo.getAmmo());
        Assert.assertEquals(12, magazine.getCurrentAmmo());
    }

    @Test
    void updateProperlyChangesReloadingStatus() {

        when(timeSource.getDeltaTime()).thenReturn(1f);
        magazine.setTimeSinceLastReload(0f);
        Assert.assertTrue(magazine.reloading());

        magazine.update();
        magazine.update();

        Assert.assertFalse(magazine.reloading());
    }

    @AfterEach
    void afterEach() {
        ServiceLocator.clear();
        reset(engine, timeSource);
    }

}
