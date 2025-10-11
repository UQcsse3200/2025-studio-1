package com.csse3200.game.ui.effects.texteffects;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EdgeBoostCyclesTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> cases() {
        return java.util.stream.Stream.of(
                Arguments.of(false, 10, 5, 0, 0.25f, 'A', 1.0f, 0),
                Arguments.of(true, 1, 0, 0, 0.25f, 'A', 1.0f, 0),
                Arguments.of(true, 5, 2, 2, 0.0f, 'A', 1.0f, 0),
                Arguments.of(true, 5, 2, 2, 0.25f, 'A', 0.0f, 0),
                Arguments.of(true, 10, 9, 0, 0.25f, 'A', 0.5f, -1),
                Arguments.of(true, 8, 7, 0, 1f / 7f, '5', 0.25f, -1)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    void edgeBoostCycles_matrix(boolean isBlast, int n, int i, int originIdx,
                                float invEdgeSpan, char t, float edgeBoost,
                                int expected) throws Exception {
        int got = (Integer) edge.invoke(null, isBlast, n, i, originIdx, invEdgeSpan, t, edgeBoost);

        if (expected >= 0) {
            assertEquals(expected, got);
        } else {
            int iglyph = (Integer) glyph.invoke(null, t);
            int exp = Math.round(Math.abs(i - originIdx) * invEdgeSpan * edgeBoost * iglyph);
            assertEquals(exp, got);
            assertTrue(got > 0);
        }
    }
}
