package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaCoreMethodsTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
    }

    @Test
    void testGetRoomNumber() {
        // Test different room types
        GameArea reception = new Reception(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(reception);
        assertEquals(2, reception.getRoomNumber());
        
        GameArea mainHall = new MainHall(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(mainHall);
        assertEquals(3, mainHall.getRoomNumber());
        
        GameArea security = new SecurityGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(security);
        assertEquals(4, security.getRoomNumber());
        
        GameArea office = new OfficeGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(office);
        assertEquals(5, office.getRoomNumber());
        
        GameArea elevator = new ElevatorGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(elevator);
        assertEquals(6, elevator.getRoomNumber());
        
        GameArea research = new ResearchGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(research);
        assertEquals(7, research.getRoomNumber());
        
        GameArea shipping = new ShippingGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(shipping);
        assertEquals(8, shipping.getRoomNumber());
        
        GameArea storage = new StorageGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(storage);
        assertEquals(9, storage.getRoomNumber());
        
        GameArea server = new ServerGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(server);
        assertEquals(10, server.getRoomNumber());
        
        GameArea tunnel = new TunnelGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(tunnel);
        assertEquals(11, tunnel.getRoomNumber());
        
        // Test default case - use a concrete implementation
        GameArea forest = new ForestGameArea(terrainFactory, cameraComponent);
        ServiceLocator.registerGameArea(forest);
        assertEquals(1, forest.getRoomNumber());
    }


    @Test
    void testGetRoomName() {
        GameArea forest = new ForestGameArea(terrainFactory, cameraComponent);
        assertEquals("Forest", forest.getRoomName());
        
        GameArea reception = new Reception(terrainFactory, cameraComponent);
        assertEquals("Reception", reception.getRoomName());
        
        GameArea mainHall = new MainHall(terrainFactory, cameraComponent);
        assertEquals("Mainhall", mainHall.getRoomName());
    }

    @Test
    void testSpawnEntity() {
        GameArea gameArea = mock(GameArea.class);
        gameArea.areaEntities = new java.util.ArrayList<>(); // manually initialize

        doCallRealMethod().when(gameArea).spawnEntity(any(Entity.class));
        doCallRealMethod().when(gameArea).dispose();

        Entity mockEntity = mock(Entity.class);

        gameArea.spawnEntity(mockEntity);
        verify(mockEntity).create();

        gameArea.dispose();
        verify(mockEntity).dispose();
    }

    @Test
    void testDispose() {
        GameArea gameArea = mock(GameArea.class);
        gameArea.areaEntities = new java.util.ArrayList<>(); // manually initialize

        doCallRealMethod().when(gameArea).spawnEntity(any(Entity.class));
        doCallRealMethod().when(gameArea).dispose();

        Entity mockEntity1 = mock(Entity.class);
        Entity mockEntity2 = mock(Entity.class);

        gameArea.spawnEntity(mockEntity1);
        gameArea.spawnEntity(mockEntity2);

        gameArea.dispose();

        verify(mockEntity1).dispose();
        verify(mockEntity2).dispose();
    }

    @Test
    void testGetEntities() {
        GameArea gameArea = mock(GameArea.class);
        gameArea.areaEntities = new java.util.ArrayList<>(); // manually initialize

        doCallRealMethod().when(gameArea).spawnEntity(any(Entity.class));
        doCallRealMethod().when(gameArea).getEntities();

        Entity mockEntity1 = mock(Entity.class);
        Entity mockEntity2 = mock(Entity.class);

        gameArea.spawnEntity(mockEntity1);
        gameArea.spawnEntity(mockEntity2);

        var entities = gameArea.getEntities();
        assertEquals(2, entities.size());
        assertTrue(entities.contains(mockEntity1));
        assertTrue(entities.contains(mockEntity2));
    }

    @Test
    void testRemoveEntity() {
        GameArea gameArea = mock(GameArea.class);
        gameArea.areaEntities = new java.util.ArrayList<>(); // manually initialize

        doCallRealMethod().when(gameArea).spawnEntity(any(Entity.class));
        doCallRealMethod().when(gameArea).removeEntity(any(Entity.class));
        doCallRealMethod().when(gameArea).getEntities();

        Entity mockEntity = mock(Entity.class);
        gameArea.spawnEntity(mockEntity);
        
        gameArea.removeEntity(mockEntity);
        
        verify(mockEntity).setEnabled(false);
        assertFalse(gameArea.getEntities().contains(mockEntity));
    }

    @Test
    void testGetRoomSpawnPosition() {
        GameArea gameArea = mock(GameArea.class);
        gameArea.areaEntities = new java.util.ArrayList<>(); // manually initialize

        doCallRealMethod().when(gameArea).getRoomSpawnPosition(anyString());

        com.badlogic.gdx.math.Vector2 floor1Pos = gameArea.getRoomSpawnPosition("Floor1");
        assertTrue(floor1Pos.x >= 2f && floor1Pos.x <= 8f);
        assertTrue(floor1Pos.y >= 2f && floor1Pos.y <= 8f);

        com.badlogic.gdx.math.Vector2 floor2Pos = gameArea.getRoomSpawnPosition("Floor2");
        assertTrue(floor2Pos.x >= 4f && floor2Pos.x <= 18f);
        assertTrue(floor2Pos.y >= 4f && floor2Pos.y <= 18f);

        com.badlogic.gdx.math.Vector2 floor7Pos = gameArea.getRoomSpawnPosition("Floor7");
        assertTrue(floor7Pos.x >= 9f && floor7Pos.x <= 28f);
        assertTrue(floor7Pos.y >= 9f && floor7Pos.y <= 28f);

        com.badlogic.gdx.math.Vector2 defaultPos = gameArea.getRoomSpawnPosition("Unknown");
        assertEquals(0f, defaultPos.x, 0.001f);
        assertEquals(0f, defaultPos.y, 0.001f);
    }
}