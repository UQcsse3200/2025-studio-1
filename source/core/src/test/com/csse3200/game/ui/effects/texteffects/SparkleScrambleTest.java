package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class SparkleScrambleTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> early() {
        return java.util.stream.Stream.of(
                Arguments.of("", 1.0f, 5.0f, 0.2f),
                Arguments.of("A", 1.0f, 5.0f, 0.0f),
                Arguments.of("A", 0.0f, 5.0f, 0.2f)
        );
    }

    static java.util.stream.Stream<Arguments> runCases() {
        return java.util.stream.Stream.of(
                Arguments.of("A", 1.0f, 0.0f, 0.05f),
                Arguments.of("A", 1.0f, 6.0f, 0.05f)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("early")
    void sparkle_early_returns_no_schedule(String base, float density, float hz, float duration) throws Exception {
        TextEffects fx = new TextEffects();
        var lbl = newLabel(base);

        try (MockedStatic<Timer> timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt())).thenThrow(new AssertionError());
            timers.when(() -> Timer.post(any(Timer.Task.class))).thenThrow(new AssertionError());

            fx.sparkle(lbl, density, hz, duration);

            assertNoTaskScheduled(fx);
            assertEquals(base, lbl.getText().toString());
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("runCases")
    void sparkle_schedules_ticks_and_restores(String base, float density, float hz, float duration) {
        TextEffects fx = new TextEffects();
        var lbl = newLabel(base);

        final AtomicReference<Timer.Task> taskRef = new AtomicReference<>();
        final AtomicReference<Float> intervalRef = new AtomicReference<>();

        try (MockedStatic<Timer> timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        float interval = inv.getArgument(2, Float.class);
                        taskRef.set(t);
                        intervalRef.set(interval);
                        return t;
                    });

            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt())).thenThrow(new AssertionError());

            fx.sparkle(lbl, density, hz, duration);

            assertNotNull(taskRef.get());
            assertNotNull(intervalRef.get());
            assertEquals(1f / 60f, intervalRef.get(), 1e-6f);

            int fps = 60;
            int totalFrames = Math.max(1, Math.round(duration * fps));
            Timer.Task t = taskRef.get();
            for (int i = 0; i < totalFrames; i++) t.run();

            assertEquals(base, lbl.getText().toString());
        }
    }

    @Test
    void space_passthrough() throws Exception {
        StringBuilder sb = new StringBuilder();
        // phase and density irrelevant for spaces
        appendSparkleChar.invoke(null, sb, ' ', /*phaseA*/ true, /*density*/ 1f);
        assertEquals(" ", sb.toString());
    }

    @ParameterizedTest(name = "twinkle ON: phaseA={0}")
    @CsvSource({
            "true,  [#ffe066]A[]",
            "false, [#ffffff]A[]"
    })
    void twinkle_on_adds_color_markup(boolean phaseA, String expected) throws Exception {
        StringBuilder sb = new StringBuilder();
        // density=1 -> always twinkle regardless of RNG
        appendSparkleChar.invoke(null, sb, 'A', phaseA, 1f);
        assertEquals(expected, sb.toString());
    }

    @Test
    void twinkle_off_leaves_plain_char() throws Exception {
        StringBuilder sb = new StringBuilder();
        // density=0 -> never twinkle
        appendSparkleChar.invoke(null, sb, 'A', /*phaseA*/ true, 0f);
        assertEquals("A", sb.toString());
    }

    @Test
    void scrambleUnlocked_updates_only_unlocked_nonspace_indices() throws Exception {
        // target: [ 'A', ' ', 'B' ]
        char[] target = new char[]{'A', ' ', 'B'};
        // locked: [ false, false, true ]  -> only index 0 should change
        boolean[] locked = new boolean[]{false, false, true};
        // curr starts as sentinel '\0' so any write is detectable
        char[] curr = new char[]{'\u0000', '\u0000', '\u0000'};

        // invoke private static method
        scrambleUnlocked.invoke(null, locked, target, curr);

        String glitch = (String) glitchChars.get(null);

        // index 0: unlocked & non-space -> must become a glitch char (and not sentinel)
        assertNotEquals('\u0000', curr[0], "index 0 should be written");
        assertTrue(glitch.indexOf(curr[0]) >= 0, "index 0 must be a glitch char");

        // index 1: space -> must remain unchanged
        assertEquals('\u0000', curr[1], "space index must not change");

        // index 2: locked -> must remain unchanged
        assertEquals('\u0000', curr[2], "locked index must not change");
    }
}
