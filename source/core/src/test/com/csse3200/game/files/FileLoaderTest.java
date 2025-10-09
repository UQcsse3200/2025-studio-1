package com.csse3200.game.files;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(GameExtension.class)
class FileLoaderTest {

    private static final Logger logger = LoggerFactory.getLogger(FileLoaderTest.class);

    @Test
    void loadFromValidFile() {
        TestStats test = FileLoader
                .read(TestStats.class, "test/files/valid.json", FileLoader.Location.INTERNAL)
                .orElseThrow(() -> new AssertionError("Missing or invalid test/files/valid.json"));
        assertEquals(3, test.stat1);
        assertEquals(4, test.stat2);
    }


    @Test
    void loadFromEmptyFile() {
        TestStats test = FileLoader
                .read(TestStats.class, "test/files/empty.json", FileLoader.Location.INTERNAL)
                .orElseThrow(() -> new AssertionError("Missing or invalid test/files/empty.json"));

        assertNotNull(test);
        assertEquals(1, test.stat1);
        assertEquals(2, test.stat2);
    }

    @Test
    void loadFromMissingFile_returnsEmptyOptional() {
        var result = FileLoader.read(
                TestStats.class,
                "test/files/missing.json",
                FileLoader.Location.INTERNAL);

        assertTrue(result.isEmpty(), "Expected empty Optional for a missing file");
    }

    @Test
    void loadFromInvalidFile_returnsEmptyOptional() {
        var result = FileLoader.read(
                TestStats.class,
                "test/files/invalid.json",
                FileLoader.Location.INTERNAL);

        assertTrue(result.isEmpty(), "Expected empty Optional for an invalid JSON file");
    }
}
