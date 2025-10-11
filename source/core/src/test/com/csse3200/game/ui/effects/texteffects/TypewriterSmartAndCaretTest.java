package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypewriterSmartAndCaretTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> smartStrings() {
        return java.util.stream.Stream.of(
                Arguments.of("A,B."),
                Arguments.of("X!!Y"),
                Arguments.of("Q??R"),
                Arguments.of("a;a:")
        );
    }

    static java.util.stream.Stream<Arguments> caretCases() {
        return java.util.stream.Stream.of(
                Arguments.of("BASE", 0.01f, "|", "|"),
                Arguments.of("ABC", 0.00f, null, "▌")
        );
    }

    static Stream<Arguments> specialCaretCases() {
        return Stream.of(
                Arguments.of(null, "▌"),
                Arguments.of("", "▌"),
                Arguments.of("|", "|")
        );
    }

    @ParameterizedTest(name = "typewriterSmart \"{0}\"")
    @MethodSource("smartStrings")
    void typewriterSmart_ticks_and_finishes(String text) {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            var lbl = newLabel("");
            AtomicInteger ticks = new AtomicInteger(0);
            fx.typewriterSmart(lbl, text, 30f, 0.05f, 0.1f, ticks::incrementAndGet);
            assertEquals(text, lbl.getText().toString());
            assertTrue(ticks.get() >= text.length());
        }
    }

    @ParameterizedTest(name = "blinkCaret base=\"{0}\", caret=\"{2}\"")
    @MethodSource("caretCases")
    void blinkCaret_toggles_and_shows_caret(String base, float interval, String caret, String expectedCaret) {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            var lbl = newLabel("");
            lbl.setText(base);
            fx.blinkCaret(lbl, interval, caret);
            assertTrue(lbl.getText().toString().endsWith(expectedCaret));
        }
    }

    @ParameterizedTest(name = "blinkCaret caret=\"{0}\" -> uses \"{1}\"")
    @MethodSource("specialCaretCases")
    void blinkCaret_uses_default_or_custom_caret(String caret, String expectedCaret) {
        TextEffects fx = new TextEffects();
        var lbl = newLabel("BASE");

        var taskRef = new java.util.concurrent.atomic.AtomicReference<com.badlogic.gdx.utils.Timer.Task>();
        try (var timers = org.mockito.Mockito.mockStatic(com.badlogic.gdx.utils.Timer.class)) {
            timers.when(() -> com.badlogic.gdx.utils.Timer.schedule(
                            org.mockito.ArgumentMatchers.any(com.badlogic.gdx.utils.Timer.Task.class),
                            org.mockito.ArgumentMatchers.anyFloat(),
                            org.mockito.ArgumentMatchers.anyFloat()))
                    .thenAnswer(inv -> {
                        var t = (com.badlogic.gdx.utils.Timer.Task) inv.getArgument(0);
                        taskRef.set(t);
                        return t;
                    });

            fx.blinkCaret(lbl, /*interval*/ 0.01f, caret);

            // Step until we hit a visible-caret frame (bounded to avoid infinite loop)
            boolean seenVisible = false;
            for (int i = 0; i < 20; i++) {
                taskRef.get().run();
                if (lbl.getText().toString().endsWith(expectedCaret)) {
                    seenVisible = true;
                    break;
                }
            }

            assertTrue(seenVisible, "final text should end with expected caret");
        }
    }
}
