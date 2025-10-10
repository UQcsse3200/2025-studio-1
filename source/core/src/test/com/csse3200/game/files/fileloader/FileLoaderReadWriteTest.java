package com.csse3200.game.files.fileloader;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.FileLoader.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileLoaderReadWriteTest {
    private String rootDir;

    static Stream<Arguments> nullArgCases() {
        var pojo = new TestPojo();
        return Stream.of(
                Arguments.of(null, "x", Location.LOCAL),
                Arguments.of(pojo, null, Location.LOCAL),
                Arguments.of(pojo, "x", null)
        );
    }

    @BeforeAll
    void setupRoot() {
        rootDir = "unit-tests/readwrite-" + UUID.randomUUID();
        Gdx.files.local(rootDir).mkdirs();
    }

    @AfterAll
    void cleanup() {
        FileHandle root = Gdx.files.local(rootDir);
        if (root.exists()) root.deleteDirectory();
    }

    private String p(String rel) {
        return rootDir + "/" + UUID.randomUUID() + (rel.startsWith("/") ? rel : ("/" + rel));
    }

    @ParameterizedTest
    @EnumSource(Location.class)
    void read_missingFile_returnsEmpty(Location location) {
        assertTrue(FileLoader.read(TestPojo.class, "nope-" + UUID.randomUUID() + ".json", location).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void write_then_read_roundTrip_local(boolean pretty) {
        String path = p("roundtrip.json");
        TestPojo in = new TestPojo("alpha", 7);

        FileLoader.write(in, path, Location.LOCAL, pretty);
        assertTrue(Gdx.files.local(path).exists());

        var out = FileLoader.read(TestPojo.class, path, Location.LOCAL);
        assertEquals(in, out.orElseThrow());
    }

    @Test
    void write_toDirectory_triggersErrorPath_butNoThrow() {
        String dir = p("cant_write_here/");
        var dh = Gdx.files.local(dir);
        dh.mkdirs();
        assertTrue(dh.isDirectory());

        assertDoesNotThrow(() -> FileLoader.write(new TestPojo("x", 1), dir, Location.LOCAL, false));
        assertTrue(dh.exists() && dh.isDirectory());
    }

    @Test
    void write_handlesNullFileHandle_gracefully() {
        Files original = Gdx.files;
        Files nullFiles = new Files() {
            @Override
            public FileHandle getFileHandle(String path, FileType type) {
                return null;
            }

            @Override
            public FileHandle classpath(String path) {
                return null;
            }

            @Override
            public FileHandle internal(String path) {
                return null;
            }

            @Override
            public FileHandle external(String path) {
                return null;
            }

            @Override
            public FileHandle absolute(String path) {
                return null;
            }

            @Override
            public FileHandle local(String path) {
                return null;
            }

            @Override
            public String getExternalStoragePath() {
                return "";
            }

            @Override
            public boolean isExternalStorageAvailable() {
                return false;
            }

            @Override
            public String getLocalStoragePath() {
                return "";
            }

            @Override
            public boolean isLocalStorageAvailable() {
                return true;
            }
        };
        try {
            Gdx.files = nullFiles;
            assertDoesNotThrow(() ->
                    FileLoader.write(new TestPojo("alpha", 1), "any/path.json", Location.LOCAL, false));
        } finally {
            Gdx.files = original;
        }
    }

    @Test
    void read_returnsEmpty_whenDeserializerReturnsNull() {
        String path = p("null.json");
        Gdx.files.local(path).writeString("null", false);
        assertTrue(FileLoader.read(TestPojo.class, path, Location.LOCAL).isEmpty());
    }

    @ParameterizedTest(name = "{index}: object={0}, filename={1}, location={2}")
    @MethodSource("nullArgCases")
    void write_throwsNPE_onNulls_param(Object object, String filename, Location location) {
        assertThrows(NullPointerException.class, () -> FileLoader.write(object, filename, location, false));
    }

    @Test
    void read_throwsNPE_onNulls() {
        assertThrows(NullPointerException.class, () -> FileLoader.read(null, "x", Location.LOCAL));
        assertThrows(NullPointerException.class, () -> FileLoader.read(TestPojo.class, null, Location.LOCAL));
        assertThrows(NullPointerException.class, () -> FileLoader.read(TestPojo.class, "x", null));
    }

    static class TestPojo {
        public String name;
        public int x;

        TestPojo() {
        }

        TestPojo(String name, int x) {
            this.name = name;
            this.x = x;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestPojo that)) return false;
            return x == that.x && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, x);
        }
    }
}
