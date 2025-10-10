package com.csse3200.game.ui.effects.texteffects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RenderAndBlocksTest extends TextEffectsTestBase {
    static Object newBAWithString(String s) throws Exception {
        int n = s.length();
        int[] delays = new int[Math.max(1, n)];
        int[] remaining = new int[Math.max(1, n)];
        char[] target = s.toCharArray();
        char[] curr = s.toCharArray();
        Object vis = baVisCtor.newInstance(false, 0.6f, 18f, "ffffff", "ffe066");
        return baCtor.newInstance(60, delays, remaining, target, curr, vis, null);
    }

    static java.util.stream.Stream<Arguments> renderCases() throws Exception {
        return java.util.stream.Stream.of(
                Arguments.of(List.of(PLAIN("pre "), PLAIN("mid"), PLAIN(" post")), List.of(), "pre mid post"),
                Arguments.of(List.of(CRAZY("x"), CRAZY("y")), List.of(newBAWithString("B1"), newBAWithString("B2")), "B1B2"),
                Arguments.of(List.of(PLAIN("pre "), CRAZY("x"), PLAIN(" mid "), CRAZY("y"), PLAIN(" post")),
                        List.of(newBAWithString("C1"), newBAWithString("C2")), "pre C1 mid C2 post")
        );
    }


    static Stream<Arguments> buildCases() {
        return Stream.of(
                Arguments.arguments(
                        /* text      */ null,
                        /* optsSpec  */ "style=normal fps=0 rhz=0.6 rshift=18 flashHexA=invalid flashHexB=invalid",
                        /* expectedFps */ 1,
                        /* expectBlast */ false,
                        /* expHexA   */ "ffffff",
                        /* expHexB   */ "ffe066",
                        /* expRainbow*/ false,
                        /* expRhz    */ 0.6f,
                        /* expRshift */ 18f
                )
        );
    }

    static java.util.stream.Stream<Arguments> maxFpsCases() {
        return java.util.stream.Stream.of(
                Arguments.of(new int[]{}, 1),
                Arguments.of(new int[]{1}, 1),
                Arguments.of(new int[]{1, 1, 1}, 1),
                Arguments.of(new int[]{30, 30}, 30),
                Arguments.of(new int[]{30, 60, 45}, 60),
                Arguments.of(new int[]{2, 1}, 2)
        );
    }

    private static Object newBlock(int fps) throws Exception {
        int[] delays = new int[1];
        int[] remaining = new int[1];
        char[] target = new char[]{'A'};
        char[] curr = new char[]{'A'};
        Object visual = visDefaults.invoke(null);
        return baCtor.newInstance(fps, delays, remaining, target, curr, visual, null);
    }

    @ParameterizedTest(name = "renderPieces: case {index}")
    @MethodSource("renderCases")
    void renderPieces_matrix(List<Object> pieces, List<Object> blocks, String expected) throws Exception {
        String out = (String) renderPieces.invoke(null, pieces, blocks);
        assertEquals(expected, out);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("buildCases")
    void buildBlock_matrix(String text, String optsSpec,
                           int expectedFps, boolean expectBlast,
                           String expHexA, String expHexB, boolean expRainbow, float expRhz, float expRshift) throws Exception {

        Object opts = parseOpts.invoke(null, optsSpec);
        Object ba = buildBlock.invoke(null, text, opts);

        assertEquals(expectedFps, geti(ba, "fps"));

        int n = (text == null ? 0 : text.length());
        char[] target = (char[]) get(ba, "target");
        char[] curr = (char[]) get(ba, "curr");
        int[] delays = (int[]) get(ba, "delays");
        int[] remain = (int[]) get(ba, "remaining");

        assertEquals(n, target.length);
        assertEquals(n, curr.length);
        assertEquals(n, delays.length);
        assertEquals(n, remain.length);

        assertEquals(expHexA.toLowerCase(), gets(ba, "flashHexA"));
        assertEquals(expHexB.toLowerCase(), gets(ba, "flashHexB"));
        assertEquals(expRainbow, getb(ba, "rainbow"));
        assertEquals(expRhz, (Float) get(ba, "rhz"), 1e-6f);
        assertEquals(expRshift, (Float) get(ba, "rshift"), 1e-6f);

        int[] flashLeft = (int[]) get(ba, "flashLeft");
        int[] overshootLeft = (int[]) get(ba, "overshootLeft");
        int[] postLockHold = (int[]) get(ba, "postLockHold");

        if (expectBlast) {
            assertNotNull(flashLeft);
            assertEquals(n, flashLeft.length);
            assertNotNull(overshootLeft);
            assertEquals(n, overshootLeft.length);
            assertNotNull(postLockHold);
            assertEquals(n, postLockHold.length);
        } else {
            assertNull(flashLeft);
            assertNull(overshootLeft);
            assertNull(postLockHold);
        }
    }

    @ParameterizedTest(name = "[{index}] maxFps({0}) = {1}")
    @MethodSource("maxFpsCases")
    void maxFps_matrix(int[] fpsValues, int expected) throws Exception {
        java.util.List<Object> blocks = new ArrayList<>();
        for (int v : fpsValues) blocks.add(newBlock(v));
        int got = (Integer) maxFPS.invoke(null, blocks);
        assertEquals(expected, got);
    }

    @Test
    void buildBlock_explode_enables_distance_delay_and_isNotBlast() throws Exception {
        final String text = "ABCDE"; // n=5 -> a sensible spread

        Object explodeOpts = parseOpts.invoke(null,
                "style=explode fps=30 spread=4 jitter=0 cycles=0 edgeboost=0 rhz=0.8 rshift=12");

        Object normalOpts = parseOpts.invoke(null,
                "style=normal fps=30 spread=4 jitter=0 cycles=0 edgeboost=0 rhz=0.8 rshift=12");

        Object baExplode = buildBlock.invoke(null, text, explodeOpts);
        Object baNormal = buildBlock.invoke(null, text, normalOpts);

        int n = text.length();
        assertEquals(n, ((char[]) get(baExplode, "target")).length);
        assertEquals(n, ((char[]) get(baNormal, "target")).length);

        assertNull(get(baExplode, "flashLeft"));
        assertNull(get(baExplode, "overshootLeft"));
        assertNull(get(baExplode, "postLockHold"));

        int[] dExplode = (int[]) get(baExplode, "delays");
        int[] dNormal = (int[]) get(baNormal, "delays");

        for (int v : dNormal) assertEquals(0, v, "normal: delays should all be zero when jitter=0");

        boolean anyNonZero = false, anyDifferent = false;
        for (int i = 0; i < dExplode.length; i++) {
            if (dExplode[i] != 0) anyNonZero = true;
            if (i > 0 && dExplode[i] != dExplode[0]) anyDifferent = true;
        }
        assertTrue(anyNonZero, "explode: at least one delay should be non-zero");
        assertTrue(anyDifferent, "explode: delays should vary by distance (not all equal)");

        assertEquals(30, (int) get(baExplode, "fps"));
        assertEquals(30, (int) get(baNormal, "fps"));
    }
}
