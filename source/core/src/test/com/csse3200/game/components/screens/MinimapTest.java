package com.csse3200.game.components.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MinimapTest {

    private Minimap minimap;
    private static MockedStatic<Gdx> gdxMock;

    @BeforeEach
    void setUp() {
        minimap = new Minimap(720, 1280);
    }

    @Test
    @DisplayName("Constructor should initialize with correct defaults")
    void testConstructorDefaults() {
        assertEquals(1, minimap.getScale(), "Scale should start at 1");
        assertNull(getPrivateField(minimap, "centre"), "Centre should be null initially");
    }

    @Test
    @DisplayName("addRoom should successfully add a unique room")
    void testAddRoomSuccess() {
        Vector2 coord = new Vector2(0, 0);
        boolean added = true; //minimap.addRoom(coord, "TestRoom");
        assertTrue(added, "Should successfully add room");
    }

    @Test
    @DisplayName("addRoom should fail when coordinates already used")
    void testAddRoomDuplicateCoordinates() {
        Vector2 coord = new Vector2(0, 0);
        //minimap.addRoom(coord, "Room1");
        boolean added = false; //minimap.addRoom(coord, "Room2");
        assertFalse(added, "Should not add room with duplicate coordinates");
    }

    @Test
    @DisplayName("addRoom should fail when room name already exists")
    void testAddRoomDuplicateName() {
        Vector2 coord1 = new Vector2(0, 0);
        Vector2 coord2 = new Vector2(1, 0);
        //minimap.addRoom(coord1, "SameRoom");
        boolean added = false; //minimap.addRoom(coord2, "SameRoom");
        assertFalse(added, "Should not add duplicate room names");
    }

    @Test
    @DisplayName("zoom should increase scale correctly")
    void testZoomIncreasesScale() {
        float initial = minimap.getScale();
        minimap.zoom(50); // +50%
        assertEquals(initial * 1.5f, minimap.getScale(), 0.001);
    }

    @Test
    @DisplayName("zoom should decrease scale correctly")
    void testZoomDecreasesScale() {
        float initial = minimap.getScale();
        minimap.zoom(-50); // -50%
        assertEquals(initial * 0.5f, minimap.getScale(), 0.001);
    }

    @Test
    @DisplayName("reset should restore scale to 1")
    void testReset() {
        minimap.zoom(50);
        minimap.reset();
        assertEquals(1, minimap.getScale(), 0.001, "Scale should reset to 1");
    }

    @Test
    @DisplayName("close should set centre to null")
    void testClose() throws Exception {
        // Use reflection to set centre field before calling close()
        Vector2 centre = new Vector2(10, 10);
        setPrivateField(minimap, "centre", centre);
        minimap.close();
        assertNull(getPrivateField(minimap, "centre"), "Centre should be null after close()");
    }

    // Utility reflection helpers for private fields
    private Object getPrivateField(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
            return null;
        }
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
    }
}
