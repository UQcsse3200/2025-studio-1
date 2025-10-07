package com.csse3200.game.components.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Minimap class.
 * This class uses Mockito to isolate the Minimap from its dependencies on LibGDX and the game's ServiceLocator.
 */
@ExtendWith(MockitoExtension.class)
class MinimapTest {

    // A temporary directory for file-based tests, managed by JUnit.
    @TempDir
    static Path tempDir;

    private Minimap minimap;

    // Mocks for external dependencies.
    @Mock
    private DiscoveryService discoveryService;
    @Mock
    private GameArea gameArea;
    @Mock
    private Graphics graphics;

    private MockedStatic<ServiceLocator> serviceLocator;
    private MockedStatic<Gdx> gdxMock;

    @BeforeEach
    void setUp() {
        // Mock the static Gdx class to control graphics dimensions.
        graphics = mock(Graphics.class);
        gdxMock = mockStatic(Gdx.class);
        Gdx.graphics = graphics;

        discoveryService = mock(DiscoveryService.class);
        gameArea = mock(GameArea.class);

        // Mock the static ServiceLocator to provide mocked services.
        serviceLocator = mockStatic(ServiceLocator.class);
        serviceLocator.when(ServiceLocator::getDiscoveryService).thenReturn(discoveryService);
        serviceLocator.when(ServiceLocator::getGameArea).thenReturn(gameArea);

        // Initialize the minimap for testing. Screen dimensions are arbitrary for most tests.
        minimap = new Minimap(720, 1280);
    }

    @AfterEach
    void tearDown() {
        // Close the static mocks to avoid test interference.
        serviceLocator.close();
        gdxMock.close();
    }

    @Test
    void testAddRoom_Success() {
        assertTrue(minimap.addRoom(new Vector2(0, 0), "Room1"), "Should successfully add a new room.");
    }

    @Test
    void testAddRoom_FailsOnDuplicateCoordinates() {
        minimap.addRoom(new Vector2(1, 1), "Room1");
        assertFalse(minimap.addRoom(new Vector2(1, 1), "Room2"), "Should fail to add room at occupied coordinates.");
    }

    @Test
    void testAddRoom_FailsOnDuplicateName() {
        minimap.addRoom(new Vector2(0, 0), "Room1");
        assertFalse(minimap.addRoom(new Vector2(1, 1), "Room1"), "Should fail to add room with a duplicate name.");
    }

    @Test
    void testOpen_SetsCentreAndUpdatesDiscoveredRooms() {
        // Arrange
        minimap.addRoom(new Vector2(0, 0), "StartRoom");
        minimap.addRoom(new Vector2(0, 1), "NorthRoom");

        when(gameArea.toString()).thenReturn("StartRoom");

        // Mock discovery status
        when(discoveryService.isDiscovered("StartRoom")).thenReturn(true);
        when(discoveryService.isDiscovered("NorthRoom")).thenReturn(false);

        minimap.open();
        Map<Vector2, String> rendered = minimap.render();

        // Assert
        assertNotNull(rendered, "Render output should not be null after opening.");
        // Check that the discovered room's image path is updated
        assertTrue(rendered.containsValue("images/minimap-images/StartRoom.png"), "Discovered room should have its specific image.");
        // Check that the undiscovered room is not present
        assertFalse(rendered.containsValue("images/minimap-images/NorthRoom.png"), "Undiscovered room should not have its specific image.");
    }

    @Test
    void testClose_SetsCentreToNull() {
        // Arrange: open the map first
        minimap.addRoom(new Vector2(0, 0), "Room1");
        when(gameArea.toString()).thenReturn("Room1");
        minimap.open();
        assertNotNull(minimap.render(), "Map should be renderable after open().");

        // Act
        minimap.close();

        // Assert
        assertNull(minimap.render(), "Render should return null after the map is closed.");
    }

    @Test
    void testZoom() {
        assertEquals(1, minimap.getScale(), "Initial scale should be 1.");
        minimap.zoom(50); // Zoom in by 50%
        assertEquals(1.5f, minimap.getScale(), "Scale should increase after zooming in.");
        minimap.zoom(-20); // Zoom out by 20%
        assertEquals(1.2f, minimap.getScale(), 0.001, "Scale should decrease after zooming out.");
    }

