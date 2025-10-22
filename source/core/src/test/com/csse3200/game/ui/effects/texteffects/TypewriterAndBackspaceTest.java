package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class TypewriterAndBackspaceTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> nullInitCases() {
        return java.util.stream.Stream.of(
                Arguments.of("fullText == null", null, 30f),
                Arguments.of("cps == 0", "abc", 0f),
                Arguments.of("cps < 0", "xyz", -5f)
        );
    }

    static java.util.stream.Stream<Arguments> backspaceCases() {
        return java.util.stream.Stream.of(
                Arguments.of("abc", 2),
                Arguments.of("hello", 0),
                Arguments.of("xy", 1)
        );
    }

    @ParameterizedTest(name = "typewriter \"{0}\" @ {1}cps")
    @CsvSource({
            "a,   60",
            "abc, 60",
            "'Hi', 45"
    })
    void typewriter_emits_full_text(String text, float cps) {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            var lbl = newLabel("");
            fx.typewriter(lbl, text, cps);
            assertEquals(text, lbl.getText().toString());
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("nullInitCases")
    void typewriter_early_return_when_init_null(String caseName, String fullText, float cps) throws Exception {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("");

        try (MockedStatic<Timer> timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat())).thenThrow(new AssertionError());
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt())).thenThrow(new AssertionError());
            timers.when(() -> Timer.post(any(Timer.Task.class))).thenThrow(new AssertionError());

            fx.typewriter(lbl, fullText, cps);
            assertNoTaskScheduled(fx);
        }
    }

    @ParameterizedTest(name = "backspaceTo from \"{0}\" -> {1}")
    @MethodSource("backspaceCases")
    void backspaceTo_reduces_to_target_length(String startText, int targetLen) {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            var lbl = newLabel("");
            fx.typewriter(lbl, startText, 60f);
            fx.backspaceTo(lbl, targetLen, 60f);
            assertEquals(targetLen, lbl.getText().length());
        }
    }
}
