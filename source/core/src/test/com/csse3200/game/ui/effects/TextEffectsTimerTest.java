package com.csse3200.game.ui.effects;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

/**
 * Timer-focused tests covering all repeating/async effects in TextEffects.
 * We mock Timer so scheduled tasks run immediately (bounded loops for repeating).
 */
@ExtendWith(GameExtension.class)
class TextEffectsTimerTest {
    private static MemFiles memFiles;

    // --- Timer immediate exec harness (same shape as your other test) ---
    private static MockedStatic<Timer> mockTimersImmediate() {
        MockedStatic<Timer> mocked = mockStatic(Timer.class, CALLS_REAL_METHODS);

        // schedule(task, delay)
        mocked.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    t.run();
                    return t;
                });

        // schedule(task, delay, interval)
        mocked.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    for (int i = 0; i < 32; i++) t.run();
                    return t;
                });

        // schedule(task, delay, interval, repeatCount)
        mocked.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    for (int i = 0; i < 32; i++) t.run();
                    return t;
                });

        // post(task)
        mocked.when(() -> Timer.post(any(Timer.Task.class)))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    t.run();
                    return null;
                });

        return mocked;
    }

    @BeforeAll
    static void initGdx() {
        memFiles = new MemFiles();
        Gdx.files = memFiles;
    }

    /**
     * Headless-safe Label for tests (no .fnt/.png needed).
     */
    private static Label newLabel(String initialText) {
        // 1) Real font data so GlyphLayout can read fields safely
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
        data.markupEnabled = true;   // exercise markup paths
        data.capHeight = 10f;
        data.ascent = -8f;
        data.lineHeight = 12f;

        // 2) Dummy region (no Texture/GL required)
        TextureRegion region = new TextureRegion(); // empty region is fine for layout paths

        // 3) Real BitmapFont using our data + region
        BitmapFont font = new BitmapFont(data, region, true);

        // 4) Build Label with a proper, non-null fontColor
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label lbl = new Label(initialText, style);

        // 5) Neutral tint + keep markup enabled
        lbl.setColor(Color.WHITE);
        font.getData().markupEnabled = true;

        return lbl;
    }

    private static String stripLibgdxMarkup(CharSequence cs) {
        if (cs == null) return "";
        // Remove [#RRGGBB] or [#RRGGBBAA] and the closing [] tokens
        return cs.toString().replaceAll("\\[#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})]|\\[]", "");
    }

    private static Class<?> ownerClass() throws Exception {
        try {
            return Class.forName("com.csse3200.game.ui.effects.TextEffects");
        } catch (ClassNotFoundException e) {
            return Class.forName("com.csse3200.game.ui.effects.TypingInit");
        }
    }

    private static Object newOwner() throws Exception {
        Class<?> c = ownerClass();
        Constructor<?> ctor = c.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }

    static Stream<Arguments> doublePunctCases() {
        return Stream.of(
                arguments("x..y", "x."),
                arguments("x!!y", "x!"),
                arguments("x??y", "x?"),
                arguments("x.xy", "x."),
                arguments("x!xy", "x!"),
                arguments("x?xy", "x?")
        );
    }

    // ---------------------- sparkle ----------------------
    @Test
    void sparkle_restoresBaseAfterBurst() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("SPARK");

            fx.sparkle(lbl, 0.6f, 12f, 0.6f);

            // If sparkle is async and your mock triggers all scheduled runs immediately,
            // this assertion happens after the restore step.
            assertEquals("SPARK", stripLibgdxMarkup(lbl.getText()),
                    "sparkle should restore base text (ignoring markup)");
        }
    }

    @AfterEach
    void clearTimers() {
        try {
            Timer.instance().clear();
        } catch (Throwable t) {
            throw new AssertionError("Timer cleanup failed", t);
        }
    }

    // ---------------------- readRandomLine ----------------------
    @Test
    void readRandomLine_variants() {
        memFiles.put("ok.txt", "#c\n \nhello");
        assertEquals("hello", TextEffects.readRandomLine("ok.txt", "fb"));

        memFiles.put("comment_only.txt", "#a\n#b");
        assertEquals("fb", TextEffects.readRandomLine("comment_only.txt", "fb"));

        memFiles.map.remove("missing.txt");
        assertEquals("x", TextEffects.readRandomLine("missing.txt", "x"));
    }

    // ---------------------- typewriter / backspace / wordReveal / marquee ----------------------
    @Test
    void typewriter_backspace_wordReveal_marquee() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();

            Label lbl = newLabel("");
            fx.typewriter(lbl, "abc", 60f);
            assertEquals("abc", lbl.getText().toString());

            fx.backspaceTo(lbl, 2, 60f);
            assertEquals(2, lbl.getText().length());

            Label lbl2 = newLabel("");
            fx.wordReveal(lbl2, "Hi  there!", 30f);
            assertEquals("Hi  there!", lbl2.getText().toString());

            Label lbl3 = newLabel("");
            fx.marquee(lbl3, "HELLO", 3, 20f);
            assertEquals(3, lbl3.getText().length());
        }
    }

    // ---------------------- typewriterSmart ----------------------
    @Test
    void typewriterSmart_ticksAndFinishes() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("");
            AtomicInteger ticks = new AtomicInteger(0);
            fx.typewriterSmart(lbl, "A,B.", 30f, 0.05f, 0.1f, ticks::incrementAndGet);
            assertEquals("A,B.", lbl.getText().toString());
            assertTrue(ticks.get() >= 3);
        }
    }

    // ---------------------- glitchReveal ----------------------
    @Test
    void glitchReveal_completesToTarget() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("");
            fx.glitchReveal(lbl, "FINAL", 0.5f);
            assertEquals("FINAL", lbl.getText().toString());
        }
    }

    // ---------------------- pulseRainbow ----------------------
    @Test
    void pulseRainbow_appliesColorMarkup() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("BASE");
            fx.pulseRainbow(lbl, 0.5f, 0f);
            String t = lbl.getText().toString();
            assertTrue(t.contains("[#") && t.endsWith("[]"), "should wrap with color markup");
        }
    }

    // ---------------------- flashHighlight ----------------------
    @Test
    void flashHighlight_blinksAndRestores() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("alpha beta gamma");
            fx.flashHighlight(lbl, "beta", 3, 5f, "ffcc00");
            assertEquals("alpha beta gamma", lbl.getText().toString(), "should restore original string");
        }
    }

    // ---------------------- strobe (markup) ----------------------
    @Test
    void strobe_wrapsThenRestoresBase() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("VICTORY");
            // call strobe with # and 3-digit to exercise sanitizeHex path indirectly
            fx.strobe(lbl, "#0f0", "#fff", 10f, 0.25f);
            assertEquals("VICTORY", lbl.getText().toString(), "strobe should restore base text");
        }
    }

    // ---------------------- pulseBetween ----------------------
    @Test
    void pulseBetween_interpolatesAndLeavesMarkup() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("BASE");
            fx.pulseBetween(lbl, "#ff0000", "#00ff00", 0.8f);
            String s = lbl.getText().toString();
            assertTrue(s.contains("[#") && s.endsWith("[]"), "should contain color tag each tick");
        }
    }

    // ---------------------- sweepRainbow ----------------------
    @Test
    void sweepRainbow_generatesPerCharColors() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("COLOR");
            fx.sweepRainbow(lbl, 0.6f, 18f, 0.12f);
            String s = lbl.getText().toString();
            assertTrue(s.contains("[#"), "rainbow sweep should add color tags");
            assertTrue(s.endsWith("[]"), "should close color tags");
        }
    }

    // ---------------------- strobeDirect (style color flip) ----------------------
    @Test
    void strobeDirect_togglesFontColor_andLeavesWhite() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("VICTORY");
            // ensure private style clone happens
            TextEffects.ensureOwnStyle(lbl);
            Color green = Color.valueOf("00ff00");
            fx.strobeDirect(lbl, green, Color.WHITE, 12f, 0.3f);
            // After bounded iterations, we leave it on 'b' (WHITE)
            assertEquals(Color.WHITE, lbl.getStyle().fontColor);
        }
    }

    // ---------------------- crazyOrType ----------------------
    @Test
    void crazyOrType_runsPlainOrCrazyAndCanCancel() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();

            Label p = newLabel("");
            fx.crazyOrType(p, "Hello", 50f);
            assertEquals("Hello", p.getText().toString());

            Label c = newLabel("");
            String crazy = "{CRAZY style=blast origin=middle spread=2 fps=60 cycles=1 rainbow=true rshift=20}OK{/CRAZY}";
            fx.crazyOrType(c, crazy, 40f);
            assertFalse(c.getText().isEmpty());
            fx.cancel(); // should not throw
        }
    }

    @Test
    void earlyReturn_whenDensityZero() throws Exception {
        // Resolve owner class (TextEffects or TypingInit) inline.
        Class<?> clazz;
        try {
            clazz = Class.forName("com.csse3200.game.ui.effects.TextEffects");
        } catch (ClassNotFoundException e) {
            clazz = Class.forName("com.csse3200.game.ui.effects.TypingInit");
        }

        Object inst = clazz.getDeclaredConstructor().newInstance();

        // basePlain non-empty so refreshBases() path is skipped.
        Field basePlain = clazz.getDeclaredField("basePlain");
        basePlain.setAccessible(true);
        basePlain.set(inst, "Hello");

        // Ensure task is null before call.
        Field taskField = clazz.getDeclaredField("task");
        taskField.setAccessible(true);
        taskField.set(inst, null);

        Label label = newLabel("X");

        Method sparkle = clazz.getDeclaredMethod("sparkle", Label.class, float.class, float.class, float.class);
        sparkle.setAccessible(true);
        sparkle.invoke(inst, label, 0f, 5f, 0.2f); // density <= 0 => early return

        assertEquals("X", label.getText().toString());
        assertNull(taskField.get(inst));
    }

    @Test
    void animates_then_resets_and_cancels() throws Exception {
        Class<?> clazz;
        try {
            clazz = Class.forName("com.csse3200.game.ui.effects.TextEffects");
        } catch (ClassNotFoundException e) {
            clazz = Class.forName("com.csse3200.game.ui.effects.TypingInit");
        }

        Object inst = clazz.getDeclaredConstructor().newInstance();

        Field basePlain = clazz.getDeclaredField("basePlain");
        basePlain.setAccessible(true);
        basePlain.set(inst, "Spark");

        Label label = newLabel("ignored");

        Method sparkle = clazz.getDeclaredMethod("sparkle", Label.class, float.class, float.class, float.class);
        sparkle.setAccessible(true);
        sparkle.invoke(inst, label, 1f, 5f, 0.1f); // ~6 frames at 60 FPS

        Field taskField = clazz.getDeclaredField("task");
        taskField.setAccessible(true);
        Timer.Task task = (Timer.Task) taskField.get(inst);
        assertNotNull(task);

        // First frame should differ from basePlain.
        task.run();
        assertNotEquals("Spark", label.getText().toString());

        int frames = Math.max(1, Math.round(0.1f * 60));
        for (int i = 1; i < frames; i++) task.run();

        // After last frame it should restore basePlain and cancel the task.
        assertEquals("Spark", label.getText().toString());
        assertFalse(task.isScheduled());
    }

    @Test
    void staticPhase_whenHzZero_framesIdentical() throws Exception {
        Class<?> clazz;
        try {
            clazz = Class.forName("com.csse3200.game.ui.effects.TextEffects");
        } catch (ClassNotFoundException e) {
            clazz = Class.forName("com.csse3200.game.ui.effects.TypingInit");
        }

        Object inst = clazz.getDeclaredConstructor().newInstance();

        Field basePlain = clazz.getDeclaredField("basePlain");
        basePlain.setAccessible(true);
        basePlain.set(inst, "Static");

        Label label = newLabel("ignored");

        Method sparkle = clazz.getDeclaredMethod("sparkle", Label.class, float.class, float.class, float.class);
        sparkle.setAccessible(true);
        sparkle.invoke(inst, label, 1f, 0f, 0.2f); // hz=0 => static phase

        Field taskField = clazz.getDeclaredField("task");
        taskField.setAccessible(true);
        Timer.Task task = (Timer.Task) taskField.get(inst);
        assertNotNull(task);

        task.run();
        String a = label.getText().toString();
        task.run();
        String b = label.getText().toString();
        assertEquals(a, b);
    }

    @Test
    void restarting_cancels_previous_task() throws Exception {
        Class<?> clazz;
        try {
            clazz = Class.forName("com.csse3200.game.ui.effects.TextEffects");
        } catch (ClassNotFoundException e) {
            clazz = Class.forName("com.csse3200.game.ui.effects.TypingInit");
        }

        Object inst = clazz.getDeclaredConstructor().newInstance();

        Field basePlain = clazz.getDeclaredField("basePlain");
        basePlain.setAccessible(true);
        basePlain.set(inst, "Again");

        Label label = newLabel("ignored");

        Method sparkle = clazz.getDeclaredMethod("sparkle", Label.class, float.class, float.class, float.class);
        sparkle.setAccessible(true);

        // Start a long animation
        sparkle.invoke(inst, label, 1f, 2f, 1.0f);
        Field taskField = clazz.getDeclaredField("task");
        taskField.setAccessible(true);
        Timer.Task t1 = (Timer.Task) taskField.get(inst);
        assertTrue(t1.isScheduled());

        // Re-invoke with density==0 -> early return, but cancel() must run first.
        sparkle.invoke(inst, label, 0f, 2f, 0.1f);
        assertFalse(t1.isScheduled());
        assertNull(taskField.get(inst));
    }

    @Test
    void earlyReturn_when_prepareTyping_returns_null() throws Exception {
        Object inst = newOwner();
        Class<?> c = inst.getClass();

        Field taskField = c.getDeclaredField("task");
        taskField.setAccessible(true);
        taskField.set(inst, null);

        Method m = c.getDeclaredMethod(
                "typewriterSmart", Label.class, String.class, float.class, float.class, float.class, Runnable.class);
        m.setAccessible(true);

        Label label = newLabel("keep");

        // null fullText → prepareTyping(...) returns null
        m.invoke(inst, label, null, 20f, 0.03f, 0.09f, (Runnable) null);

        assertNull(taskField.get(inst), "No Timer task should be scheduled on early return");
        assertEquals("", label.getText().toString(),
                "Current implementation clears the label when init fails");
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" ⇒ after 2 runs = \"{1}\"")
    @MethodSource("doublePunctCases")
    void doublePunctuation_scaling_branch_executes(String input, String expectedAfterSecondRun) throws Exception {
        Object inst = newOwner();
        Class<?> c = inst.getClass();

        Method m = c.getDeclaredMethod("typewriterSmart",
                Label.class, String.class, float.class, float.class, float.class, Runnable.class);
        m.setAccessible(true);

        Field taskField = c.getDeclaredField("task");
        taskField.setAccessible(true);

        Label label = newLabel("");
        m.invoke(inst, label, input, 100f, 0.01f, 0.5f, (Runnable) () -> {
        });

        Timer.Task task = (Timer.Task) taskField.get(inst);
        assertNotNull(task, "Task should be scheduled");

        task.run(); // 'x'
        assertEquals("x", label.getText().toString());

        task.run(); // first punctuation; next char is same punctuation -> branch executes
        assertEquals(expectedAfterSecondRun, label.getText().toString());
    }

    @Test
    void progresses_chars_calls_onTick_and_finishes() throws Exception {
        Object inst = newOwner();
        Class<?> c = inst.getClass();

        Field taskField = c.getDeclaredField("task");
        taskField.setAccessible(true);

        Method m = c.getDeclaredMethod("typewriterSmart", Label.class, String.class, float.class, float.class, float.class, Runnable.class);
        m.setAccessible(true);

        Label label = newLabel("");
        AtomicInteger ticks = new AtomicInteger();

        // Simple two-char text so we can hit the "finished" branch on the third run
        m.invoke(inst, label, "ab", 60f, 0.01f, 0.02f, (Runnable) ticks::incrementAndGet);
        Timer.Task task = (Timer.Task) taskField.get(inst);
        assertNotNull(task, "Task should be scheduled immediately at t=0");

        // 1st run → 'a'
        task.run();
        assertEquals("a", label.getText().toString());
        assertEquals(1, ticks.get());

        // 2nd run → 'b'
        task.run();
        assertEquals("ab", label.getText().toString());
        assertEquals(2, ticks.get());

        // 3rd run → i[0] >= len → cancel & keep final text
        task.run();
        assertEquals("ab", label.getText().toString());
        assertFalse(task.isScheduled(), "Task should cancel itself after finishing");
    }

    @Test
    void shortPause_branch_triggered_by_comma() throws Exception {
        Object inst = newOwner();
        Class<?> c = inst.getClass();

        Field taskField = c.getDeclaredField("task");
        taskField.setAccessible(true);

        Method m = c.getDeclaredMethod("typewriterSmart", Label.class, String.class, float.class, float.class, float.class, Runnable.class);
        m.setAccessible(true);

        Label label = newLabel("");

        // text with a comma to trigger shortPause branch
        m.invoke(inst, label, "a,a", 50f, /*shortPause*/0.05f, /*longPause*/0.2f, null);
        Timer.Task task = (Timer.Task) taskField.get(inst);
        assertNotNull(task);

        task.run(); // 'a'
        assertEquals("a", label.getText().toString());

        task.run(); // ',' → executes case ',', ';', ':'
        assertEquals("a,", label.getText().toString());
        assertTrue(task.isScheduled(), "Rescheduled after shortPause calculation");
    }

    @Test
    void longPause_and_doublePunctuation_scaling_for_ellipsisLike_case() throws Exception {
        Object inst = newOwner();
        Class<?> c = inst.getClass();

        Field taskField = c.getDeclaredField("task");
        taskField.setAccessible(true);

        Method m = c.getDeclaredMethod("typewriterSmart", Label.class, String.class, float.class, float.class, float.class, Runnable.class);
        m.setAccessible(true);

        Label label = newLabel("");

        // 'a..b' will hit '.' with next '.' → extra *= 0.6f branch
        m.invoke(inst, label, "a..b", 80f, 0.03f, 0.5f, null);
        Timer.Task task = (Timer.Task) taskField.get(inst);
        assertNotNull(task);

        task.run(); // 'a'
        assertEquals("a", label.getText().toString());

        task.run(); // '.' with next '.' → executes longPause, then scaling (extra *= 0.6)
        assertEquals("a.", label.getText().toString());

        // Not asserting the actual delay time; executing this branch is enough for coverage.
        assertTrue(task.isScheduled());
    }

    @Test
    void covers_refreshBases_branch_then_animates() throws Exception {
        // basePlain is null so (basePlain == null || isEmpty) is true
        Class<?> clazz = ownerClass();
        Constructor<?> ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object inst = ctor.newInstance();

        Field basePlain = clazz.getDeclaredField("basePlain");
        basePlain.setAccessible(true);
        basePlain.set(inst, null);                          // <- triggers refreshBases path

        Label label = newLabel("A[B");              // include '[' to hit the "[[" escape branch
        Method sweep = clazz.getDeclaredMethod("sweepRainbow", Label.class, float.class, float.class, float.class);
        sweep.setAccessible(true);

        // invoke and manually tick one frame
        sweep.invoke(inst, label, /*bandHz*/1f, /*perCharShiftDeg*/10f, /*travelHz*/4f);

        Field taskField = clazz.getDeclaredField("task");
        taskField.setAccessible(true);
        Timer.Task task = (Timer.Task) taskField.get(inst);
        assertNotNull(task, "Task should be scheduled after refreshBases fills basePlain");

        task.run(); // drive one frame

        String text = label.getText().toString();
        assertTrue(text.startsWith("[#"), "should prepend hex colour tags");
        assertTrue(text.contains("[["), "‘[’ must be escaped as ‘[[’");
        assertTrue(text.endsWith("[]"), "LibGDX markup close tag should be present");
    }

    @Test
    void covers_early_return_when_n_is_zero() throws Exception {
        // basePlain empty, label empty -> n == 0 triggers early return
        Class<?> clazz = ownerClass();
        Constructor<?> ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object inst = ctor.newInstance();

        Field basePlain = clazz.getDeclaredField("basePlain");
        basePlain.setAccessible(true);
        basePlain.set(inst, "");                            // empty triggers refreshBases, but label is also empty

        Label label = newLabel("");

        Field taskField = clazz.getDeclaredField("task");
        taskField.setAccessible(true);
        taskField.set(inst, null);

        Method sweep = clazz.getDeclaredMethod("sweepRainbow", Label.class, float.class, float.class, float.class);
        sweep.setAccessible(true);

        // Act
        sweep.invoke(inst, label, 2f, 15f, 3f);

        // returned before scheduling
        assertNull(taskField.get(inst), "No task should be scheduled when n==0");
        assertEquals("", label.getText().toString(), "Label text must remain unchanged on early return");
    }

    // ---------------------- Gdx.files stub ----------------------
    static class MemFiles implements Files {
        final Map<String, String> map = new HashMap<>();

        void put(String path, String content) {
            map.put(path, content);
        }

        private FileHandle fh(String path) {
            String content = map.get(path);
            return new FileHandle() {
                @Override
                public String readString() {
                    if (content == null) throw new RuntimeException("no file");
                    return content;
                }

                @Override
                public String readString(String charset) {
                    return new String(readBytes(), StandardCharsets.UTF_8);
                }

                @Override
                public boolean exists() {
                    return content != null;
                }

                @Override
                public byte[] readBytes() {
                    if (content == null) throw new RuntimeException("no file");
                    return content.getBytes(StandardCharsets.UTF_8);
                }
            };
        }

        @Override
        public FileHandle getFileHandle(String s, FileType type) {
            return fh(s);
        }

        @Override
        public FileHandle classpath(String s) {
            return fh(s);
        }

        @Override
        public FileHandle internal(String s) {
            return fh(s);
        }

        @Override
        public FileHandle external(String s) {
            return fh(s);
        }

        @Override
        public FileHandle absolute(String s) {
            return fh(s);
        }

        @Override
        public FileHandle local(String s) {
            return fh(s);
        }

        @Override
        public String getExternalStoragePath() {
            return "/";
        }

        @Override
        public boolean isExternalStorageAvailable() {
            return true;
        }

        @Override
        public String getLocalStoragePath() {
            return "/";
        }

        @Override
        public boolean isLocalStorageAvailable() {
            return true;
        }
    }
}
