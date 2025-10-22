package com.csse3200.game.ui.effects.texteffects;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GlitchAndLockingTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> glitchTargets() {
        return java.util.stream.Stream.of(
                Arguments.of("A B"),
                Arguments.of("   "),
                Arguments.of("XY")
        );
    }

    static java.util.stream.Stream<Arguments> lockProgressCases() {
        return java.util.stream.Stream.of(
                Arguments.of("AB C", "____", "", 3),
                Arguments.of("A B ", "xyz_", "1", 3),
                Arguments.of("Q R", "---", "", 0),
                Arguments.of("Z 9 ", "....", "", 2)
        );
    }

    static Stream<Arguments> noOpenCases() {
        return Stream.of(
                // name,                s,              idxArg (-1 means n), expectedRenderedTail, expectedPieces
                Arguments.of("all tail from 0", "hello world", 0, "hello world", 1),
                Arguments.of("nonzero tail", "prefix body", 7, "body", 1),
                Arguments.of("idx==n empty", "just text", -1, "", 0)
        );
    }

    @ParameterizedTest(name = "initGlitchState target=\"{0}\"")
    @MethodSource("glitchTargets")
    void initGlitchState_marks_spaces_locked_and_fills_nonspaces_with_glitch(String targetStr) throws Exception {
        char[] target = targetStr.toCharArray();
        char[] curr = new char[target.length];
        boolean[] locked = new boolean[target.length];

        initGlitch.invoke(null, target, curr, locked);

        String glitch = (String) glitchChars.get(null);
        for (int i = 0; i < target.length; i++) {
            if (target[i] == ' ') {
                assertTrue(locked[i]);
                assertEquals(' ', curr[i]);
            } else {
                assertFalse(locked[i]);
                assertTrue(glitch.indexOf(curr[i]) >= 0);
            }
        }
    }

    @ParameterizedTest(name = "lockCount frame={0}, total={1}, len={2} -> {3}")
    @CsvSource({
            "0,   10, 5, 0",
            "5,   10, 5, 3",
            "10,  10, 5, 5",
            "20,  10, 5, 5",
            "-1,  10, 5, 0",
            "0,   10, 0, 0"
    })
    void lockCountForFrame_cases(int frame, int total, int len, int expected) throws Exception {
        int got = (Integer) lockCount.invoke(null, frame, total, len);
        assertEquals(expected, got);
    }

    @ParameterizedTest(name = "lockProgress target=\"{0}\" lockCount={3}")
    @MethodSource("lockProgressCases")
    void lockProgress_matrix(String targetStr, String seedCurr, String initiallyLockedCsv, int lockCount) throws Exception {
        char[] target = targetStr.toCharArray();
        char[] curr = seedCurr.toCharArray();
        assertEquals(target.length, curr.length);

        boolean[] locked = new boolean[target.length];

        if (initiallyLockedCsv != null && !initiallyLockedCsv.isBlank()) {
            for (String s : initiallyLockedCsv.split(",")) {
                int idx = Integer.parseInt(s.trim());
                locked[idx] = true;
                curr[idx] = target[idx];
            }
        }

        boolean[] beforeLocked = locked.clone();
        char[] beforeCurr = curr.clone();

        lockProgress.invoke(null, locked, target, curr, lockCount);

        int limit = Math.min(lockCount, target.length);
        int expectedTotal = 0;
        for (boolean b : beforeLocked) if (b) expectedTotal++;

        for (int i = 0; i < limit; i++) {
            if (!beforeLocked[i] && target[i] != ' ') {
                expectedTotal++;
                assertTrue(locked[i]);
                assertEquals(target[i], curr[i]);
            } else {
                assertEquals(beforeLocked[i], locked[i]);
                assertEquals(beforeCurr[i], curr[i]);
            }
        }
        for (int i = limit; i < target.length; i++) {
            assertEquals(beforeLocked[i], locked[i]);
            assertEquals(beforeCurr[i], curr[i]);
        }

        int actualTotal = 0;
        for (boolean b : locked) if (b) actualTotal++;
        assertEquals(expectedTotal, actualTotal);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("noOpenCases")
    void processCrazyChunk_noOpen_branch(
            String _name,
            String s,
            int idxArg,
            String expectedTail,
            int expectedPieces
    ) throws Exception {
        int idx = (idxArg < 0) ? s.length() : idxArg;
        int n = s.length();

        List<Object> out = new ArrayList<>();
        int ret = (int) processCrazyChunk.invoke(null, s, idx, out);

        // Always returns n when no open tag is found
        assertEquals(n, ret, "should return string length when no more opens");

        // When idx < n, exactly one plain piece is added; when idx == n, none.
        assertEquals(expectedPieces, out.size(), "piece count mismatch for no-open branch");

        // If renderer is available, validate the exact emitted text
        if (expectedPieces == 1 && renderPieces != null) {
            @SuppressWarnings("unchecked")
            String rendered = (String) renderPieces.invoke(null, out, List.of());
            assertEquals(expectedTail, rendered, "emitted plain text should equal s.substring(idx)");
        }
    }
}
