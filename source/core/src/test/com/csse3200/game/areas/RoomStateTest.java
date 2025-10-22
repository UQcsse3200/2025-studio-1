package com.csse3200.game.areas;

import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.events.EventHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.mockito.MockedConstruction;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(GameExtension.class)
class RoomStateTest {

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
    }

    @Test
    void testLockDoors() throws Exception {
        GameArea reception = mock(Reception.class, withSettings()
                .defaultAnswer(RETURNS_DEFAULTS));
        doCallRealMethod().when(reception).registerDoors(any());

        Field doorListField = GameArea.class.getDeclaredField("doorList");
        doorListField.setAccessible(true);
        doorListField.set(reception, new ArrayList<>());

        Entity door = new Entity().addComponent(new DoorComponent(() -> {}));
        reception.registerDoors(new Entity[]{door});
        assert(door.getComponent(DoorComponent.class).isLocked());
    }

    @Test
    void testNoEnemiesClearsRoom() throws Exception {
        GameArea ga = mock(ServerGameArea.class, withSettings()
                .defaultAnswer(RETURNS_DEFAULTS));
        doCallRealMethod().when(ga).registerEnemy(any());
        doCallRealMethod().when(ga).removeEnemy();
        doCallRealMethod().when(ga).getEvents();

        Field enemyCountF = GameArea.class.getDeclaredField("enemyCount");
        enemyCountF.setAccessible(true);
        enemyCountF.set(ga, 0);

        Field eventField = GameArea.class.getDeclaredField("eventHandler");
        eventField.setAccessible(true);
        eventField.set(ga, new EventHandler());

        Field waves = GameArea.class.getDeclaredField("wavesManager");
        waves.setAccessible(true);
        waves.set(ga, null);

        Entity enemy1 = new Entity();
        Entity enemy2 = new Entity();
        Entity enemy3 = new Entity();
        Entity enemy4 = new Entity();
        Entity enemy5 = new Entity();
        ga.registerEnemy(enemy1);
        ga.registerEnemy(enemy2);
        ga.registerEnemy(enemy3);
        ga.registerEnemy(enemy4);
        ga.registerEnemy(enemy5);

        boolean[] triggered = {false};
        ga.getEvents().addListener("room cleared", () -> triggered[0] = true);

        enemy1.getEvents().trigger("death");
        enemy2.getEvents().trigger("death");

        assertFalse(triggered[0]); // Shouldn't be cleared yet...

        enemy3.getEvents().trigger("death");
        enemy4.getEvents().trigger("death");
        enemy5.getEvents().trigger("death");

        assertTrue(triggered[0]);
    }

    @Test
    void testClearAndUnclear() {
        ServerGameArea.clearRoom();
        TunnelGameArea.clearRoom();
        MovingBossRoom.clearRoom();
        ResearchGameArea.clearRoom();

        assert(ServerGameArea.getClearField());
        assert(TunnelGameArea.getClearField());
        assert(MovingBossRoom.getClearField());
        assert(ResearchGameArea.getClearField());

        ServerGameArea.unclearRoom();
        TunnelGameArea.unclearRoom();
        MovingBossRoom.unclearRoom();
        ResearchGameArea.unclearRoom();

        assert(!ServerGameArea.getClearField());
        assert(!TunnelGameArea.getClearField());
        assert(!MovingBossRoom.getClearField());
        assert(!ResearchGameArea.getClearField());
    }

    @Test
    void testEnemyCreationOnUnclear() {
        MockedConstruction<ItemSpawner> mockItemSpawner = 
                mockConstruction(ItemSpawner.class, (mock, context) -> {
                    doNothing().when(mock).spawnItems(any());
        });

        ResearchGameArea.clearRoom();
        ResearchGameArea ga = mock(ResearchGameArea.class, withSettings()
                .defaultAnswer(RETURNS_DEFAULTS));

        doCallRealMethod().when(ga).spawnEnemiesAndWeapons();
        ga.spawnEnemiesAndWeapons();
        verify(ga, never()).startWaves(any());
        ResearchGameArea.unclearRoom();
        ga.spawnEnemiesAndWeapons();
        verify(ga, times(1)).startWaves(any());

        SecurityGameArea.clearRoom();
        SecurityGameArea ga2 = mock(SecurityGameArea.class, withSettings()
                .defaultAnswer(RETURNS_DEFAULTS));

        doCallRealMethod().when(ga2).spawnEnemiesAndWeapons();
        ga2.spawnEnemiesAndWeapons();
        verify(ga2, never()).startWaves(any());
        SecurityGameArea.unclearRoom();
        ga2.spawnEnemiesAndWeapons();
        verify(ga2, times(1)).startWaves(any());

        MainHall.clearRoom();
        MainHall ga3 = mock(MainHall.class, withSettings()
                .defaultAnswer(RETURNS_DEFAULTS));

        doCallRealMethod().when(ga3).spawnEnemiesAndWeapons();
        ga3.spawnEnemiesAndWeapons();
        verify(ga3, never()).startWaves(any());
        MainHall.unclearRoom();
        ga3.spawnEnemiesAndWeapons();
        verify(ga3, times(1)).startWaves(any());

        mockItemSpawner.close();
    }

    @Test
    void testBossRooms() {
        MockedConstruction<ItemSpawner> mockItemSpawner = 
                mockConstruction(ItemSpawner.class, (mock, context) -> {
                    doNothing().when(mock).spawnItems(any());
        });

        MovingBossRoom.clearRoom();
        MovingBossRoom ga = mock(MovingBossRoom.class, withSettings()
                .defaultAnswer(RETURNS_DEFAULTS));

        doCallRealMethod().when(ga).spawnBossAndItems();
        ga.spawnBossAndItems();
        verify(ga, never()).startWaves(any());
        MovingBossRoom.unclearRoom();
        ga.spawnBossAndItems();
        verify(ga, times(1)).spawnBoss();

        mockItemSpawner.close();
    }
}