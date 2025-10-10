package com.csse3200.game.files.fileloader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileLoaderParseTest {

    private Method tryParseRootObject;

    @BeforeAll
    void reflect() throws Exception {
        tryParseRootObject = FileLoader.class.getDeclaredMethod("tryParseRootObject", FileHandle.class);
        tryParseRootObject.setAccessible(true);
    }

    private String tmp(String name) {
        String base = "unit-tests/try-parse-root/" + UUID.randomUUID();
        Gdx.files.local(base).mkdirs();
        return base + "/" + name;
    }

    private Optional<JsonValue> invoke(FileHandle fh) {
        try {
            @SuppressWarnings("unchecked")
            Optional<JsonValue> out = (Optional<JsonValue>) tryParseRootObject.invoke(null, fh);
            return out;
        } catch (Exception e) {
            throw new AssertionError("Reflection invoke failed", e);
        }
    }

    @Test
    void returnsRoot_whenTopLevelIsObject() {
        String path = tmp("object.json");
        Gdx.files.local(path).writeString("""
                {
                  "a": 1,
                  "b": {"c": 2}
                }
                """, false);

        Optional<JsonValue> out = invoke(Gdx.files.local(path));
        assertTrue(out.isPresent(), "Expected Optional.of(root) for object root");
        assertTrue(out.get().isObject(), "Root should be an object");
        assertEquals(1, out.get().getInt("a", -1));
        assertTrue(out.get().get("b").isObject());
    }

    @Test
    void returnsEmpty_whenTopLevelIsNotObject() {
        String path = tmp("array.json");
        Gdx.files.local(path).writeString("""
                [ {"x":1}, {"y":2} ]
                """, false);

        Optional<JsonValue> out = invoke(Gdx.files.local(path));
        assertTrue(out.isEmpty(), "Top-level array should return Optional.empty()");
    }

    @Test
    void returnsEmpty_whenParseThrowsException() {
        String path = tmp("malformed.json");
        Gdx.files.local(path).writeString("{", false); // malformed JSON → JsonReader throws

        Optional<JsonValue> out = invoke(Gdx.files.local(path));
        assertTrue(out.isEmpty(), "Malformed JSON should return Optional.empty()");
    }

    @Test
    void returnsEmpty_whenParseReturnsNull_rootIsNullBranch() throws Exception {
        // small helper to write any valid file; content doesn't matter because we stub parse(...)
        String base = "unit-tests/try-parse-root-null/" + UUID.randomUUID();
        Gdx.files.local(base).mkdirs();
        String path = base + "/any.json";
        Gdx.files.local(path).writeString("{}", false); // will be ignored by our stub
        FileHandle fh = Gdx.files.local(path);

        // Reflect private static method
        Method m = FileLoader.class.getDeclaredMethod("tryParseRootObject", FileHandle.class);
        m.setAccessible(true);

        // Intercept `new JsonReader()` inside the method and force parse(file) -> null
        try (MockedConstruction<JsonReader> ignored =
                     mockConstruction(JsonReader.class, (readerMock, ctx) -> when(readerMock.parse(any(FileHandle.class))).thenReturn(null))) {

            Optional<JsonValue> out =
                    (Optional<JsonValue>) m.invoke(null, fh);

            assertTrue(out.isEmpty(), "root == null should trigger Optional.empty()");
        }
    }
}
