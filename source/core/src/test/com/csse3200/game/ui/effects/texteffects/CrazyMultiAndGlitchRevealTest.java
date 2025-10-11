package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.csse3200.game.ui.effects.texteffects.RenderAndBlocksTest.newBAWithString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class CrazyMultiAndGlitchRevealTest extends TextEffectsTestBase {

    /* ----------------------------- startCrazyRevealMulti ----------------------------- */

    static Stream<Arguments> crazyCases() throws Exception {
        Object opts1 = parseOpts.invoke(null, "style=normal fps=30 cycles=1");
        Object opts2 = parseOpts.invoke(null, "style=normal fps=30 cycles=1");

        var piecesA = List.of(
                piecePlain.invoke(null, "pre "),
                pieceCrazy.invoke(null, "X", opts1),
                piecePlain.invoke(null, " post")
        );
        var blocksA = List.of(newBAWithString("X"));

        var piecesB = List.of(
                piecePlain.invoke(null, "pre "),
                pieceCrazy.invoke(null, "Y", opts1),
                piecePlain.invoke(null, " mid "),
                pieceCrazy.invoke(null, "Z", opts2),
                piecePlain.invoke(null, " post")
        );
        var blocksB = List.of(newBAWithString("Y"), newBAWithString("Z"));

        return Stream.of(
                Arguments.of(piecesA, blocksA),
                Arguments.of(piecesB, blocksB)
        );
    }

    static java.util.stream.Stream<Arguments> glitchEarlyCases() {
        return java.util.stream.Stream.of(
                Arguments.of(null, 0.5f, ""),   // null -> "", early via tgt.isEmpty()
                Arguments.of("", 1.0f, ""),   // empty string
                Arguments.of("OK", 0.0f, "OK")  // duration<=0
        );
    }

    @Test
    void startCrazy_noBlocks_setsText_immediately_and_noSchedule() throws Exception {
        var pieces = List.of(
                piecePlain.invoke(null, "pre "),
                piecePlain.invoke(null, "mid"),
                piecePlain.invoke(null, " post")
        );
        var lbl = newLabel("");

        try (var timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                    .thenThrow(new AssertionError("no schedule(delay) expected"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenThrow(new AssertionError("no schedule(interval) expected"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                    .thenThrow(new AssertionError("no schedule(repeat) expected"));

            startCrazyMulti.invoke(new TextEffects(), lbl, pieces);

            assertEquals("pre mid post", lbl.getText().toString());
        }
    }

    /* -------------------------------- glitchReveal --------------------------------- */

    @ParameterizedTest(name = "startCrazy interval = 1/maxFps")
    @MethodSource("crazyCases")
    void startCrazy_interval_matches_1_over_maxFps(
            List<Object> pieces
    ) throws Exception {
        var lbl = newLabel("");
        var fx = new TextEffects();

        // Build blocks exactly as production does, then compute expected FPS/interval.
        Object built = buildBlocks.invoke(null, pieces);
        int expectedFps = (Integer) maxFPS.invoke(null, built);
        float expectedInterval = 1f / expectedFps;

        var taskRef = new java.util.concurrent.atomic.AtomicReference<Timer.Task>();
        var intervalRef = new java.util.concurrent.atomic.AtomicReference<Float>();

        try (var timers = mockStatic(Timer.class)) {
            // Capture the scheduled task + interval
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        float interval = inv.getArgument(2, Float.class);
                        taskRef.set(t);
                        intervalRef.set(interval);
                        return t;
                    });
            // Disallow other overloads
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                    .thenThrow(new AssertionError("unexpected schedule(delay) overload"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                    .thenThrow(new AssertionError("unexpected schedule(repeat) overload"));
            // Ensure any posted cleanups run immediately
            timers.when(() -> Timer.post(any(Timer.Task.class)))
                    .thenAnswer(inv -> {
                        ((Timer.Task) inv.getArgument(0)).run();
                        return null;
                    });

            startCrazyMulti.invoke(fx, lbl, pieces);

            // Assert interval correctness
            assertNotNull(taskRef.get(), "task should be scheduled");
            assertEquals(expectedInterval, intervalRef.get(), 1e-6f, "interval must be 1/maxFps(blocks)");

            // Drive frames until output stabilises (no changes for 10 consecutive ticks), bounded.
            int guard = 0;
            int stableTicks = 0;
            String prev = lbl.getText().toString();

            while (guard++ < 10000 && stableTicks < 10) {
                taskRef.get().run();
                String cur = lbl.getText().toString();
                if (cur.equals(prev)) {
                    stableTicks++;
                } else {
                    stableTicks = 0;
                    prev = cur;
                }
            }

            assertTrue(stableTicks >= 10, "animation should stop updating after completion");
        }
    }


    @ParameterizedTest(name = "glitch early: text=\"{0}\", dur={1}")
    @MethodSource("glitchEarlyCases")
    void glitch_earlyReturn_setsFinalText_and_noSchedule(String finalText, float durationSec, String expected) {
        var fx = new TextEffects();
        var lbl = newLabel("seed");

        try (var timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                    .thenThrow(new AssertionError("no schedule(delay) expected"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenThrow(new AssertionError("no schedule(interval) expected"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                    .thenThrow(new AssertionError("no schedule(repeat) expected"));

            fx.glitchReveal(lbl, finalText, durationSec);
            assertEquals(expected, lbl.getText().toString());
        }
    }

    @Test
    void glitch_schedules_60fps_and_completes_to_target() throws Exception {
        var fx = new TextEffects();
        var lbl = newLabel("");

        AtomicReference<Timer.Task> taskRef = new AtomicReference<>();
        AtomicReference<Float> intervalRef = new AtomicReference<>();

        final String tgt = "AB C";
        final float dur = 0.05f; // ~3 frames @60fps
        final int fps = 60;
        final int totalFrames = Math.max(1, Math.round(dur * fps));

        try (var timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        float interval = inv.getArgument(2, Float.class);
                        taskRef.set(t);
                        intervalRef.set(interval);
                        return t;
                    });

            // Ensure any posted cleanup runs immediately (so task is cleared)
            timers.when(() -> Timer.post(any(Timer.Task.class)))
                    .thenAnswer(inv -> {
                        ((Timer.Task) inv.getArgument(0)).run();
                        return null;
                    });

            fx.glitchReveal(lbl, tgt, dur);

            assertEquals(1f / 60f, intervalRef.get(), 1e-6f);

            String glitch = (String) glitchChars.get(null);

            // Run all work frames: 0..totalFrames-1
            for (int i = 0; i < totalFrames; i++) {
                taskRef.get().run();
                String cur = lbl.getText().toString();
                assertEquals(tgt.length(), cur.length());
                for (int k = 0; k < cur.length(); k++) {
                    char c = cur.charAt(k);
                    char tc = tgt.charAt(k);
                    if (tc == ' ') {
                        assertEquals(' ', c);
                    } else {
                        assertTrue(glitch.indexOf(c) >= 0 || c == tc, "frame char must be glitch or target");
                    }
                }
            }

            // +1 tick triggers finalization (>= totalFrames) -> sets final text + cancel()
            taskRef.get().run();
            assertEquals(tgt, lbl.getText().toString());
        }
    }
}