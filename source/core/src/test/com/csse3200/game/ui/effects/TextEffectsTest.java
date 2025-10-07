package com.csse3200.game.ui.effects;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
 * High-coverage tests for TextEffects.
 * <p>
 * Strategy:
 * - Mock Timer.schedule/post so tasks run immediately; for repeating schedules we run a small, bounded loop.
 * - Stub Gdx.files to an in-memory Files so readRandomLine can be exercised without disk.
 * - Keep input strings tiny to avoid deep recursion when effects self-reschedule.
 */
@ExtendWith(GameExtension.class)
class TextEffectsTest {

    // -------------------- Timer immediate exec --------------------

    private static MemFiles memFiles;

    // -------------------- Gdx.files stub --------------------

    private static MockedStatic<Timer> mockTimersImmediate() {
        MockedStatic<Timer> mocked = mockStatic(Timer.class, CALLS_REAL_METHODS);

        // schedule(task, delay)
        mocked.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    t.run(); // many effects reschedule "this"; our static mock will recursively handle it
                    return t;
                });

        // schedule(task, delay, interval) -> bounded "repeating" loop
        mocked.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    // run a handful of iterations so short texts finish; avoid infinite caret blink, etc.
                    for (int i = 0; i < 32; i++) t.run();
                    return t;
                });

        // schedule(task, delay, interval, repeatCount) -> bounded loop
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
        Gdx.files = memFiles; // inject stub
    }

    // NPE-proof Label factory for headless tests.
    private static Label newLabel(String initialText) {
        // Minimal font data
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
        data.markupEnabled = false;
        data.capHeight = 10f;
        data.ascent = -8f;
        data.lineHeight = 12f;

        // Mock font + cache (lenient)
        BitmapFont font = org.mockito.Mockito.mock(BitmapFont.class, org.mockito.Mockito.withSettings());
        org.mockito.Mockito.when(font.getData()).thenReturn(data);
        org.mockito.Mockito.when(font.getCapHeight()).thenReturn(data.capHeight);
        org.mockito.Mockito.when(font.getAscent()).thenReturn(data.ascent);
        org.mockito.Mockito.when(font.getLineHeight()).thenReturn(data.lineHeight);
        org.mockito.Mockito.when(font.usesIntegerPositions()).thenReturn(true);

        com.badlogic.gdx.graphics.g2d.BitmapFontCache cache =
                org.mockito.Mockito.mock(com.badlogic.gdx.graphics.g2d.BitmapFontCache.class,
                        org.mockito.Mockito.withSettings());
        org.mockito.Mockito.when(cache.getFont()).thenReturn(font);
        org.mockito.Mockito.when(font.newFontCache()).thenReturn(cache);

        // CRUCIAL: some code paths read font.getColor(); make it non-null
        org.mockito.Mockito.when(font.getColor()).thenReturn(Color.WHITE);

        // Use convenience ctor so Label copies a non-null color during super()
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        Label lbl = new Label(initialText, style);

        // Belt & braces: ensure instance color is non-null even if style mutates later
        lbl.setColor(Color.WHITE);

        // Also ensure the retained style instance has non-null fields
        Label.LabelStyle s = lbl.getStyle();
        if (s == null || s.font == null || s.fontColor == null) {
            s = new Label.LabelStyle(s != null ? s : style);
            if (s.font == null) s.font = font;
            if (s.fontColor == null) s.fontColor = Color.WHITE.cpy();
            lbl.setStyle(s);
        }

        return lbl;
    }


    @AfterEach
    void clearTimers() {
        try {
            Timer.instance().clear();
        } catch (Throwable ignored) {
            // We don't need to check it's output
        }
    }

    // -------------------- Helpers --------------------

    @Test
    void readRandomLine_success_commentBlank_and_fallbacks() {
        // present file with one good line -> deterministic
        memFiles.put("ok.txt", """
                  # comment
                  \t  
                  hello
                """);
        assertEquals("hello", TextEffects.readRandomLine("ok.txt", "fb"));

        // comment-only -> fallback
        memFiles.put("comment_only.txt", """
                  #a
                  #b
                """);
        assertEquals("fb", TextEffects.readRandomLine("comment_only.txt", "fb"));

        // missing -> fallback
        memFiles.map.remove("missing.txt");
        assertEquals("x", TextEffects.readRandomLine("missing.txt", "x"));

        // bad read -> fallback (simulate by removing after exists check)
        memFiles.put("bad.txt", "will vanish");
        memFiles.map.remove("bad.txt");
        assertEquals("fallback", TextEffects.readRandomLine("bad.txt", "fallback"));
    }

    // -------------------- Tests --------------------

    @Test
    void typewriter_and_backspace_and_wordReveal_and_marquee() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();

            // typewriter -> full text emitted under repeated runs
            Label lbl = newLabel("");
            fx.typewriter(lbl, "abc", 60f);
            assertEquals("abc", lbl.getText().toString());

            // backspace to length 2
            fx.backspaceTo(lbl, 2, 60f);
            assertEquals("ab".length(), lbl.getText().length());

            // wordReveal keeps spacing and completes
            Label lbl2 = newLabel("");
            fx.wordReveal(lbl2, "Hi  there!", 30f);
            assertEquals("Hi  there!", lbl2.getText().toString());

            // marquee shows exactly window width
            Label lbl3 = newLabel("");
            fx.marquee(lbl3, "HELLO", 3, 20f);
            assertEquals(3, lbl3.getText().length());
        }
    }

    @Test
    void typewriterSmart_ticks_and_finishes() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();
            Label lbl = newLabel("");

            AtomicInteger ticks = new AtomicInteger(0);
            fx.typewriterSmart(lbl, "A,B.", 30f, 0.05f, 0.1f, ticks::incrementAndGet);

            assertEquals("A,B.", lbl.getText().toString());
            assertTrue(ticks.get() >= 3); // onTick ran multiple times
        }
    }

    @Test
    void crazyOrType_plain_uses_typewriter_and_crazy_runs_blocks_then_cancel() {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();

            // No CRAZY -> plain typewriter (no markup in final)
            Label p = newLabel("");
            fx.crazyOrType(p, "Hello", 60f);
            assertEquals("Hello", p.getText().toString());

            // With CRAZY -> runs block animation (text will be markup-wrapped / animated)
            Label c = newLabel("");
            String crazy = "{CRAZY style=blast origin=middle spread=2 fps=60 cycles=1 rainbow=true rshift=20}OK{/CRAZY}";
            fx.crazyOrType(c, crazy, 40f);
            // Bounded run may still be mid-animation; ensure non-empty and has some chars
            assertFalse(c.getText().isEmpty());
            // Stop whatever is running
            fx.cancel();
        }
    }


    /**
     * Minimal Files stub with in-memory "internal" contents.
     */
    static class MemFiles implements Files {
        private final Map<String, String> map = new HashMap<>();

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
        public FileHandle getFileHandle(String s, FileType fileType) {
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
