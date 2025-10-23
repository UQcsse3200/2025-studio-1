package com.csse3200.game.files;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.files.FileLoader.MapEntitySpec;
import com.csse3200.game.files.LevelIO.LevelData;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LevelIOTest {
    @Test
    void load_returnsEmpty_whenBothTexturesAndEntitiesEmpty() {
        try (MockedStatic<FileLoader> st = mockStatic(FileLoader.class)) {
            st.when(() -> FileLoader.readTextureMap("lev.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.empty());
            st.when(() -> FileLoader.readMapEntities("lev.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.empty());

            var out = LevelIO.load("lev.json", FileLoader.Location.LOCAL);
            assertTrue(out.isEmpty());
        }
    }

    @Test
    void load_texturesOnly_returnsLevelDataWithEmptyEntities() {
        Map<String, String> tex = new LinkedHashMap<>();
        tex.put("button", "ui/button.png");

        try (MockedStatic<FileLoader> st = mockStatic(FileLoader.class)) {
            st.when(() -> FileLoader.readTextureMap("t.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(tex));
            st.when(() -> FileLoader.readMapEntities("t.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.empty());

            var ld = LevelIO.load("t.json", FileLoader.Location.LOCAL).orElseThrow();
            assertEquals(tex, ld.textures());
            assertTrue(ld.entities().isEmpty());
        }
    }

    @Test
    void load_entitiesOnly_returnsLevelDataWithEmptyTextures() {
        List<MapEntitySpec> ents = List.of(
                new MapEntitySpec("p1", "player", new GridPoint2(1, 2))
        );

        try (MockedStatic<FileLoader> st = mockStatic(FileLoader.class)) {
            st.when(() -> FileLoader.readTextureMap("e.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.empty());
            st.when(() -> FileLoader.readMapEntities("e.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(ents));

            var ld = LevelIO.load("e.json", FileLoader.Location.LOCAL).orElseThrow();
            assertTrue(ld.textures().isEmpty());
            assertEquals(ents, ld.entities());
        }
    }

    @Test
    void load_bothPresent_mergesIntoLevelData() {
        Map<String, String> tex = new LinkedHashMap<>();
        tex.put("button", "ui/button.png");
        tex.put("label", "ui/label.png");

        List<MapEntitySpec> ents = List.of(
                new MapEntitySpec("p1", "player", new GridPoint2(0, 0)),
                new MapEntitySpec("t1", "tree", new GridPoint2(2, 3))
        );

        try (MockedStatic<FileLoader> st = mockStatic(FileLoader.class)) {
            st.when(() -> FileLoader.readTextureMap("both.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(tex));
            st.when(() -> FileLoader.readMapEntities("both.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(ents));

            var ld = LevelIO.load("both.json", FileLoader.Location.LOCAL).orElseThrow();
            assertEquals(tex, ld.textures());
            assertEquals(ents, ld.entities());
        }
    }

    /* ------------------ enqueueTextures(...) branches ------------------ */

    @Test
    void enqueueTextures_loadsOnlyNotAlreadyLoaded() {
        AssetManager assets = mock(AssetManager.class);

        Map<String, String> tex = new LinkedHashMap<>();
        tex.put("button", "ui/button.png");       // already loaded -> skip
        tex.put("grass", "world/grass.png");     // not loaded -> load
        tex.put("label", "ui/label.png");        // not loaded -> load
        LevelData data = new LevelData(tex, List.of());

        when(assets.isLoaded("ui/button.png", Texture.class)).thenReturn(true);
        when(assets.isLoaded("world/grass.png", Texture.class)).thenReturn(false);
        when(assets.isLoaded("ui/label.png", Texture.class)).thenReturn(false);

        LevelIO.enqueueTextures(assets, data);

        verify(assets, never()).load("ui/button.png", Texture.class);
        verify(assets, times(1)).load("world/grass.png", Texture.class);
        verify(assets, times(1)).load("ui/label.png", Texture.class);
    }

    @Test
    void enqueueTextures_nullAssets_throwsNpe() {
        LevelData data = new LevelData(Map.of(), List.of());
        assertThrows(NullPointerException.class, () -> LevelIO.enqueueTextures(null, data));
    }

    /* --------------- loadTexturesBlocking(...) convenience --------------- */

    @Test
    void loadTexturesBlocking_enqueuesThenFinishes() {
        AssetManager assets = mock(AssetManager.class);
        Map<String, String> tex = Map.of("button", "ui/button.png");
        LevelData data = new LevelData(tex, List.of());

        when(assets.isLoaded("ui/button.png", Texture.class)).thenReturn(false);

        LevelIO.loadTexturesBlocking(assets, data);

        // enqueued
        verify(assets, times(1)).load("ui/button.png", Texture.class);
        // and finishLoading called
        verify(assets, times(1)).finishLoading();
    }
}
