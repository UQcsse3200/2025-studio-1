package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class ProcessCrazyFlashHighlightTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> earlyReturnCases() {
        return java.util.stream.Stream.of(
                Arguments.of("hello world", "lo", 0, 2f, null, "hello world"),
                Arguments.of("hello", null, 2, 2f, "abc", "hello"),
                Arguments.of("hello", "", 2, 2f, "abc", "hello"),
                Arguments.of("", "x", 2, 2f, "abc", ""),
                Arguments.of("abc", "z", 3, 2f, "abc", "abc")
        );
    }

    static java.util.stream.Stream<Arguments> successCases() {
        return java.util.stream.Stream.of(
                Arguments.of("HELLO", "EL", 1, 2.0f, null, 1f / 2f),
                Arguments.of("abc", "b", 3, 0.0f, "AbCdEf", 10f)
        );
    }

    static java.util.stream.Stream<Arguments> earlyCases() {
        return java.util.stream.Stream.of(
                Arguments.of("flashes<=0", "hello world", "lo", 0, 2f, null),
                Arguments.of("substring=null", "hello", null, 2, 2f, "abc"),
                Arguments.of("substringEmpty", "hello", "", 2, 2f, "abc"),
                Arguments.of("emptyLabel", "", "x", 2, 2f, "abc"),
                Arguments.of("notFound", "abc", "z", 3, 2f, "abc")
        );
    }

    static Stream<Arguments> hexCases() {
        return Stream.of(
                // input colorHex, expected safeHex
                Arguments.of(null, "ffff00"),
                Arguments.of("", "ffff00"),
                Arguments.of("AbCdEf", "abcdef")
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("earlyReturnCases")
    void flashHighlight_early_returns(String labelText, String substring, int flashes, float hz,
                                      String colorHex, String expectedFinal) {
        TextEffects fx = new TextEffects();
        var lbl = newLabel(labelText);

        try (MockedStatic<Timer> timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat())).thenThrow(new AssertionError("No schedule(delay)"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat())).thenThrow(new AssertionError("No schedule(delay,interval)"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt())).thenThrow(new AssertionError("No schedule(delay,interval,repeat)"));
            timers.when(() -> Timer.post(any(Timer.Task.class))).thenThrow(new AssertionError("No post()"));

            fx.flashHighlight(lbl, substring, flashes, hz, colorHex);
            assertEquals(expectedFinal, lbl.getText().toString());
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("successCases")
    void flashHighlight_toggles_and_restores(String labelText, String substring, int flashes, float hz,
                                             String colorHex, float expectedInterval) {
        TextEffects fx = new TextEffects();
        var lbl = newLabel(labelText);

        AtomicReference<Timer.Task> taskRef = new AtomicReference<>();
        AtomicReference<Float> intervalRef = new AtomicReference<>();

        try (MockedStatic<Timer> timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        float interval = inv.getArgument(2, Float.class);
                        taskRef.set(t);
                        intervalRef.set(interval);
                        return t;
                    });
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat())).thenThrow(new AssertionError("Unexpected overload"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt())).thenThrow(new AssertionError("Unexpected overload"));

            fx.flashHighlight(lbl, substring, flashes, hz, colorHex);
            assertNotNull(intervalRef.get());
            assertEquals(expectedInterval, intervalRef.get(), 1e-6f);

            int idx = labelText.indexOf(substring);
            assertTrue(idx >= 0);
            String pre = labelText.substring(0, idx);
            String mid = labelText.substring(idx, idx + substring.length());
            String post = labelText.substring(idx + substring.length());
            String safeHex = (colorHex == null || colorHex.isEmpty()) ? "ffff00" : colorHex.toLowerCase(java.util.Locale.ROOT);

            Timer.Task task = taskRef.get();
            task.run();
            assertEquals(pre + "[#" + safeHex + "]" + mid + "[]" + post, lbl.getText().toString());

            if (flashes >= 2) {
                task.run();
                assertEquals(labelText, pre + mid + post);
                assertEquals(labelText, lbl.getText().toString());
            }

            int totalTicks = flashes * 2;
            int already = (flashes >= 2) ? 2 : 1;
            for (int k = already; k < totalTicks; k++) task.run();

            assertEquals(labelText, lbl.getText().toString());
        }
    }

    @ParameterizedTest(name = "early return: {0}")
    @MethodSource("earlyCases")
    void flashHighlight_early_returns(String _name, String labelText, String substring, int flashes, float hz, String colorHex) {
        TextEffects fx = new TextEffects();
        var lbl = newLabel(labelText);

        try (var timers = mockStatic(Timer.class)) {
            // Any schedule/post means the guard didn't fire
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt())).thenThrow(new AssertionError());
            timers.when(() -> Timer.post(any(Timer.Task.class))).thenThrow(new AssertionError());

            fx.flashHighlight(lbl, substring, flashes, hz, colorHex);

            assertEquals(labelText, lbl.getText().toString());
        }
    }

    @Test
    void flashHighlight_success_interval_clamps_and_toggles_then_restores() {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("HELLO");

        AtomicReference<Timer.Task> taskRef = new AtomicReference<>();
        AtomicReference<Float> intervalRef = new AtomicReference<>();

        try (var timers = mockStatic(Timer.class)) {
            // capture periodic schedule
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        float interval = inv.getArgument(2, Float.class);
                        taskRef.set(t);
                        intervalRef.set(interval);
                        return t;
                    });

            fx.flashHighlight(lbl, "EL", /*flashes*/ 2, /*hz*/ 0f, /*color*/ null);
            // hz=0 → clamp to 0.1 → interval = 1/0.1 = 10
            assertEquals(10f, intervalRef.get(), 1e-6f);

            // Tick 0: ON (colored)
            taskRef.get().run();
            assertEquals("H[#ffff00]EL[]LO", lbl.getText().toString());
            // Tick 1: OFF (plain)
            taskRef.get().run();
            assertEquals("HELLO", lbl.getText().toString());
            // Remaining ticks (2*flashes = 4): after completion restore original
            taskRef.get().run();
            taskRef.get().run();
            assertEquals("HELLO", lbl.getText().toString());
        }
    }

    @Test
    void flashHighlight_uses_fallback_hex_when_null() {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("HELLO");      // substring "EL" in the middle

        AtomicReference<Timer.Task> taskRef = new AtomicReference<>();
        AtomicReference<Float> intervalRef = new AtomicReference<>();

        try (var timers = mockStatic(Timer.class)) {
            // capture schedule(task, delay, interval) and do not auto-run
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        float interval = inv.getArgument(2, Float.class);
                        taskRef.set(t);
                        intervalRef.set(interval);
                        return t;
                    });

            fx.flashHighlight(lbl, "EL", /*flashes*/1, /*hz*/2f, /*colorHex*/ null);
            assertNotNull(taskRef.get());

            // First tick = ON (colored). Fallback hex must be "ffff00".
            taskRef.get().run();
            assertEquals("H[#ffff00]EL[]LO", lbl.getText().toString());
        }
    }

    @Test
    void flashHighlight_lowercases_supplied_hex() {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("HELLO");

        AtomicReference<Timer.Task> taskRef = new AtomicReference<>();
        try (var timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        taskRef.set(t);
                        return t;
                    });

            // Mixed-case hex should be lowercased in output
            fx.flashHighlight(lbl, "EL", /*flashes*/1, /*hz*/2f, /*colorHex*/ "AbCdEf");

            taskRef.get().run(); // ON frame
            assertEquals("H[#abcdef]EL[]LO", lbl.getText().toString());
        }
    }

    @ParameterizedTest(name = "safeHex: colorHex=\"{0}\" -> \"{1}\"")
    @MethodSource("hexCases")
    void flashHighlight_safeHex_branches(String inputHex, String expectedSafeHex) {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("HELLO"); // substring "EL" present

        // Capture the scheduled task; do not auto-run
        var taskRef = new AtomicReference<Timer.Task>();
        try (var timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        taskRef.set(t);
                        return t;
                    });

            // flashes=1 so totalTicks=2 (ON then OFF). We'll only run the first tick (ON).
            fx.flashHighlight(lbl, "EL", /*flashes*/1, /*hz*/2f, /*colorHex*/ inputHex);

            // First tick = ON (colored with safeHex)
            taskRef.get().run();

            // Expect H [#safeHex] EL [] LO
            String expected = "H[#" + expectedSafeHex + "]EL[]LO";
            assertEquals(expected, lbl.getText().toString());
        }
    }
}
