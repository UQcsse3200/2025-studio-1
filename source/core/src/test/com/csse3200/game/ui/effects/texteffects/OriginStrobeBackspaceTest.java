package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OriginStrobeBackspaceTest extends TextEffectsTestBase {

    /* ------------------------- originIndex ------------------------- */

    static Stream<Arguments> originCases() {
        return Stream.of(
                // n == 0 → all should clamp to 0
                Arguments.of(0, "LEFT", 0),
                Arguments.of(0, "MIDDLE", 0),
                Arguments.of(0, "RIGHT", 0),

                // n == 1 → only index 0 valid
                Arguments.of(1, "LEFT", 0),
                Arguments.of(1, "MIDDLE", 0),
                Arguments.of(1, "RIGHT", 0),

                // n == 2 → RIGHT -> 1, MIDDLE -> 0 (since (2-1)/2 = 0)
                Arguments.of(2, "LEFT", 0),
                Arguments.of(2, "MIDDLE", 0),
                Arguments.of(2, "RIGHT", 1),

                // n == 5 → LEFT=0, MIDDLE=(5-1)/2=2, RIGHT=4
                Arguments.of(5, "LEFT", 0),
                Arguments.of(5, "MIDDLE", 2),
                Arguments.of(5, "RIGHT", 4)
        );
    }

    private static void setBasePlain(TextEffects fx, String val) throws Exception {
        Field f = TextEffects.class.getDeclaredField("basePlain");
        f.setAccessible(true);
        if (Modifier.isStatic(f.getModifiers())) f.set(null, val);
        else f.set(fx, val);
    }

    private static String getBasePlain(TextEffects fx) throws Exception {
        Field f = TextEffects.class.getDeclaredField("basePlain");
        f.setAccessible(true);
        Object out = Modifier.isStatic(f.getModifiers()) ? f.get(null) : f.get(fx);
        return (String) out;
    }
    /* ------------------------- strobeDirect ------------------------ */

    @ParameterizedTest(name = "originIndex(n={0}, origin={1}) -> {2}")
    @MethodSource("originCases")
    void originIndex_switch_is_covered(int n, String originName, int expected) throws Exception {
        Object origin = java.util.Arrays.stream(originEnum.getEnumConstants())
                .map(e -> (Enum<?>) e)
                .filter(e -> e.name().equals(originName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown origin: " + originName));

        int got = (Integer) originIndex.invoke(null, n, origin);
        assertEquals(expected, got);
    }

    @Test
    void originIndex_throws_on_null_origin() {
        int n = 5;
        // origin == null should throw NPE
        assertThrows(InvocationTargetException.class, () -> originIndex.invoke(null, n, null));
    }

    @ParameterizedTest(name = "refreshBases runs when basePlain=\"{0}\"")
    @NullAndEmptySource
    void strobeDirect_refreshBases_populates_basePlain(String initialBasePlain) throws Exception {
        // Arrange: label with seed text, fx with basePlain forced to null/empty
        var lbl = newLabel("SeedText");
        var fx = new TextEffects();

        Field fBasePlain = TextEffects.class.getDeclaredField("basePlain");
        fBasePlain.setAccessible(true);
        fBasePlain.set(fx, initialBasePlain); // null or ""

        // Capture schedule so we don't actually tick; make post() immediate just in case
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
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                    .thenThrow(new AssertionError("unexpected schedule(delay) overload"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                    .thenThrow(new AssertionError("unexpected schedule(repeat) overload"));
            timers.when(() -> Timer.post(any(Timer.Task.class)))
                    .thenAnswer(inv -> {
                        ((Timer.Task) inv.getArgument(0)).run();
                        return null;
                    });

            // Act: call strobeDirect (should hit the refreshBases branch before scheduling)
            fx.strobeDirect(lbl, Color.RED, Color.WHITE, /*hz*/ 2f, /*duration*/ 0.1f);

            // Assert: basePlain was populated from the label
            assertEquals("SeedText", fBasePlain.get(fx));

            // (Optional sanity) a task was scheduled; we won't run it here
            assertNotNull(taskRef.get(), "strobeDirect should schedule a task");
            assertTrue(intervalRef.get() > 0f);
        }
    }

    @ParameterizedTest(name = "strobe interval 1/max(0.1,hz)/2 for hz={0}")
    @CsvSource({
            "0.0, 5.0",        // clamped to 0.1 -> 1/0.1/2 = 5
            "0.1, 5.0",
            "2.0, 0.25",       // 1/2 / 2 = 0.25
            "10.0, 0.05"       // 1/10 / 2 = 0.05
    })
    void strobeDirect_interval_and_completion(float hz, float expectedInterval) throws Exception {
        TextEffects fx = new TextEffects();
        Label lbl = newLabel("Hello");
        Color a = new Color(1, 0, 0, 1);  // red
        Color b = new Color(1, 1, 1, 1);  // white

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
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                    .thenThrow(new AssertionError("unexpected schedule(delay) overload"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                    .thenThrow(new AssertionError("unexpected schedule(repeat) overload"));
            // If your cancel() posts cleanups, execute them immediately:
            timers.when(() -> Timer.post(any(Timer.Task.class)))
                    .thenAnswer(inv -> {
                        ((Timer.Task) inv.getArgument(0)).run();
                        return null;
                    });

            // duration chosen so we actually tick a few times:
            float duration = expectedInterval * 5.1f; // ~5 ticks
            fx.strobeDirect(lbl, a, b, hz, duration);

            assertNotNull(taskRef.get());
            assertEquals(expectedInterval, intervalRef.get(), 1e-6f);

            // Advance ticks: a,b,a,b,... and ends on b with cancel()
            Label.LabelStyle style = lbl.getStyle();
            // Tick 0 -> a
            taskRef.get().run();
            assertEquals(a, style.fontColor);
            // Tick 1 -> b
            taskRef.get().run();
            assertEquals(b, style.fontColor);

            // Drive until cancel; after final run, it should be b
            int guard = 0;
            Field fTask = TextEffects.class.getDeclaredField("task");
            fTask.setAccessible(true);
            while (fTask.get(fx) != null && guard++ < 10000) {
                taskRef.get().run();
            }
            assertEquals(b, style.fontColor, "final color should be b");
        }
    }

    /* -------------------------- backspaceTo ------------------------ */

    @Test
    void strobeDirect_earlyReturn_when_style_is_null() throws Exception {
        TextEffects fx = new TextEffects();

        // real label gives non-null getText()
        Label real = newLabel("SeedText");
        Label lbl = spy(real);

        // force style to be null even after ensureOwnStyle()
        doReturn(null).when(lbl).getStyle();
        doNothing().when(lbl).setStyle(any());

        try (var timers = mockStatic(Timer.class)) {
            // no scheduling should occur on early return
            timers.when(() -> Timer.schedule(
                            any(Timer.Task.class),
                            anyFloat(),
                            anyFloat()))
                    .then(inv -> {
                        throw new AssertionError("No schedule() expected for style==null");
                    });
            timers.when(() -> Timer.schedule(
                            any(Timer.Task.class),
                            anyFloat()))
                    .then(inv -> {
                        throw new AssertionError("No schedule(delay) expected");
                    });
            timers.when(() -> Timer.schedule(
                            any(Timer.Task.class),
                            anyFloat(),
                            anyFloat(),
                            anyInt()))
                    .then(inv -> {
                        throw new AssertionError("No schedule(repeat) expected");
                    });
            timers.when(() -> Timer.post(
                            any(Timer.Task.class)))
                    .then(inv -> {
                        throw new AssertionError("No post() expected");
                    });

            fx.strobeDirect(lbl, com.badlogic.gdx.graphics.Color.RED, com.badlogic.gdx.graphics.Color.WHITE, 5f, 0.5f);

            // verify we didn’t call color/invalidations (these happen after style check)
            verify(lbl, never()).setColor(any());
            verify(lbl, never()).invalidateHierarchy();

            // task should remain null
            var fTask = com.csse3200.game.ui.effects.TextEffects.class.getDeclaredField("task");
            fTask.setAccessible(true);
            assertNull(fTask.get(fx));

            // basePlain should have been refreshed from the label text (since refreshBases runs first)
            var fBasePlain = com.csse3200.game.ui.effects.TextEffects.class.getDeclaredField("basePlain");
            fBasePlain.setAccessible(true);
            assertEquals("SeedText", fBasePlain.get(fx));
        }
    }

    @ParameterizedTest(name = "strobeDirect refreshes basePlain when initial=\"{0}\"")
    @NullAndEmptySource
    void strobeDirect_refreshes_basePlain_when_null_or_empty(String initialBasePlain) throws Exception {
        var fx = new TextEffects();
        var lbl = newLabel("SeedText");      // real Label => getText() non-null

        // --- reflect basePlain correctly (static vs instance) ---
        Field fBasePlain = TextEffects.class.getDeclaredField("basePlain");
        fBasePlain.setAccessible(true);
        boolean isStatic = java.lang.reflect.Modifier.isStatic(fBasePlain.getModifiers());
        // set to null/"" to trigger the guard
        if (isStatic) fBasePlain.set(null, initialBasePlain);
        else fBasePlain.set(fx, initialBasePlain);

        // Capture scheduling (don’t auto-run)
        var taskRef = new java.util.concurrent.atomic.AtomicReference<com.badlogic.gdx.utils.Timer.Task>();
        var intervalRef = new java.util.concurrent.atomic.AtomicReference<Float>();
        try (var timers = org.mockito.Mockito.mockStatic(com.badlogic.gdx.utils.Timer.class)) {
            timers.when(() -> com.badlogic.gdx.utils.Timer.schedule(
                            org.mockito.ArgumentMatchers.any(com.badlogic.gdx.utils.Timer.Task.class),
                            org.mockito.ArgumentMatchers.anyFloat(),
                            org.mockito.ArgumentMatchers.anyFloat()))
                    .thenAnswer(inv -> {
                        var t = (com.badlogic.gdx.utils.Timer.Task) inv.getArgument(0);
                        float interval = inv.getArgument(2, Float.class);
                        taskRef.set(t);
                        intervalRef.set(interval);
                        return t;
                    });

            // IMPORTANT: do NOT stub getStyle() to null; we need to pass the style check
            fx.strobeDirect(lbl, com.badlogic.gdx.graphics.Color.RED, com.badlogic.gdx.graphics.Color.WHITE, 2f, 0.1f);

            // read the same way we wrote (static vs instance)
            Object actual = isStatic ? fBasePlain.get(null) : fBasePlain.get(fx);
            org.junit.jupiter.api.Assertions.assertEquals("SeedText", actual);

            // sanity: scheduled
            org.junit.jupiter.api.Assertions.assertNotNull(taskRef.get());
            org.junit.jupiter.api.Assertions.assertTrue(intervalRef.get() > 0f);
        }
    }

    @ParameterizedTest(name = "backspaceTo early return: cps={1}, start=\"{0}\", target={2}")
    @CsvSource({
            "'',        10, 0",   // already <= tgt
            "'abc',      0, 2",   // cps<=0
            "'xy',      10, 5"    // already <= tgt
    })
    void backspaceTo_early_returns_no_schedule(String startText, float cps, int targetLen) throws Exception {
        TextEffects fx = new TextEffects();
        Label lbl = newLabel(startText);

        try (MockedStatic<Timer> timers = mockStatic(Timer.class)) {
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                    .thenThrow(new AssertionError("no schedule on early return"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                    .thenThrow(new AssertionError("no schedule on early return"));
            timers.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                    .thenThrow(new AssertionError("no schedule on early return"));
            timers.when(() -> Timer.post(any(Timer.Task.class)))
                    .thenThrow(new AssertionError("no post on early return"));
            fx.backspaceTo(lbl, targetLen, cps);

            // unchanged
            assertEquals(startText, lbl.getText().toString());
            Field fTask = TextEffects.class.getDeclaredField("task");
            fTask.setAccessible(true);
            assertNull(fTask.get(fx));
        }
    }

    @Test
    void backspaceTo_reduces_to_target_length_and_stops() throws Exception {
        TextEffects fx = new TextEffects();
        Label lbl = newLabel("ABCDEFG");

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
            timers.when(() -> Timer.post(any(Timer.Task.class)))
                    .thenAnswer(inv -> {
                        ((Timer.Task) inv.getArgument(0)).run();
                        return null;
                    });

            float cps = 20f;
            fx.backspaceTo(lbl, 3, cps);

            assertNotNull(taskRef.get());
            assertEquals(1f / cps, intervalRef.get(), 1e-6f);

            // Step until it reaches length 3 and cancels
            int guard = 0;
            Field fTask = TextEffects.class.getDeclaredField("task");
            fTask.setAccessible(true);
            while (fTask.get(fx) != null && guard++ < 10000) {
                taskRef.get().run();
            }

            assertEquals(3, lbl.getText().length(), "final length");
            assertEquals("ABC", lbl.getText().toString(), "leading characters preserved");
        }
    }

    @Test
    void ensureBasePlain_refreshes_when_missing() throws Exception {
        var fx = new TextEffects();
        var lbl = newLabel("SeedText");  // real Label ⇒ getText() non-null

        setBasePlain(fx, null);          // or "" to cover the other half


        ensureBasePlain.invoke(fx, lbl);
        assertEquals("SeedText", getBasePlain(fx));
    }

    @Test
    void ensureBasePlain_noop_when_already_set() throws Exception {
        var fx = new TextEffects();
        var lbl = newLabel("OtherText");

        setBasePlain(fx, "AlreadyThere");

        ensureBasePlain.invoke(fx, lbl);
        assertEquals("AlreadyThere", getBasePlain(fx));
    }
}
