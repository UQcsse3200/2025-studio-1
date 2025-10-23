package com.csse3200.game.files.fileloader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.FileLoader.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileLoaderReadTextureMapTest {
    private String rootDir;

    private static <T> T req(Optional<T> o) {
        assertTrue(o.isPresent());
        return o.get();
    }


    private String p(String rel) {
        return rootDir + "/" + UUID.randomUUID() + (rel.startsWith("/") ? rel : "/" + rel);
    }

    @BeforeAll
    void setup() {
        rootDir = "unit-tests/texmap-" + UUID.randomUUID();
        Gdx.files.local(rootDir).mkdirs();
    }

    @AfterAll
    void clean() {
        var r = Gdx.files.local(rootDir);
        if (r.exists()) r.deleteDirectory();
    }

    @Test
    void supportsNestedAndLegacy_firstWins_dropsInvalid() {
        String path = p("textures.json");
        String json = """
                {
                  "textures": {
                    "uiTextures": {
                      "button": "ui/button.png",
                      "label": "ui/label.png",
                      "skipObj": {"nope":true},
                      "skipBlank": "   "
                    }
                  },
                  "worldTextures": {
                    "button": "world/override_button.png",
                    "grass": "world/grass.png"
                  }
                }
                """;
        Gdx.files.local(path).writeString(json, false);

        var map = req(FileLoader.readTextureMap(path, Location.LOCAL));
        var keys = new ArrayList<>(map.keySet());
        assertEquals(List.of("button", "label", "grass"), keys);
        assertEquals("ui/button.png", map.get("button"));
        assertEquals("ui/label.png", map.get("label"));
        assertEquals("world/grass.png", map.get("grass"));
        assertFalse(map.containsKey("skipObj"));
        assertFalse(map.containsKey("skipBlank"));
    }

    @Test
    void noGroups_returnsEmpty() {
        String path = p("empty.json");
        Gdx.files.local(path).writeString("{}", false);
        assertTrue(FileLoader.readTextureMap(path, Location.LOCAL).isEmpty());
    }

    @Test
    void missingFile_returnsEmpty() {
        var out = FileLoader.readTextureMap("nope-" + java.util.UUID.randomUUID() + ".json",
                FileLoader.Location.LOCAL);
        org.junit.jupiter.api.Assertions.assertTrue(out.isEmpty());
    }

    @Test
    void malformed_returnsEmpty() {
        String path = p("bad.json");
        Gdx.files.local(path).writeString("{", false);
        assertTrue(FileLoader.readTextureMap(path, Location.LOCAL).isEmpty());
    }

    @Test
    void nestedTexturesContainer_isProcessed() {
        String dir = "unit-tests/tex-nested/" + java.util.UUID.randomUUID();
        String path = dir + "/textures.json";
        Gdx.files.local(dir).mkdirs();

        String json = """
                {
                  "textures": {
                    "uiTextures": {
                      "button": "ui/button.png",
                      "label":  "ui/label.png",
                      "blank":  "   "
                    }
                  },
                  "legacyTextures": {
                    "grass": "world/grass.png"
                  }
                }
                """;
        Gdx.files.local(path).writeString(json, false);

        var out = FileLoader.readTextureMap(path, FileLoader.Location.LOCAL).orElseThrow();

        // first-wins: "button","label" from nested; "blank" skipped; root-level "grass" included later
        org.junit.jupiter.api.Assertions.assertEquals("ui/button.png", out.get("button"));
        org.junit.jupiter.api.Assertions.assertEquals("ui/label.png", out.get("label"));
        org.junit.jupiter.api.Assertions.assertFalse(out.containsKey("blank"));
        org.junit.jupiter.api.Assertions.assertEquals("world/grass.png", out.get("grass"));
    }

    @Test
    void nestedTexturesContainer_isProcessed_andLegacyAlsoScanned() {
        String path = p("textures.json");
        String json = """
                {
                  "textures": {
                    "uiTextures": {
                      "button": "ui/button.png",
                      "label":  "ui/label.png",
                      "blank":  "   "
                    }
                  },
                  "worldTextures": {
                    "grass": "world/grass.png"
                  }
                }
                """;
        Gdx.files.local(path).writeString(json, false);

        var outOpt = FileLoader.readTextureMap(path, Location.LOCAL);
        var map = outOpt.orElseThrow();

        // Nested entries included (except blank), root-level also included afterward
        assertEquals("ui/button.png", map.get("button"));
        assertEquals("ui/label.png", map.get("label"));
        assertFalse(map.containsKey("blank"));
        assertEquals("world/grass.png", map.get("grass"));

        // Optional: verify order is first-wins then legacy
        assertEquals(Map.of("button", "ui/button.png", "label", "ui/label.png", "grass", "world/grass.png").keySet(),
                map.keySet());
    }

    @Test
    void exceptionDuringTexturesCheck_hitsCatchAndReturnsEmpty() {
        // Prepare a trivially valid file so parsing is attempted
        String dir = "unit-tests/tex-catch/" + java.util.UUID.randomUUID();
        String path = dir + "/textures.json";
        Gdx.files.local(dir).mkdirs();
        Gdx.files.local(path).writeString("{\"textures\":{}}", false);

        // Build spies for root and textures nodes
        JsonValue rootSpy = spy(new JsonValue(JsonValue.ValueType.object));
        JsonValue texturesSpy = spy(new JsonValue(JsonValue.ValueType.object));
        // When readTextureMap looks up "textures", return our spy...
        when(rootSpy.get("textures")).thenReturn(texturesSpy);
        // ...and throw when it checks isObject() → triggers the catch block in readTextureMap
        doThrow(new RuntimeException("boom")).when(texturesSpy).isObject();

        try (MockedConstruction<JsonReader> ignored =
                     mockConstruction(JsonReader.class, (readerMock, ctx) -> {
                         // When tryParseRootObject calls reader.parse(file), return our crafted root spy
                         when(readerMock.parse(any(FileHandle.class))).thenReturn(rootSpy);
                     })) {

            var out = FileLoader.readTextureMap(path, Location.LOCAL);
            assertTrue(out.isEmpty(), "When texturesNode.isObject throws, method should catch and return empty");
        }
    }

    @Test
    void nestedContainer_true_branch_via_real_file() {
        String dir = "unit-tests/tex-nested-only/" + UUID.randomUUID();
        String path = dir + "/textures.json";
        Gdx.files.local(dir).mkdirs();
        Gdx.files.local(path).writeString("""
                  {
                    "textures": {
                      "uiTextures": { "button": "ui/button.png", "label": "ui/label.png" }
                    }
                  }
                """, false);

        var map = FileLoader.readTextureMap(path, Location.LOCAL).orElseThrow();
        assertEquals("ui/button.png", map.get("button"));
        assertEquals("ui/label.png", map.get("label"));
    }

    @Test
    void readTextureMap_handles_textures_present_but_not_object() throws Exception {
        // Arrange: "textures" is a STRING, not an OBJECT → should skip preferred path,
        // then fall back to legacy root-level groups.
        String json = """
                  {
                    "textures": "oops",
                    "uiTextures": {
                      "play": "images/btn_play.png"
                    }
                  }
                """;

        var temp = Files.createTempFile("texmap-nonobj-", ".json");
        Files.writeString(temp, json);
        FileHandle fh = new FileHandle(temp.toFile());

        // If your API wants a filename + Location, feed it the absolute path and ABSOLUTE location.
        // Replace FileLoader/Location with your actual class/enum names.
        var opt = FileLoader.readTextureMap(fh.file().getAbsolutePath(), Location.ABSOLUTE);

        // Assert: preferred branch is skipped; legacy path processes uiTextures.
        assertTrue(opt.isPresent(), "Expected non-empty map from legacy groups when 'textures' is not an object");
        Map<String, String> out = opt.get();
        assertEquals(1, out.size());
        assertEquals("images/btn_play.png", out.get("play"));
    }
}
