package com.csse3200.game.files.fileloader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class FileLoaderHandleTextureEntryTest {

    private static final String SAMPLE_PATH = "a/b/c.txt";
    private Method handleTextureEntry;
    private FileHandle fileForLogs;

    private static void setName(JsonValue v, String name) {
        try {
            Field f = JsonValue.class.getDeclaredField("name");
            f.setAccessible(true);
            f.set(v, name);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parameter set that hits id==null, path blank, path null, valid insert, duplicate keep-first.
     */
    static Stream<Arguments> entryCases() {
        JsonValue idNull = new JsonValue("whatever");
        setName(idNull, null);

        JsonValue pathBlank = new JsonValue("   ");
        setName(pathBlank, "buttonBlank");

        JsonValue pathNull = new JsonValue((String) null); // string-typed, null payload
        setName(pathNull, "buttonNull");

        JsonValue valid = new JsonValue("ui/button.png");
        setName(valid, "button");

        JsonValue dup = new JsonValue("world/override_button.png");
        setName(dup, "button");

        return Stream.of(
                Arguments.of("id==null → ignore", "uiTextures", idNull, null, Map.of()),
                Arguments.of("blank path → skip", "uiTextures", pathBlank, null, Map.of()),
                Arguments.of("null path  → skip", "uiTextures", pathNull, null, Map.of()),
                Arguments.of("valid insert", "uiTextures", valid, null,
                        new LinkedHashMap<>(Map.of("button", "ui/button.png"))),
                Arguments.of("duplicate keep first", "worldTextures", dup,
                        (Consumer<Map<String, String>>) m -> m.put("button", "ui/button.png"),
                        new LinkedHashMap<>(Map.of("button", "ui/button.png")))
        );
    }

    /**
     * Parameter set that *only* exercises the path==null/blank/valid branches thoroughly.
     */
    static Stream<Arguments> pathBranchCases() {
        // LHS true → short-circuit
        JsonValue pathNull = new JsonValue((String) null);
        setName(pathNull, "kNull");

        // RHS true variants
        JsonValue pathSpaces = new JsonValue("   ");
        setName(pathSpaces, "kSpaces");
        JsonValue pathEmpty = new JsonValue("");
        setName(pathEmpty, "kEmpty");
        JsonValue pathTabs = new JsonValue("\t");
        setName(pathTabs, "kTabs");

        // RHS false variants (ensure RHS is evaluated and false)
        JsonValue pathSingle = new JsonValue("X");
        setName(pathSingle, "kSingle");
        JsonValue pathValid = new JsonValue("ui/button.png");
        setName(pathValid, "kOk");

        return Stream.of(
                Arguments.of("path == null → skip", pathNull, Map.of()),
                Arguments.of("path is spaces → skip", pathSpaces, Map.of()),
                Arguments.of("path is empty  → skip", pathEmpty, Map.of()),
                Arguments.of("path is tab    → skip", pathTabs, Map.of()),
                Arguments.of("path single non-blank → insert",
                        pathSingle, new LinkedHashMap<>(Map.of("kSingle", "X"))),
                Arguments.of("path valid non-blank → insert",
                        pathValid, new LinkedHashMap<>(Map.of("kOk", "ui/button.png")))
        );
    }

    // --- helper to call the private method reflectively ---
    private static FileHandle invokeGetFileHandle(String filename, FileLoader.Location location) {
        try {
            var m = FileLoader.class.getDeclaredMethod("getFileHandle", String.class, FileLoader.Location.class);
            m.setAccessible(true);
            return (FileHandle) m.invoke(null, filename, location);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Reflection failed", e);
        }
    }

    private FileHandle invokeUnsafe(String filename, String locName) {
        try {
            Method m = FileLoader.class.getDeclaredMethod("getFileHandleUnsafe", String.class, String.class);
            m.setAccessible(true);
            return (FileHandle) m.invoke(null, filename, locName);
        } catch (Exception e) {
            throw new AssertionError("reflection failed", e);
        }
    }

    @Test
    void defaultBranch_unknownLocation_returnsNull_andLogs() {
        // Arrange: any filename; the path isn’t dereferenced in this branch
        String path = "unit-tests/ghf-default/dummy.txt";
        Gdx.files.local(path).writeString("", false);

        // Act
        FileHandle fh = invokeUnsafe(path, "WILD"); // unknown name → default arm

        // Assert
        assertNull(fh, "Unknown location name must return null (default branch)");
    }

    @Test
    void getFileHandle_throwsOnNulls() {
        assertThrows(NullPointerException.class, () -> FileLoader.getFileHandle(null, FileLoader.Location.LOCAL));
        assertThrows(NullPointerException.class, () -> FileLoader.getFileHandle("x", null));
    }

    @BeforeEach
    void setUp() throws Exception {
        handleTextureEntry = FileLoader.class.getDeclaredMethod(
                "handleTextureEntry", String.class, JsonValue.class, FileHandle.class, Map.class);
        handleTextureEntry.setAccessible(true);

        String path = "unit-tests/tex/" + UUID.randomUUID() + ".json";
        Gdx.files.local(path).parent().mkdirs();
        Gdx.files.local(path).writeString("{}", false);
        fileForLogs = Gdx.files.local(path);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("entryCases")
    void handleTextureEntry_param(String label, String group, JsonValue entry,
                                  Consumer<Map<String, String>> prefill,
                                  Map<String, String> expected) throws Exception {
        Map<String, String> out = new LinkedHashMap<>();
        if (prefill != null) prefill.accept(out);

        handleTextureEntry.invoke(null, group, entry, fileForLogs, out);

        assertEquals(expected, out, label);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("pathBranchCases")
    void coversPathNullOrBlankBranch(String label, JsonValue entry, Map<String, String> expected) throws Exception {
        Map<String, String> out = new LinkedHashMap<>();
        handleTextureEntry.invoke(null, "uiTextures", entry, fileForLogs, out);
        assertEquals(expected, out, label);
    }

    /**
     * Public API drive: ensure RHS true (blank) and RHS false (valid) via readTextureMap → processTextureGroup → handleTextureEntry.
     */
    @Test
    void publicPipeline_blankAndValid_hitBothSides() {
        String root = "unit-tests/texmap-blank/" + UUID.randomUUID();
        String path = root + "/textures.json";
        Gdx.files.local(root).mkdirs();

        String json = """
                {
                  "textures": {
                    "uiTextures": {
                      "blanky": "   ",
                      "kept":   "ui/button.png"
                    }
                  }
                }
                """;
        Gdx.files.local(path).writeString(json, false);

        var outOpt = FileLoader.readTextureMap(path, FileLoader.Location.LOCAL);
        var map = outOpt.orElseThrow();

        // blanky should be skipped by (path == null || path.isBlank())
        assertFalse(map.containsKey("blanky"));
        // kept should be inserted (false branch)
        assertEquals("ui/button.png", map.get("kept"));
    }

    @ParameterizedTest(name = "maps {0} correctly")
    @EnumSource(FileLoader.Location.class)
    void mapsEachLocationToCorrespondingGdxFiles(FileLoader.Location loc) {
        FileHandle viaLoader = invokeGetFileHandle(SAMPLE_PATH, loc);
        FileHandle expected = switch (loc) {
            case CLASSPATH -> Gdx.files.classpath(SAMPLE_PATH);
            case INTERNAL -> Gdx.files.internal(SAMPLE_PATH);
            case LOCAL -> Gdx.files.local(SAMPLE_PATH);
            case EXTERNAL -> Gdx.files.external(SAMPLE_PATH);
            case ABSOLUTE -> Gdx.files.absolute(SAMPLE_PATH);
        };

        // We can't rely on .equals() across FileHandle impls, but the path and type should match.
        assertNotNull(viaLoader, "FileHandle must not be null for " + loc);
        assertEquals(expected.path(), viaLoader.path(), "path mismatch for " + loc);
        assertEquals(expected.type(), viaLoader.type(), "type mismatch for " + loc);
    }

    @ParameterizedTest
    @EnumSource(FileLoader.Location.class)
    void mapsEachEnum(FileLoader.Location loc) throws Exception {
        var m = FileLoader.class.getDeclaredMethod("getFileHandle", String.class, FileLoader.Location.class);
        m.setAccessible(true);
        FileHandle via = (FileHandle) m.invoke(null, SAMPLE_PATH, loc);

        FileHandle expected = switch (loc) {
            case CLASSPATH -> Gdx.files.classpath(SAMPLE_PATH);
            case INTERNAL -> Gdx.files.internal(SAMPLE_PATH);
            case LOCAL -> Gdx.files.local(SAMPLE_PATH);
            case EXTERNAL -> Gdx.files.external(SAMPLE_PATH);
            case ABSOLUTE -> Gdx.files.absolute(SAMPLE_PATH);
        };

        assertNotNull(via);
        assertEquals(expected.path(), via.path());
        assertEquals(expected.type(), via.type());
    }

    @Test
    void enumOverload_nullGuards() throws Exception {
        var m = FileLoader.class.getDeclaredMethod("getFileHandle", String.class, FileLoader.Location.class);
        m.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> m.invoke(null, new Object[]{null, FileLoader.Location.LOCAL}));
        assertThrows(IllegalArgumentException.class, () -> m.invoke(null, "x", new Object[]{null}));
    }

    @Test
    void handleTextureEntry_skips_when_path_is_blank() throws Exception {
        // Arrange: entry whose value is whitespace → isString() = true, asString() = "   "
        JsonValue entry = new JsonValue("   ");
        entry.setName("player_idle"); // id must be non-null to get past the first guard

        // Any FileHandle is fine; only .path() is used for logging
        FileHandle file = new FileHandle(Files.createTempFile("texmap", ".json").toFile());

        Map<String, String> out = new HashMap<>();
        String groupName = "ui";

        // Act
        // Replace 'YourClass' with the class that defines handleTextureEntry(...)
        FileLoader.handleTextureEntry(groupName, entry, file, out);

        // Assert: blank path is skipped → nothing added
        assertTrue(out.isEmpty(), "No entries should be added when the texture path is blank");
    }

    @Test
    void handleTextureEntry_skips_when_path_is_null() throws Exception {
        // Create a JsonValue of type string, then null out its internal stringValue via reflection
        JsonValue entry = new JsonValue(JsonValue.ValueType.stringValue);
        entry.setName("enemy_walk");

        var f = JsonValue.class.getDeclaredField("stringValue");
        f.setAccessible(true);
        f.set(entry, null); // asString() will return null

        FileHandle file = new FileHandle(Files.createTempFile("texmap", ".json").toFile());
        Map<String, String> out = new HashMap<>();

        FileLoader.handleTextureEntry("sprites", entry, file, out);

        assertTrue(out.isEmpty(), "No entries should be added when the texture path is null");
    }
}