    @Test
    void testPanAndZoom() {
        when(gameArea.toString()).thenReturn("Room1");
        when(discoveryService.isDiscovered("Room1")).thenReturn(true);

        minimap.addRoom(new Vector2(0, 0), "Room1");
        minimap.open();
        minimap.pan(new Vector2(10, 0));
        Map<Vector2, String> render = minimap.render();

        assertEquals(1, minimap.getScale());
        Vector2 correctPosition = new Vector2(630, 360);
        assertTrue(render.containsKey(correctPosition), "Map should pan 10 pixels left.");

        minimap.zoom(100);
        render = minimap.render();

        assertEquals(2, minimap.getScale());
        correctPosition = new Vector2(620, 360);
        assertTrue(render.containsKey(correctPosition), "Map should zoom in by 2x.");
    }

    @Test
    void testConstructor_WithFile() throws IOException {
        // Arrange: Create a temporary config file.
        File tempFile = new File(tempDir.toFile(), "minimap.cfg");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("RoomA, 0, 0");
            writer.newLine();
            writer.write("RoomB, 1, 0");
        }

        // Act
        Minimap fileMinimap = new Minimap(720, 1280, tempFile.getAbsolutePath());
        when(gameArea.toString()).thenReturn("RoomA");
        when(discoveryService.isDiscovered("RoomA")).thenReturn(true);
        when(discoveryService.isDiscovered("RoomB")).thenReturn(false); // Added missing stub for RoomB

        // Assert
        fileMinimap.open();
        Map<Vector2, String> rendered = fileMinimap.render();
        assertNotNull(rendered);
        assertTrue(rendered.containsValue("images/minimap-images/RoomA.png"));
    }

    @Test
    void testConstructor_WithInvalidFilePath() {
        // Act and Assert
        // This constructor catches the IOException and logs it. We can verify that no rooms are added.
        Minimap fileMinimap = new Minimap(720, 1280, "non/existent/path.cfg");
        assertThrows(NullPointerException.class, fileMinimap::open);
    }

    @Test
    void testEverything() {
        // Arrange
        minimap.addRoom(new Vector2(0, 0), "StartRoom");
        minimap.addRoom(new Vector2(0, 1), "NorthRoom");
        minimap.addRoom(new Vector2(1, 0), "EastRoom");

        // Mock player position and current room
        when(gameArea.toString()).thenReturn("StartRoom");

        // Mock discovery status
        when(discoveryService.isDiscovered("StartRoom")).thenReturn(true);
        when(discoveryService.isDiscovered("NorthRoom")).thenReturn(true);
        when(discoveryService.isDiscovered("EastRoom")).thenReturn(true);

        minimap.open();
        minimap.zoom(-98);
        Map<Vector2, String> rendered = minimap.render();
        minimap.pan(new Vector2(10, 10));
        rendered = minimap.render();
        minimap.pan(new Vector2(-10, -10));
        rendered = minimap.render();
        minimap.reset();
        minimap.zoom(-50);

        // Assert
        assertEquals(0.5, minimap.getScale());
        // All three rooms should be visible on 0.5x scale
        assertTrue(rendered.containsValue("images/minimap-images/StartRoom.png"));
        assertTrue(rendered.containsValue("images/minimap-images/NorthRoom.png"));
        assertTrue(rendered.containsValue("images/minimap-images/EastRoom.png"));

        minimap.zoom(100);
        minimap.pan(new Vector2(10, 10));
        rendered = minimap.render();
        // Assert
        assertEquals(1, minimap.getScale());
        // All three rooms should be visible when panned diagonally towards both rooms
        assertTrue(rendered.containsValue("images/minimap-images/StartRoom.png"));
        assertTrue(rendered.containsValue("images/minimap-images/NorthRoom.png"));
        assertTrue(rendered.containsValue("images/minimap-images/EastRoom.png"));

        minimap.pan(new Vector2(-10000, 0));
        rendered = minimap.render();
        // no rooms should be visible when panned away from all of them
        assertTrue(rendered.isEmpty());

        minimap.reset();
        rendered = minimap.render();
        // Should return to default minimap state, where zoom is 1 and is centred on player
        assertEquals(1, minimap.getScale());
        assertTrue(rendered.containsValue("images/minimap-images/StartRoom.png"));
        assertTrue(rendered.containsKey(new Vector2(640, 360)));



        // The rendered result should be null
        minimap.close();
        assertNull(minimap.render());
    }
}

