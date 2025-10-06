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
import org.mockito.MockedStatic;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
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
        // (optional) not needed when region is provided, but harmless:
        // data.imagePaths = new String[] {"dummy.png"};

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

    @AfterEach
    void clearTimers() {
        try {
            Timer.instance().clear();
        } catch (Throwable ignored) {
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

    // ---------------------- sparkle ----------------------
//    @Test
//    void sparkle_restoresBaseAfterBurst() {
//        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
//            TextEffects fx = new TextEffects();
//            Label lbl = newLabel("SPARK");
//            fx.sparkle(lbl, 0.6f, 12f, 0.6f);
//            assertEquals("SPARK", lbl.getText().toString(), "sparkle should restore base text");
//        }
//    }

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

    // --- Minimal headless Label factory (safe style/font) ---
// --- Test-only Label that never touches GlyphLayout ---
    static class TestLabel extends Label {
        private float prefWidth;
        private float prefHeight;

        TestLabel(CharSequence text, LabelStyle style) {
            super(text, style);
        }

        protected void computePrefSize() {
            // Skip GlyphLayout: provide stable, fake pref sizes
            this.prefWidth = getText() == null ? 0f : getText().length();
            this.prefHeight = 12f;
        }
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
