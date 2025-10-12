package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class MinimapDisplayTest {
    private MinimapDisplay display;
    private Stage mockStage;
    private Minimap mockMinimap;

    @BeforeEach
    void setup() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.getResourceService().loadAll();

        mockStage = mock(Stage.class);
        RenderService renderService = new RenderService();
        renderService.setStage(mockStage);
        ServiceLocator.registerRenderService(renderService);

        GdxGame mockGame = mock(GdxGame.class);
        mockMinimap = mock(Minimap.class);

        display = new MinimapDisplay(mockGame, mockMinimap);
        display.create();
        when(mockMinimap.getScale()).thenReturn(1.0f);
        when(mockMinimap.render()).thenReturn(new HashMap<>());
    }

    private static void setField(Object target, String field, Object value) throws Exception {
        Field f = findField(target.getClass(), field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static <T> T getField(Object target, String field) throws Exception {
        Field f = findField(target.getClass(), field);
        f.setAccessible(true);
        return (T) f.get(target);
    }

    private static Field findField(Class<?> cls, String name) throws NoSuchFieldException {
        while (cls != null) {
            try {
                return cls.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    @Test
    void testZoomInAndOutWithScaleBounds() {
        // Zoom in within limit
        when(mockMinimap.getScale()).thenReturn(1.0f);
        display.zoomIn();
        verify(mockMinimap).zoom(25f);

        // Zoom in at upper bound
        reset(mockMinimap);
        when(mockMinimap.getScale()).thenReturn(5.1f);
        display.zoomIn();
        verify(mockMinimap, never()).zoom(25f);

        // Zoom out within limit
        reset(mockMinimap);
        when(mockMinimap.getScale()).thenReturn(1.0f);
        display.zoomOut();
        verify(mockMinimap).zoom(-25f);

        // Zoom out below lower bound
        reset(mockMinimap);
        when(mockMinimap.getScale()).thenReturn(0.1f);
        display.zoomOut();
        verify(mockMinimap, never()).zoom(-25f);
    }

    @Test
    void testRenderMinimapImagesWithRooms() throws Exception {
        Map<Vector2, String> rooms = new HashMap<>();
        rooms.put(new Vector2(50, 100), "test-room.png");

        when(mockMinimap.render()).thenReturn(rooms);

        // Inject fake textures map
        Map<String, Texture> fakeTex = new HashMap<>();
        Texture texMock = mock(Texture.class);
        fakeTex.put("test-room.png", texMock);
        setField(display, "textures", fakeTex);

        display.renderMinimapImages();

        Table minimapTable = getField(display, "minimapTable");
        assertTrue(minimapTable.getChildren().size > 0);
    }

    @Test
    void testRenderMinimapImagesNullSafe() {
        when(mockMinimap.render()).thenReturn(null);
        assertDoesNotThrow(() -> display.renderMinimapImages());
    }

    @ParameterizedTest
    @ValueSource(floats = {1f, -1f, 0f})
    void testScrollZoomMethod(float amountY) {
        when(mockMinimap.getScale()).thenReturn(1.0f, 1.25f);
        when(mockMinimap.getCentre()).thenReturn(new Vector2(0f, 0f));

        display.zoom(100f, 200f, amountY);

        verify(mockMinimap, atLeastOnce()).zoom(anyFloat());
        verify(mockMinimap, atLeastOnce()).pan(any(Vector2.class));
    }

    @Test
    void testDisposeReleasesResourcesProperly() throws Exception {
        Texture t1 = mock(Texture.class);
        Texture t2 = mock(Texture.class);
        Map<String, Texture> texMap = new HashMap<>();
        texMap.put("a", t1);
        texMap.put("b", t2);
        setField(display, "textures", texMap);

        Table minimapTable = new Table();
        setField(display, "minimapTable", minimapTable);

        when(mockStage.getScrollFocus()).thenReturn(minimapTable);
        when(mockStage.getKeyboardFocus()).thenReturn(minimapTable);

        display.dispose();

        verify(mockMinimap).close();
        verify(mockStage).setScrollFocus(null);
        verify(mockStage).setKeyboardFocus(null);
        verify(t1).dispose();
        verify(t2).dispose();

        Map<String, Texture> after = getField(display, "textures");
        assertTrue(after.isEmpty());
    }

    @Test
    void testZoomNoScrollDoesNothing() {
        when(mockMinimap.getScale()).thenReturn(1.0f);
        when(mockMinimap.getCentre()).thenReturn(new Vector2(0, 0));

        display.zoom(150, 150, 0f);

        Vector2 zeroPan =  new Vector2(0, 0);

        verify(mockMinimap).zoom(0f);
        verify(mockMinimap).pan(zeroPan);
    }

    @Test
    void testZoomExtremeScrollValues() {
        when(mockMinimap.getScale()).thenReturn(1.0f, 1.25f);
        when(mockMinimap.getCentre()).thenReturn(new Vector2(0, 0));

        assertDoesNotThrow(() -> display.zoom(100f, 200f, 9999f));
        assertDoesNotThrow(() -> display.zoom(100f, 200f, -9999f));
    }

    @Test
    void testRenderMinimapImagesWithMultipleRoomsAndReuse() throws Exception {
        Map<Vector2, String> rooms = new HashMap<>();
        rooms.put(new Vector2(10, 10), "images/minimap-images/Casino.png");
        rooms.put(new Vector2(20, 20), "images/minimap-images/Elevator.png");

        when(mockMinimap.render()).thenReturn(rooms);
        when(mockMinimap.getScale()).thenReturn(1.0f);

        Map<String, Texture> texCache = spy(new HashMap<>());
        setField(display, "textures", texCache);

        display.renderMinimapImages();

        verify(texCache, times(2)).computeIfAbsent(anyString(), any());
    }

    @Test
    void testDisposeHandlesNullStageAndDimmer() throws Exception {
        setField(display, "minimapTable", new Table());
        setField(display, "stage", null);
        setField(display, "dimmer", null);

        Texture tex = mock(Texture.class);
        Map<String, Texture> map = new HashMap<>();
        map.put("x", tex);
        setField(display, "textures", map);

        display.dispose();

        verify(tex).dispose();
    }

    @Test
    void testZoomInOutSequence() {
        when(mockMinimap.getScale()).thenReturn(1.0f);
        display.zoomIn();
        verify(mockMinimap).zoom(25f);

        when(mockMinimap.getScale()).thenReturn(1.25f);
        display.zoomOut();
        verify(mockMinimap).zoom(-25f);
    }

    @Test
    void testDisposeWithNullTextures() throws Exception {
        Map<String, Texture> texMap = new HashMap<>();
        texMap.put("a", null);
        setField(display, "textures", texMap);

        display.dispose(); // should not throw
    }

    @Test
    void testZoomPanOffsetComputation() {
        when(mockMinimap.getScale()).thenReturn(1.0f, 1.25f);
        when(mockMinimap.getCentre()).thenReturn(new Vector2(0, 0));

        display.zoom(50f, 50f, 1f);

        verify(mockMinimap).pan(argThat(v -> Math.abs(v.len()) > 0));
    }

    @Test
    void testGetZIndex() {
        assertEquals(100f, display.getZIndex());
    }
}
