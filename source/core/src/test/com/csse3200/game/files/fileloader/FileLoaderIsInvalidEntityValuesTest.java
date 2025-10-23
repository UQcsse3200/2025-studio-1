package com.csse3200.game.files.fileloader;

import com.csse3200.game.files.FileLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileLoaderIsInvalidEntityValuesTest {
    private static Method isInvalid;

    @BeforeAll
    static void reflect() throws Exception {
        isInvalid = FileLoader.class.getDeclaredMethod(
                "isInvalidEntityValues", String.class, String.class, int.class, int.class);
        isInvalid.setAccessible(true);
    }

    static Stream<Arguments> cases() {
        int min = Integer.MIN_VALUE;
        return Stream.of(
                Arguments.of("name == null", null, "npc", 1, 1, true),
                Arguments.of("name blank", "  ", "npc", 1, 1, true),
                Arguments.of("type == null", "ok", null, 1, 1, true),
                Arguments.of("type blank", "ok", "", 1, 1, true),
                Arguments.of("x == MIN", "ok", "t", min, 1, true),
                Arguments.of("y == MIN", "ok", "t", 1, min, true),
                Arguments.of("all valid", "ok", "t", 3, 4, false)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cases")
    void coversAllBranches(String label, String name, String type, int x, int y, boolean expected) throws Exception {
        Object out = isInvalid.invoke(null, name, type, x, y);
        assertEquals(expected, out, label);
    }
}