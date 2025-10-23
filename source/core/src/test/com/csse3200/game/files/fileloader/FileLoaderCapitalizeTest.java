package com.csse3200.game.files.fileloader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class FileLoaderCapitalizeTest {
    private Method capitalize;

    @BeforeEach
    void reflect() throws Exception {
        capitalize = FileLoader.class.getDeclaredMethod("capitalize", String.class);
        capitalize.setAccessible(true);
    }

    @Test
    void returnsNullForNullInput() throws Exception {
        assertNull(capitalize.invoke(null, new Object[]{null}));
    }

    @Test
    void returnsEmptyForEmptyInput() throws Exception {
        assertEquals("", capitalize.invoke(null, ""));
    }

    @ParameterizedTest
    @CsvSource({
            "a, A",
            "zebra, Zebra",
            "alphaBeta, AlphaBeta",
            "Hello, Hello",
            "1hello, 1hello",
            "#tag, #tag",
            "' Hello', ' Hello'",
            "b, B",
            "B, B",
            "ñandú, Ñandú",
            "ßeta, ßeta"
    })
    void capitalizesFirstCharacterOnly(String in, String expected) throws Exception {
        assertEquals(expected, capitalize.invoke(null, in));
    }

    @Test
    void doesNotModifyRestOfString() throws Exception {
        String in = "kEEP_thisExactly";
        String expected = "K" + in.substring(1);
        String out = (String) capitalize.invoke(null, in);
        assertEquals(expected, out);
        assertEquals(in.substring(1), out.substring(1));
    }

    @Test
    void resolveExisting_executesDebugSupplier_andReturnsFile_whenPresent() throws Exception {
        // Arrange: make a real local file to pass the existence check
        String dir = "unit-tests/resolve-existing/" + UUID.randomUUID();
        String path = dir + "/present.json";
        Gdx.files.local(dir).mkdirs();
        Gdx.files.local(path).writeString("{}", false);

        // Reflect private static method: resolveExisting(String, Location, String, String)
        Method m = FileLoader.class.getDeclaredMethod(
                "resolveExisting", String.class, FileLoader.Location.class, String.class, String.class);
        m.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        Optional<FileHandle> out = (Optional<FileHandle>)
                m.invoke(null, path, FileLoader.Location.LOCAL, "read", "TestKind");

        // Assert: we hit the debug line (supplier line executed) and returned the file
        assertTrue(out.isPresent(), "Expected Optional.of(file) for existing file");
        assertEquals(path, out.get().path());
    }

    @Test
    void resolveExisting_executesDebugSupplier_andReturnsEmpty_whenMissing() throws Exception {
        String path = "unit-tests/resolve-existing/" + UUID.randomUUID() + "/missing.json";

        Method m = FileLoader.class.getDeclaredMethod(
                "resolveExisting", String.class, FileLoader.Location.class, String.class, String.class);
        m.setAccessible(true);

        @SuppressWarnings("unchecked")
        Optional<FileHandle> out = (Optional<FileHandle>)
                m.invoke(null, path, FileLoader.Location.LOCAL, "readTextureMap", "TextureMap");

        assertTrue(out.isEmpty(), "Expected Optional.empty() for missing file");
    }
}

