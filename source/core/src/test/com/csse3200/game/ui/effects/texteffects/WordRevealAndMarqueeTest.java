package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class WordRevealAndMarqueeTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> earlyReturnCases() {
        return java.util.stream.Stream.of(
                Arguments.of(null, 10f, ""),
                Arguments.of("", 10f, ""),
                Arguments.of("abc", 0f, "abc"),
                Arguments.of("xyz", -1f, "xyz")
        );
    }

    static java.util.stream.Stream<Arguments> marqueeCases() {
        return java.util.stream.Stream.of(
                Arguments.of("HELLO", 3),
                Arguments.of("ABCDE", 1),
                Arguments.of("X", 5)
        );
    }

    static java.util.stream.Stream<Arguments> windowCases() {
        return java.util.stream.Stream.of(
                Arguments.of("HELLO", 3),
                Arguments.of("X", 5),    // window wider than text
                Arguments.of("", 2),     // empty text
                Arguments.of(null, 0)    // null text + zero window -> coerced to 1
        );
    }

    @ParameterizedTest(name = "wordReveal \"{0}\"")
    @CsvSource({"'Hi  there!'", "'a b  c'"})
    void wordReveal_completes(String text) {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            var lbl = newLabel("");
            fx.wordReveal(lbl, text, 40f);
            assertEquals(text, lbl.getText().toString());
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("earlyReturnCases")
    void wordReveal_earlyReturn_guard_is_hit(String fullText, float wps, String expected) {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("");

        try (MockedStatic<Timer> timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt())).thenThrow(new AssertionError());
            timers.when(() -> Timer.post(any(Timer.Task.class))).thenThrow(new AssertionError());
            fx.wordReveal(lbl, fullText, wps);
            assertEquals(expected, lbl.getText().toString());
        }
    }

    @ParameterizedTest(name = "marquee width={1} on \"{0}\" -> fixed window")
    @MethodSource("marqueeCases")
    void marquee_window_length(String text, int width) {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            var lbl = newLabel("");
            fx.marquee(lbl, text, width, 20f);
            assertEquals(width, lbl.getText().length(), "marquee renders a fixed-width window");
        }
    }

    @ParameterizedTest(name = "fixed window: text=\"{0}\", win={1}")
    @MethodSource("windowCases")
    void marquee_renders_fixed_window(String text, int window) {
        try (var ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            var lbl = newLabel("");

            fx.marquee(lbl, text, window, 20f);

            // After immediate runs, the view should always be exactly `window` chars long (or 1 if window<=0)
            int expectedLen = Math.max(1, window);
            assertEquals(expectedLen, lbl.getText().length());
        }
    }

    @ParameterizedTest(name = "interval cps={0} -> 1/max(1,cps)")
    @CsvSource({
            "0,   1.0",
            "1,   1.0",
            "5,   0.2",
            "20,  0.05"
    })
    void marquee_interval_is_inverse_of_cps(float cps, float expectedInterval) {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("");

        AtomicReference<Timer.Task> taskRef = new AtomicReference<>();
        AtomicReference<Float> intervalRef = new AtomicReference<>();

        try (var timers = mockStatic(Timer.class)) {
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
                    .thenThrow(new AssertionError("Unexpected schedule(delay) overload"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                    .thenThrow(new AssertionError("Unexpected schedule(delay,interval,repeat) overload"));

            fx.marquee(lbl, "ABC", 2, cps);

            assertNotNull(taskRef.get(), "Timer task should be scheduled");
            assertEquals(expectedInterval, intervalRef.get(), 1e-6f);
        }
    }

    @Test
    void marquee_slides_and_wraps_expected_sequence() {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("");

        AtomicReference<Timer.Task> taskRef = new AtomicReference<>();

        try (var timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenAnswer(inv -> {
                        Timer.Task t = inv.getArgument(0);
                        taskRef.set(t);
                        return t;
                    });

            // Use a tiny string + win=3 so we exercise wrap-around
            final String text = "AB";
            final int win = 3;
            fx.marquee(lbl, text, win, /*cps*/ 10f);

            Timer.Task task = taskRef.get();
            assertNotNull(task, "Timer task should be captured");

            // Precomputed expected views for offsets 0..5 on tape = "   AB   "
            List<String> expected = List.of(
                    "   ", // 0
                    "  A", // 1
                    " AB", // 2
                    "AB ", // 3
                    "B  ", // 4
                    "   "  // 5
            );

            for (String exp : expected) {
                task.run(); // advance one tick
                assertEquals(win, lbl.getText().length(), "window length must stay constant");
                assertEquals(exp, lbl.getText().toString(), "sliding window content mismatch");
            }
        }
    }

    @Test
    void marquee_handles_null_text_and_zero_window() {
        try (var ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            var lbl = newLabel("seed");

            // null text -> treated as "", window<=0 -> coerced to 1
            fx.marquee(lbl, null, 0, 5f);
            assertEquals(1, lbl.getText().length(), "win coerced to 1");
            // Content will be a single whitespace; just assert it’s a single char
            assertEquals(1, lbl.getText().toString().length());
        }
    }
}
