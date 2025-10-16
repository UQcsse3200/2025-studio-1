package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaTerrainAndUtilityTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private GameArea gameArea;
    private ResourceService resourceService;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);
        
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        
        gameArea = mock(GameArea.class, withSettings()
                .useConstructor(terrainFactory, cameraComponent)
                .defaultAnswer(CALLS_REAL_METHODS));
        
        // Manually initialize areaEntities list
        try {
            var areaEntitiesField = GameArea.class.getDeclaredField("areaEntities");
            areaEntitiesField.setAccessible(true);
            areaEntitiesField.set(gameArea, new java.util.ArrayList<>());
        } catch (Exception e) {
            fail("Failed to initialize areaEntities: " + e.getMessage());
        }
    }




    @Test
    void testSetupTerrainWithoutOverlay() {
        TerrainComponent mockTerrain = mock(TerrainComponent.class);
        when(terrainFactory.createTerrain(any())).thenReturn(mockTerrain);
        
        gameArea.setupTerrainWithOverlay(terrainFactory, TerrainFactory.TerrainType.FOREST_DEMO, null);
        
        verify(terrainFactory).createTerrain(TerrainFactory.TerrainType.FOREST_DEMO);
        assertEquals(mockTerrain, gameArea.terrain);
        
        // Check that terrain entity was spawned
        assertTrue(gameArea.getEntities().stream()
                .anyMatch(entity -> entity.getComponent(TerrainComponent.class) == mockTerrain));
        
        // Check that no overlay entity was spawned
        assertFalse(gameArea.getEntities().stream()
                .anyMatch(entity -> entity.getComponent(SolidColorRenderComponent.class) != null));
    }


    @Test
    void testAddSolidWallLeft() {
        try (MockedStatic<ObstacleFactory> obstacleFactoryMock = mockStatic(ObstacleFactory.class)) {
            Entity mockWall = mock(Entity.class);
            obstacleFactoryMock.when(() -> ObstacleFactory.createWall(anyFloat(), anyFloat()))
                    .thenReturn(mockWall);
            
            GameArea.Bounds bounds = new GameArea.Bounds(0f, 20f, 0f, 15f, 20f, 15f, new Vector2(10f, 7.5f));
            
            gameArea.addSolidWallLeft(bounds, 0.5f);
            
            verify(mockWall).setPosition(0f, 0f);
            obstacleFactoryMock.verify(() -> ObstacleFactory.createWall(0.5f, 15f));
        }
    }

    @Test
    void testAddSolidWallRight() {
        try (MockedStatic<ObstacleFactory> obstacleFactoryMock = mockStatic(ObstacleFactory.class)) {
            Entity mockWall = mock(Entity.class);
            obstacleFactoryMock.when(() -> ObstacleFactory.createWall(anyFloat(), anyFloat()))
                    .thenReturn(mockWall);
            
            GameArea.Bounds bounds = new GameArea.Bounds(0f, 20f, 0f, 15f, 20f, 15f, new Vector2(10f, 7.5f));
            
            gameArea.addSolidWallRight(bounds, 0.5f);
            
            verify(mockWall).setPosition(19.5f, 0f);
            obstacleFactoryMock.verify(() -> ObstacleFactory.createWall(0.5f, 15f));
        }
    }

    @Test
    void testAddSolidWallTop() {
        try (MockedStatic<ObstacleFactory> obstacleFactoryMock = mockStatic(ObstacleFactory.class)) {
            Entity mockWall = mock(Entity.class);
            obstacleFactoryMock.when(() -> ObstacleFactory.createWall(anyFloat(), anyFloat()))
                    .thenReturn(mockWall);
            
            GameArea.Bounds bounds = new GameArea.Bounds(0f, 20f, 0f, 15f, 20f, 15f, new Vector2(10f, 7.5f));
            
            gameArea.addSolidWallTop(bounds, 0.5f);
            
            verify(mockWall).setPosition(0f, 14.5f);
            obstacleFactoryMock.verify(() -> ObstacleFactory.createWall(20f, 0.5f));
        }
    }

    @Test
    void testAddSolidWallBottom() {
        try (MockedStatic<ObstacleFactory> obstacleFactoryMock = mockStatic(ObstacleFactory.class)) {
            Entity mockWall = mock(Entity.class);
            obstacleFactoryMock.when(() -> ObstacleFactory.createWall(anyFloat(), anyFloat()))
                    .thenReturn(mockWall);
            
            GameArea.Bounds bounds = new GameArea.Bounds(0f, 20f, 0f, 15f, 20f, 15f, new Vector2(10f, 7.5f));
            
            gameArea.addSolidWallBottom(bounds, 0.5f);
            
            verify(mockWall).setPosition(0f, 0f);
            obstacleFactoryMock.verify(() -> ObstacleFactory.createWall(20f, 0.5f));
        }
    }

    @Test
    void testSpawnOrRepositionPlayerWithExistingPlayer() {
        Entity existingPlayer = mock(Entity.class);
        ServiceLocator.registerPlayer(existingPlayer);
        
        // Mock terrain
        TerrainComponent mockTerrain = mock(TerrainComponent.class);
        when(mockTerrain.tileToWorldPosition(any(GridPoint2.class)))
                .thenReturn(new Vector2(10f, 20f));
        
        try {
            Field terrainField = GameArea.class.getDeclaredField("terrain");
            terrainField.setAccessible(true);
            terrainField.set(gameArea, mockTerrain);
        } catch (Exception e) {
            fail("Failed to set terrain field: " + e.getMessage());
        }
        
        GridPoint2 spawnPosition = new GridPoint2(5, 10);
        Entity result = gameArea.spawnOrRepositionPlayer(spawnPosition);
        
        assertEquals(existingPlayer, result);
        verify(existingPlayer).setPosition(any(Vector2.class));
        verify(existingPlayer).setEnabled(true);
    }

    @Test
    void testSpawnOrRepositionPlayerWithNewPlayer() {
        ServiceLocator.clearPlayer();
        
        // Mock terrain
        TerrainComponent mockTerrain = mock(TerrainComponent.class);
        when(mockTerrain.tileToWorldPosition(any(GridPoint2.class)))
                .thenReturn(new Vector2(10f, 20f));
        when(mockTerrain.getTileSize()).thenReturn(2f);
        
        try {
            Field terrainField = GameArea.class.getDeclaredField("terrain");
            terrainField.setAccessible(true);
            terrainField.set(gameArea, mockTerrain);
        } catch (Exception e) {
            fail("Failed to set terrain field: " + e.getMessage());
        }
        
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            when(mockPlayer.getCenterPosition()).thenReturn(new Vector2(0.5f, 0.5f));
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);
            
            GridPoint2 spawnPosition = new GridPoint2(5, 10);
            Entity result = gameArea.spawnOrRepositionPlayer(spawnPosition);
            
            assertEquals(mockPlayer, result);
            verify(mockPlayer).setPosition(any(Vector2.class));
        }
    }


    @Test
    void testSpawnObjectDoorsWithNullPositions() {
        try (MockedStatic<ObstacleFactory> obstacleFactoryMock = mockStatic(ObstacleFactory.class)) {
            Entity mockDoor = mock(Entity.class);
            obstacleFactoryMock.when(ObstacleFactory::createDoor).thenReturn(mockDoor);
            
            gameArea.spawnObjectDoors(null, null);
            
            // Should not create any doors
            obstacleFactoryMock.verify(ObstacleFactory::createDoor, never());
        }
    }

    @Test
    void testSpawnItem() {
        Entity mockItem = mock(Entity.class);
        GridPoint2 position = new GridPoint2(5, 10);
        
        // Mock terrain
        TerrainComponent mockTerrain = mock(TerrainComponent.class);
        when(mockTerrain.tileToWorldPosition(any(GridPoint2.class)))
                .thenReturn(new Vector2(10f, 20f));
        when(mockTerrain.getTileSize()).thenReturn(2f);
        
        try {
            Field terrainField = GameArea.class.getDeclaredField("terrain");
            terrainField.setAccessible(true);
            terrainField.set(gameArea, mockTerrain);
        } catch (Exception e) {
            fail("Failed to set terrain field: " + e.getMessage());
        }
        
        gameArea.spawnItem(mockItem, position);
        
        verify(mockItem).setPosition(any(Vector2.class));
        assertTrue(gameArea.getEntities().contains(mockItem));
    }

    @Test
    void testSpawnEntityInRoom() {
        Entity mockEntity = mock(Entity.class);
        String roomName = "Floor2";
        
        gameArea.spawnEntityInRoom(roomName, mockEntity);
        
        verify(mockEntity).setPosition(any(Vector2.class));
        assertTrue(gameArea.getEntities().contains(mockEntity));
    }

    @Test
    void testGetRoomSpawnPosition() {
        Vector2 floor1Pos = gameArea.getRoomSpawnPosition("Floor1");
        assertTrue(floor1Pos.x >= 2f && floor1Pos.x <= 8f);
        assertTrue(floor1Pos.y >= 2f && floor1Pos.y <= 8f);
        
        Vector2 floor2Pos = gameArea.getRoomSpawnPosition("Floor2");
        assertTrue(floor2Pos.x >= 4f && floor2Pos.x <= 18f);
        assertTrue(floor2Pos.y >= 4f && floor2Pos.y <= 18f);
        
        Vector2 floor7Pos = gameArea.getRoomSpawnPosition("Floor7");
        assertTrue(floor7Pos.x >= 9f && floor7Pos.x <= 28f);
        assertTrue(floor7Pos.y >= 9f && floor7Pos.y <= 28f);
        
        Vector2 defaultPos = gameArea.getRoomSpawnPosition("Unknown");
        assertEquals(0f, defaultPos.x, 0.001f);
        assertEquals(0f, defaultPos.y, 0.001f);
    }

    @Test
    void testEnsureTextures() {
        String[] texturePaths = {"texture1.png", "texture2.png"};
        
        when(resourceService.containsAsset("texture1.png", Texture.class)).thenReturn(false);
        when(resourceService.containsAsset("texture2.png", Texture.class)).thenReturn(true);
        
        gameArea.ensureTextures(texturePaths);
        
        verify(resourceService).loadTextures(new String[]{"texture1.png"});
        verify(resourceService).loadAll();
    }

    @Test
    void testEnsureAtlases() {
        String[] atlasPaths = {"atlas1.atlas", "atlas2.atlas"};
        
        when(resourceService.containsAsset("atlas1.atlas", TextureAtlas.class)).thenReturn(false);
        when(resourceService.containsAsset("atlas2.atlas", TextureAtlas.class)).thenReturn(true);
        
        gameArea.ensureAtlases(atlasPaths);
        
        verify(resourceService).loadTextureAtlases(new String[]{"atlas1.atlas"});
        verify(resourceService).loadAll();
    }

    @Test
    void testEnsurePlayerAtlas() {
        when(resourceService.containsAsset("images/player.atlas", TextureAtlas.class)).thenReturn(false);
        
        gameArea.ensurePlayerAtlas();
        
        verify(resourceService).loadTextureAtlases(new String[]{"images/player.atlas"});
        verify(resourceService).loadAll();
    }

    @Test
    void testUnloadAssets() {
        String[] assetPaths = {"asset1.png", "asset2.atlas"};
        
        gameArea.unloadAssets(assetPaths);
        
        verify(resourceService).unloadAssets(assetPaths);
    }
}