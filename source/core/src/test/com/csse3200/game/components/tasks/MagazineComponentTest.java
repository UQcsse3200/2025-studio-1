package com.csse3200.game.components.tasks;


import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class MagazineComponentTest {

    Entity player;
    Entity weapon;
    CombatStatsComponent combat;
    MagazineComponent magazine;

    @Mock
    PhysicsEngine engine;
    @Mock
    GameTime timeSource;


    @BeforeEach
    void setup() {

        player = new Entity();
        weapon = new Entity();

        combat = new CombatStatsComponent(100, 20);
        magazine = new MagazineComponent(12);

        player.addComponent(combat);
        weapon.addComponent(magazine);
        player.setCurrItem(weapon);

        PhysicsService service = new PhysicsService(engine);
        ServiceLocator.registerPhysicsService(service);
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

    @Test
    void updateProperlyChangesReloadingStatus() {

        when(timeSource.getDeltaTime()).thenReturn(1f);
        magazine.seTimeSinceLastReload(0f);
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
