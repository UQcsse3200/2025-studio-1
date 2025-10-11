package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(GameExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TextEffectsTestBase {

    // ---------- Reflected members used across many tests ----------
    protected static Method initBlastAt;
    protected static Method appendSparkleChar;
    protected static Method renderPieces;
    protected static Class<?> piece;
    protected static Method piecePlain;
    protected static Method pieceCrazy;
    protected static Method startCrazyMulti;
    protected static Class<?> crazyOpts;
    protected static Method processCrazyChunk;
    protected static Method initGlitch;
    protected static Method lockCount;
    protected static Method lockProgress;
    protected static Field glitchChars;
    protected static Method visDefaults;
    protected static Method maxFPS;
    protected static Method edge;
    protected static Method glyph;
    protected static Constructor<?> baCtor;
    protected static Class<?> ba;
    protected static Class<?> baVisual;
    protected static Constructor<?> baVisCtor;
    protected static Method parseOpts;
    protected static Method buildBlock;
    protected static Method buildBlocks;
    protected static Class<?> originEnum;
    protected static Method originIndex;
    protected static Method ensureBasePlain;
    protected static Method scrambleUnlocked;
    protected static Method advanceAll;
    protected MemFiles memFiles;

    // ---------- Common helpers ----------
    protected static MockedStatic<Timer> mockTimersImmediate() {
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

    // NPE-proof Label factory for headless tests.
    protected static Label newLabel(String text) {
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
        data.markupEnabled = false;
        data.capHeight = 10f;
        data.ascent = -8f;
        data.lineHeight = 12f;

        BitmapFont font = org.mockito.Mockito.mock(BitmapFont.class, org.mockito.Mockito.withSettings());
        org.mockito.Mockito.when(font.getData()).thenReturn(data);
        org.mockito.Mockito.when(font.getCapHeight()).thenReturn(data.capHeight);
        org.mockito.Mockito.when(font.getAscent()).thenReturn(data.ascent);
        org.mockito.Mockito.when(font.getLineHeight()).thenReturn(data.lineHeight);
        org.mockito.Mockito.when(font.usesIntegerPositions()).thenReturn(true);
        org.mockito.Mockito.when(font.getColor()).thenReturn(Color.WHITE);

        com.badlogic.gdx.graphics.g2d.BitmapFontCache cache =
                org.mockito.Mockito.mock(com.badlogic.gdx.graphics.g2d.BitmapFontCache.class, org.mockito.Mockito.withSettings());
        org.mockito.Mockito.when(cache.getFont()).thenReturn(font);
        org.mockito.Mockito.when(font.newFontCache()).thenReturn(cache);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label lbl = new Label("", style);
        lbl.setText(text);
        lbl.setColor(Color.WHITE);

        Label.LabelStyle s = lbl.getStyle();
        if (s == null || s.font == null || s.fontColor == null) {
            s = new Label.LabelStyle(s != null ? s : style);
            if (s.font == null) s.font = font;
            if (s.fontColor == null) s.fontColor = Color.WHITE.cpy();
            lbl.setStyle(s);
        }
        return lbl;
    }

    protected static Object get(Object obj, String field) throws Exception {
        Field f = ba.getDeclaredField(field);
        f.setAccessible(true);
        return f.get(obj);
    }

    protected static int geti(Object obj, String field) throws Exception {
        return (Integer) get(obj, field);
    }

    protected static String gets(Object obj, String field) throws Exception {
        return (String) get(obj, field);
    }

    protected static boolean getb(Object obj, String field) throws Exception {
        return (Boolean) get(obj, field);
    }

    protected static Object PLAIN(String text) throws Exception {
        return piecePlain.invoke(null, text);
    }

    protected static Object CRAZY(String text) throws Exception {
        return pieceCrazy.invoke(null, text, null);
    }

    protected static Object newbaWithString(String s) throws Exception {
        int n = s.length();
        int[] delays = new int[Math.max(1, n)];
        int[] remaining = new int[Math.max(1, n)];
        char[] target = s.toCharArray();
        char[] curr = s.toCharArray();
        Object vis = baVisCtor.newInstance(false, 0.6f, 18f, "ffffff", "ffe066");
        return baCtor.newInstance(60, delays, remaining, target, curr, vis, null);
    }

    // Quick guard helper for “task should remain null”
    protected static void assertNoTaskScheduled(TextEffects fx) throws Exception {
        Field fTask = TextEffects.class.getDeclaredField("task");
        fTask.setAccessible(true);
        assertNull(fTask.get(fx), "task should remain null");
    }

    @BeforeAll
    void reflect() throws Exception {
        Class<?> te = Class.forName("com.csse3200.game.ui.effects.TextEffects");
        ba = Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim");
        Class<?> blast = Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim$BlastExtras");
        Class<?> vis = Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim$Visual");

        processCrazyChunk = te.getDeclaredMethod(
                "processCrazyChunk", String.class, int.class, java.util.List.class
        );
        processCrazyChunk.setAccessible(true);

        try {
            parseOpts = te.getDeclaredMethod("parseOpts", String.class);
        } catch (NoSuchMethodException e) {
            parseOpts = te.getDeclaredMethod("dparseOpts", String.class);
        }

        initBlastAt = te.getDeclaredMethod(
                "initBlastAt",
                boolean.class, int[].class, int[].class, int[].class,
                int.class, int.class, int.class
        );
        initBlastAt.setAccessible(true);
        piece = Class.forName("com.csse3200.game.ui.effects.TextEffects$Piece");
        piecePlain = piece.getDeclaredMethod("plain", String.class);
        piecePlain.setAccessible(true);
        crazyOpts = Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts");
        pieceCrazy = piece.getDeclaredMethod("crazy", String.class, crazyOpts);
        pieceCrazy.setAccessible(true);
        startCrazyMulti = te.getDeclaredMethod("startCrazyRevealMulti",
                com.badlogic.gdx.scenes.scene2d.ui.Label.class, java.util.List.class);
        startCrazyMulti.setAccessible(true);
        renderPieces = te.getDeclaredMethod("renderPieces", java.util.List.class, java.util.List.class);
        renderPieces.setAccessible(true);
        initGlitch = te.getDeclaredMethod("initGlitchState", char[].class, char[].class, boolean[].class);
        initGlitch.setAccessible(true);
        lockCount = te.getDeclaredMethod("lockCountForFrame", int.class, int.class, int.class);
        lockCount.setAccessible(true);
        lockProgress = te.getDeclaredMethod("lockProgress", boolean[].class, char[].class, char[].class, int.class);
        lockProgress.setAccessible(true);
        glitchChars = te.getDeclaredField("GLITCH_CHARS");
        glitchChars.setAccessible(true);
        visDefaults = vis.getDeclaredMethod("defaults");
        visDefaults.setAccessible(true);
        maxFPS = te.getDeclaredMethod("maxFps", java.util.List.class);
        maxFPS.setAccessible(true);
        edge = te.getDeclaredMethod("edgeBoostCycles", boolean.class, int.class, int.class, int.class, float.class, char.class, float.class);
        edge.setAccessible(true);
        glyph = te.getDeclaredMethod("glyphSpan", char.class);
        glyph.setAccessible(true);

        appendSparkleChar = te.getDeclaredMethod(
                "appendSparkleChar", StringBuilder.class, char.class, boolean.class, float.class);
        appendSparkleChar.setAccessible(true);

        baCtor = ba.getDeclaredConstructor(int.class, int[].class, int[].class, char[].class, char[].class, vis, blast);
        baCtor.setAccessible(true);
        baVisual = vis;
        baVisCtor = vis.getDeclaredConstructor(boolean.class, float.class, float.class, String.class, String.class);
        baVisCtor.setAccessible(true);

        parseOpts.setAccessible(true);
        buildBlock = te.getDeclaredMethod("buildBlock", String.class, crazyOpts);
        buildBlock.setAccessible(true);
        buildBlocks = te.getDeclaredMethod("buildBlocks", java.util.List.class);
        buildBlocks.setAccessible(true);

        originEnum = Arrays.stream(crazyOpts.getDeclaredClasses())
                .filter(Class::isEnum)
                .filter(c -> c.getSimpleName().equals("Origin"))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("CrazyOpts.Origin enum not found"));
        originIndex = te.getDeclaredMethod("originIndex", int.class, originEnum);
        originIndex.setAccessible(true);

        ensureBasePlain = te.getDeclaredMethod("ensureBasePlain",
                com.badlogic.gdx.scenes.scene2d.ui.Label.class);
        ensureBasePlain.setAccessible(true);

        scrambleUnlocked = te.getDeclaredMethod(
                "scrambleUnlocked", boolean[].class, char[].class, char[].class);
        scrambleUnlocked.setAccessible(true);

        advanceAll = te.getDeclaredMethod("advanceAll", java.util.List.class, int.class);
        advanceAll.setAccessible(true);

        ba = Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim");
    }

    @BeforeAll
    void initGdx() {
        memFiles = new MemFiles();
        Gdx.files = memFiles;
    }

    @AfterEach
    void clearBetween() {
        try {
            Timer.instance().clear();
        } catch (Throwable ignored) {
        }
        memFiles.clear();
    }

    @AfterAll
    void teardown() {
        memFiles.clear();
    }

    // ---------- In-memory Files stub ----------
    protected static class MemFiles implements Files {
        private final Map<String, String> map = new HashMap<>();

        void put(String path, String content) {
            map.put(path, content);
        }

        void remove(String path) {
            map.remove(path);
        }

        void clear() {
            map.clear();
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
