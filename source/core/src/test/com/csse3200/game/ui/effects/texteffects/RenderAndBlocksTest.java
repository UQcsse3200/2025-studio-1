package com.csse3200.game.ui.effects.texteffects;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RenderAndBlocksTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> renderCases() throws Exception {
        return java.util.stream.Stream.of(
                Arguments.of(List.of(PLAIN("pre "), PLAIN("mid"), PLAIN(" post")), List.of(), "pre mid post"),
                Arguments.of(List.of(CRAZY("x"), CRAZY("y")), List.of(newBAWithString("B1"), newBAWithString("B2")), "B1B2"),
                Arguments.of(List.of(PLAIN("pre "), CRAZY("x"), PLAIN(" mid "), CRAZY("y"), PLAIN(" post")),
                        List.of(newBAWithString("C1"), newBAWithString("C2")), "pre C1 mid C2 post")
        );
    }

    static java.util.stream.Stream<Arguments> buildCases() {
        return java.util.stream.Stream.of(
                Arguments.arguments("normal-null", null,
                        "style=normal fps=0 rhz=0.6 rshift=18 flashHexA=invalid flashHexB=invalid",
                        1, false, "ffffff", "ffe066", false, 0.6f, 18f),
                Arguments.arguments("blast-two", "XZ",
                        "style=blast fps=60 rainbow=true rhz=1.2 rshift=33 flashHexA=#abc flashHexB=def flash=2 overshoot=3 edgeboost=1",
                        60, true, "aabbcc", "ddeeff", true, 1.2f, 33f),
                Arguments.arguments("explode-one", "Q",
                        "style=explode fps=30 rhz=0.8 rshift=45 flashHexA=zzzzzz flashHexB=",
                        30, false, "ffffff", "ffe066", false, 0.8f, 45f)
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
        Object visual = VIS_DEFAULTS.invoke(null);
        Object blast = null;
        return BA_CTOR.newInstance(fps, delays, remaining, target, curr, visual, blast);
    }

    @ParameterizedTest(name = "renderPieces: case {index}")
    @MethodSource("renderCases")
    void renderPieces_matrix(List<Object> pieces, List<Object> blocks, String expected) throws Exception {
        @SuppressWarnings("unchecked")
        String out = (String) RENDER_PIECES.invoke(null, pieces, blocks);
        assertEquals(expected, out);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("buildCases")
    void buildBlock_matrix(String _name, String text, String optsSpec,
                           int expectedFps, boolean expectBlast,
                           String expHexA, String expHexB, boolean expRainbow, float expRhz, float expRshift) throws Exception {

        Object opts = PARSE_OPTS.invoke(null, optsSpec);
        Object ba = BUILD_BLOCK.invoke(null, text, opts);

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
        int got = (Integer) MAX_FPS.invoke(null, blocks);
        assertEquals(expected, got);
    }
}
