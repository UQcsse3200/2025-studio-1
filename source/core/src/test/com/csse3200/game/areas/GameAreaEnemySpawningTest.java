package com.csse3200.game.areas;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaEnemySpawningTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private GameArea gameArea;
    private Entity mockPlayer;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        mockPlayer = mock(Entity.class);
        
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

        doNothing().when(gameArea).registerEnemy(any());
    }


    @Test
    void testSpawnGhostGPT() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        spawnPositions.add(new Vector2(15f, 8f));
        positions.put("GhostGPT", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockGhostGPT = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createGhostGPT(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockGhostGPT);

            gameArea.spawnGhostGPT(2, 1.5f, mockPlayer, positions);

            // Verify NPCFactory was called for each spawn position
            npcFactoryMock.verify(() -> NPCFactory.createGhostGPT(eq(mockPlayer), eq(gameArea), eq(1.5f)), times(2));
            verify(mockGhostGPT, times(2)).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnDeepspin() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("DeepSpin", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockDeepSpin = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createDeepspin(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockDeepSpin);

            gameArea.spawnDeepspin(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createDeepspin(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockDeepSpin).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnGrokDroid() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("GrokDroid", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockGrokDroid = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createGrokDroid(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockGrokDroid);

            gameArea.spawnGrokDroid(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createGrokDroid(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockGrokDroid).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnVroomba() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("Vroomba", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockVroomba = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createVroomba(eq(mockPlayer), eq(1.5f)))
                    .thenReturn(mockVroomba);

            gameArea.spawnVroomba(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createVroomba(eq(mockPlayer), eq(1.5f)));
            verify(mockVroomba).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnTurret() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("Turret", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockTurret = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createTurret(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockTurret);

            gameArea.spawnTurret(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createTurret(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockTurret).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testGetEnemySpawnPosition() {
        // Test different room spawn positions
        Map<String, ArrayList<Vector2>> forestPositions = gameArea.getEnemySpawnPosition("Forest");
        assertTrue(forestPositions.containsKey("DeepSpin"));
        assertTrue(forestPositions.containsKey("Turret"));
        
        Map<String, ArrayList<Vector2>> receptionPositions = gameArea.getEnemySpawnPosition("Reception");
        assertTrue(receptionPositions.containsKey("Vroomba"));
        assertTrue(receptionPositions.containsKey("GhostGPT"));
        
        Map<String, ArrayList<Vector2>> mainhallPositions = gameArea.getEnemySpawnPosition("Mainhall");
        assertTrue(mainhallPositions.containsKey("DeepSpin"));
        assertTrue(mainhallPositions.containsKey("Vroomba"));
        
        Map<String, ArrayList<Vector2>> securityPositions = gameArea.getEnemySpawnPosition("Security");
        assertTrue(securityPositions.containsKey("GhostGPT"));
        assertTrue(securityPositions.containsKey("DeepSpin"));
        
        Map<String, ArrayList<Vector2>> officePositions = gameArea.getEnemySpawnPosition("Office");
        assertTrue(officePositions.containsKey("GhostGPT"));
        assertTrue(officePositions.containsKey("Vroomba"));
        assertTrue(officePositions.containsKey("DeepSpin"));
        
        Map<String, ArrayList<Vector2>> elevatorPositions = gameArea.getEnemySpawnPosition("Elevator");
        assertTrue(elevatorPositions.containsKey("GhostGPT"));
        assertTrue(elevatorPositions.containsKey("GrokDroid"));
        
        Map<String, ArrayList<Vector2>> researchPositions = gameArea.getEnemySpawnPosition("Research");
        assertTrue(researchPositions.containsKey("Turret"));
        assertTrue(researchPositions.containsKey("GhostGPT"));
        assertTrue(researchPositions.containsKey("GrokDroid"));
        
        Map<String, ArrayList<Vector2>> shippingPositions = gameArea.getEnemySpawnPosition("Shipping");
        assertTrue(shippingPositions.containsKey("GrokDroid"));
        
        Map<String, ArrayList<Vector2>> storagePositions = gameArea.getEnemySpawnPosition("Storage");
        assertTrue(storagePositions.containsKey("Turret"));
        assertTrue(storagePositions.containsKey("GrokDroid"));
        
        Map<String, ArrayList<Vector2>> serverPositions = gameArea.getEnemySpawnPosition("Server");
        assertTrue(serverPositions.containsKey("GhostGPT"));
        assertTrue(serverPositions.containsKey("DeepSpin"));
        
        Map<String, ArrayList<Vector2>> tunnelPositions = gameArea.getEnemySpawnPosition("Tunnel");
        assertTrue(tunnelPositions.containsKey("GhostGPT"));
        assertTrue(tunnelPositions.containsKey("Turret"));
        assertTrue(tunnelPositions.containsKey("Vroomba"));
        assertTrue(tunnelPositions.containsKey("GrokDroid"));
        
        // Test default case
        Map<String, ArrayList<Vector2>> defaultPositions = gameArea.getEnemySpawnPosition("Unknown");
        assertTrue(defaultPositions.isEmpty());
    }

    @Test
    void testSpawnDeepspinRed() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("DeepSpinRed", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockDeepSpinRed = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createDeepspinRed(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockDeepSpinRed);

            gameArea.spawnDeepspinRed(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createDeepspinRed(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockDeepSpinRed).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnDeepspinBlue() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("DeepSpinBlue", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockDeepSpinBlue = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createDeepspinBlue(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockDeepSpinBlue);

            gameArea.spawnDeepspinBlue(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createDeepspinBlue(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockDeepSpinBlue).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnGrokDroidRed() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("GrokDroidRed", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockGrokDroidRed = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createGrokDroidRed(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockGrokDroidRed);

            gameArea.spawnGrokDroidRed(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createGrokDroidRed(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockGrokDroidRed).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnGrokDroidBlue() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("GrokDroidBlue", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockGrokDroidBlue = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createGrokDroidBlue(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockGrokDroidBlue);

            gameArea.spawnGrokDroidBlue(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createGrokDroidBlue(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockGrokDroidBlue).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnVroombaRed() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("VroombaRed", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockVroombaRed = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createVroombaRed(eq(mockPlayer), eq(1.5f)))
                    .thenReturn(mockVroombaRed);

            gameArea.spawnVroombaRed(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createVroombaRed(eq(mockPlayer), eq(1.5f)));
            verify(mockVroombaRed).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnVroombaBlue() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("VroombaBlue", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockVroombaBlue = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createVroombaBlue(eq(mockPlayer), eq(1.5f)))
                    .thenReturn(mockVroombaBlue);

            gameArea.spawnVroombaBlue(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createVroombaBlue(eq(mockPlayer), eq(1.5f)));
            verify(mockVroombaBlue).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnGhostGPTRed() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("GhostGPTRed", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockGhostGPTRed = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createGhostGPTRed(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockGhostGPTRed);

            gameArea.spawnGhostGPTRed(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createGhostGPTRed(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockGhostGPTRed).setPosition(any(Vector2.class));
        }
    }

    @Test
    void testSpawnGhostGPTBlue() {
        Map<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> spawnPositions = new ArrayList<>();
        spawnPositions.add(new Vector2(10f, 5f));
        positions.put("GhostGPTBlue", spawnPositions);

        try (MockedStatic<NPCFactory> npcFactoryMock = mockStatic(NPCFactory.class)) {
            Entity mockGhostGPTBlue = mock(Entity.class);
            npcFactoryMock.when(() -> NPCFactory.createGhostGPTBlue(eq(mockPlayer), eq(gameArea), eq(1.5f)))
                    .thenReturn(mockGhostGPTBlue);

            gameArea.spawnGhostGPTBlue(1, 1.5f, mockPlayer, positions);

            npcFactoryMock.verify(() -> NPCFactory.createGhostGPTBlue(eq(mockPlayer), eq(gameArea), eq(1.5f)));
            verify(mockGhostGPTBlue).setPosition(any(Vector2.class));
        }
    }
}