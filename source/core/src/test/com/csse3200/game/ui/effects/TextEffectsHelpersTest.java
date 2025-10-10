package com.csse3200.game.ui.effects;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Helper-only coverage for TextEffects (no timers/animations).
 * Covers: readRandomLine, enableMarkup/ensureOwnStyle, stripMarkup, sanitizeHex,
 * hsvToRgb, toHex6, isUpper, isLetter, isDigit, isPunctGlobal, parseOpts,
 * parseCrazyPieces, joinPiecesStatic.
 */
@ExtendWith(GameExtension.class)
class TextEffectsHelpersTest {

    @BeforeAll
    static void setupFiles() {
        Gdx.files = new MemFiles(); // inject headless stub
    }

    // -------------------------- Reflection helpers --------------------------
    private static Method priv(String name, Class<?>... params) throws Exception {
        Method m = TextEffects.class.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    private static Object get(Object o, String field) throws Exception {
        Field f = o.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(o);
    }

    // -------------------------- readRandomLine --------------------------

    private static Class<?> TE() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects");
    }

    private static Method TE_m(String name, Class<?>... ptypes) throws Exception {
        Method m = TE().getDeclaredMethod(name, ptypes);
        m.setAccessible(true);
        return m;
    }

    private static Class<?> ORIGIN() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts$Origin");
    }

    private static Object optsFrom(String spec) throws Exception {
        Class<?> te = Class.forName("com.csse3200.game.ui.effects.TextEffects");
        java.lang.reflect.Method pm;
        try {
            pm = te.getDeclaredMethod("parseOpts", String.class);  // current name
        } catch (NoSuchMethodException e) {
            pm = te.getDeclaredMethod("dparseOpts", String.class); // fallback if older name
        }
        pm.setAccessible(true);
        return pm.invoke(null, spec == null ? "" : spec);
    }

    private static Class<?> CRAZY_OPTS() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts");
    }


    @Test
    void readRandomLine_exception_in_handle_returns_fallback() {
        // Arrange
        Files files = mock(Files.class, CALLS_REAL_METHODS);
        FileHandle fh = mock(FileHandle.class);
        Gdx.files = files;

        when(files.internal("boom.txt")).thenReturn(fh);

        // Shotgun-stub typical read paths
        when(fh.readString()).thenThrow(new RuntimeException("kaboom"));
        when(fh.readString(any())).thenThrow(new RuntimeException("kaboom"));
        when(fh.read()).thenThrow(new RuntimeException("kaboom"));
        when(fh.reader()).thenThrow(new RuntimeException("kaboom"));

        // Act + Assert
        assertEquals("FALLBACK", TextEffects.readRandomLine("boom.txt", "FALLBACK"));
    }

    // -------------------------- enableMarkup / ensureOwnStyle --------------------------
    @Test
    void enableMarkup_sets_fontData_flag_and_ensureOwnStyle_clones_style() {
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
        data.markupEnabled = false;
        BitmapFont font = mock(BitmapFont.class, withSettings());
        when(font.getData()).thenReturn(data);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = Color.WHITE;

        Label lbl = mock(Label.class, withSettings());

        // Style store for getStyle/setStyle
        final Label.LabelStyle[] store = {style};
        when(lbl.getStyle()).thenAnswer(i -> store[0]);
        doAnswer(i -> {
            store[0] = i.getArgument(0);
            return null;
        })
                .when(lbl).setStyle(any(Label.LabelStyle.class));

        // enableMarkup
        assertFalse(font.getData().markupEnabled);
        TextEffects.enableMarkup(lbl);
        assertTrue(font.getData().markupEnabled);

        // ensureOwnStyle
        Label.LabelStyle before = store[0];
        TextEffects.ensureOwnStyle(lbl);
        Label.LabelStyle after = store[0];
        assertNotSame(before, after);
        assertSame(before.font, after.font);
        assertEquals(before.fontColor, after.fontColor);
    }

    // -------------------------- stripMarkup (private) --------------------------
    @Test
    void stripMarkup_removes_color_tags_and_preserves_literal_brackets() throws Exception {
        Method strip = priv("stripMarkup", String.class);
        assertEquals("", strip.invoke(null, ""));
        assertEquals("plain", strip.invoke(null, "plain"));
        assertEquals("[abc]", strip.invoke(null, "[[abc]"));
        assertEquals("hello world", strip.invoke(null, "[#ff0]hello[] [#00ff00]world[]"));
    }

    // -------------------------- sanitizeHex (private) --------------------------
    @Test
    void sanitizeHex_validates_and_normalizes_hex() throws Exception {
        Method sanitize = priv("sanitizeHex", String.class, String.class);
        assertEquals("ffffff", sanitize.invoke(null, null, "ffffff"));          // null -> default
        assertEquals("00ff00", sanitize.invoke(null, "#00FF00", "ffffff"));     // strip '#', lowercase
        assertEquals("aabbcc", sanitize.invoke(null, "abc", "ffffff"));         // 3-digit expand
        assertEquals("ffffff", sanitize.invoke(null, "zzzzzz", "ffffff"));      // invalid -> default
    }

    // -------------------------- hsvToRgb + toHex6 (private) --------------------------
    @Test
    void hsvToRgb_covers_all_switch_paths_including_case3_and_default() throws Exception {
        Method hsvToRgb = priv("hsvToRgb", float.class, float.class, float.class);
        Method toHex6 = priv("toHex6", int.class);

        // case 0: 0° -> red
        assertEquals("ff0000", toHex6.invoke(null, hsvToRgb.invoke(null, 0f, 1f, 1f)));
        // case 1: 60° -> yellow-ish (#ffff00 expected)
        assertEquals("ffff00", toHex6.invoke(null, hsvToRgb.invoke(null, 60f, 1f, 1f)));
        // case 2: 120° -> green
        assertEquals("00ff00", toHex6.invoke(null, hsvToRgb.invoke(null, 120f, 1f, 1f)));
        // case 3 (MISSING): 180° -> cyan (#00ffff)
        assertEquals("00ffff", toHex6.invoke(null, hsvToRgb.invoke(null, 180f, 1f, 1f)));
        // case 4: 240° -> blue
        assertEquals("0000ff", toHex6.invoke(null, hsvToRgb.invoke(null, 240f, 1f, 1f)));
        // default (MISSING): 300° -> magenta (#ff00ff)
        assertEquals("ff00ff", toHex6.invoke(null, hsvToRgb.invoke(null, 300f, 1f, 1f)));
    }

    // -------------------------- char helpers (private) --------------------------
    @Test
    void character_class_helpers_cover_all_branches() throws Exception {
        Method isUpper = priv("isUpper", char.class);
        Method isLetter = priv("isLetter", char.class);
        Method isDigit = priv("isDigit", char.class);
        Method isPunct = priv("isPunctGlobal", char.class); // keep your existing punct tests

        // --- isUpper: inside + both boundaries + just outside on each side
        assertTrue((Boolean) isUpper.invoke(null, 'A'));   // lower bound
        assertTrue((Boolean) isUpper.invoke(null, 'M'));   // middle
        assertTrue((Boolean) isUpper.invoke(null, 'Z'));   // upper bound
        assertFalse((Boolean) isUpper.invoke(null, '@'));  // just below 'A'
        assertFalse((Boolean) isUpper.invoke(null, '['));  // just above 'Z'

        // --- isLetter: uppercase path (delegates to isUpper), lowercase path, and non-letter
        assertTrue((Boolean) isLetter.invoke(null, 'A'));  // via isUpper(c)
        assertTrue((Boolean) isLetter.invoke(null, 'Z'));  // boundary via isUpper(c)
        assertTrue((Boolean) isLetter.invoke(null, 'a'));  // lower bound of lowercase range
        assertTrue((Boolean) isLetter.invoke(null, 'z'));  // upper bound of lowercase range
        assertFalse((Boolean) isLetter.invoke(null, '`')); // just below 'a'
        assertFalse((Boolean) isLetter.invoke(null, '{')); // just above 'z'

        // --- isDigit: inside + both boundaries + just outside on each side
        assertTrue((Boolean) isDigit.invoke(null, '0'));   // lower bound
        assertTrue((Boolean) isDigit.invoke(null, '5'));   // middle
        assertTrue((Boolean) isDigit.invoke(null, '9'));   // upper bound
        assertFalse((Boolean) isDigit.invoke(null, '/'));  // just below '0'
        assertFalse((Boolean) isDigit.invoke(null, ':'));  // just above '9'

        // --- isPunctGlobal (from your original): early-return + typical true/false
        assertFalse((Boolean) isPunct.invoke(null, ' '));  // early-return branch
        for (char c : new char[]{'.', '!', '?', '-', '/', '\\', '[', ']', '@', '#', '{', '}', '^', '*', '~', '`'}) {
            assertTrue((Boolean) isPunct.invoke(null, c), "expected punct true for: " + c);
        }
        assertFalse((Boolean) isPunct.invoke(null, 'A'));  // non-punct
    }

    // -------------------------- parseCrazyPieces + joinPiecesStatic (private) --------------------------
    @Test
    void parseCrazyPieces_tagEnd_missing_treated_as_plain_remainder() throws Exception {
        Method parse = priv("parseCrazyPieces", String.class);
        // Missing the '}' that ends the opening tag header
        String src = "lead {CRAZY style=blast inner text with no close brace";
        @SuppressWarnings("unchecked")
        List<Object> pieces = (List<Object>) parse.invoke(null, src);

        // Expect two pieces: "lead " (plain) + remainder starting at "{CRAZY..." as plain
        assertEquals(2, pieces.size());
        assertEquals("PLAIN", get(pieces.get(0), "kind").toString());
        assertEquals("lead ", get(pieces.get(0), "text").toString());
        assertEquals("PLAIN", get(pieces.get(1), "kind").toString());
        assertTrue(get(pieces.get(1), "text").toString().startsWith("{CRAZY"));
    }

    @Test
    void parseCrazyPieces_close_tag_missing_treated_as_plain_remainder() throws Exception {
        Method parse = priv("parseCrazyPieces", String.class);
        // Proper header ends with '}', but missing the closing "{/CRAZY}"
        String src = "head {CRAZY fps=60}dangling inner text without close";
        @SuppressWarnings("unchecked")
        List<Object> pieces = (List<Object>) parse.invoke(null, src);

        // Expect two pieces: "head " (plain) + remainder starting at "{CRAZY..." as plain
        assertEquals(2, pieces.size());
        assertEquals("PLAIN", get(pieces.get(0), "kind").toString());
        assertEquals("head ", get(pieces.get(0), "text").toString());
        assertEquals("PLAIN", get(pieces.get(1), "kind").toString());
        assertTrue(get(pieces.get(1), "text").toString().startsWith("{CRAZY fps=60}"));
    }

    @Test
    void joinPiecesStatic_concatenates_in_order_for_plain_and_crazy() throws Exception {
        Method join = priv("joinPiecesStatic", List.class);
        Class<?> pieceClz = Class.forName("com.csse3200.game.ui.effects.TextEffects$Piece");
        Method plain = pieceClz.getDeclaredMethod("plain", String.class);
        Method crazy = pieceClz.getDeclaredMethod("crazy", String.class, Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts"));

        // Build pieces: PLAIN + CRAZY + PLAIN
        Object p1 = plain.invoke(null, "pre ");
        Object p2 = crazy.invoke(null, "MID", /* opts */ null); // text field still "MID"
        Object p3 = plain.invoke(null, " post");

        String out = (String) join.invoke(null, Arrays.asList(p1, p2, p3));
        assertEquals("pre MID post", out);
    }

    @Test
    void joinPiecesStatic_handles_empty_list() throws Exception {
        Method join = priv("joinPiecesStatic", List.class);
        String out = (String) join.invoke(null, Collections.emptyList());
        assertEquals("", out);
    }

    @Test
    void joinPiecesStatic_single_element_passthrough() throws Exception {
        Method join = priv("joinPiecesStatic", List.class);
        Class<?> pieceClz = Class.forName("com.csse3200.game.ui.effects.TextEffects$Piece");
        Method plain = pieceClz.getDeclaredMethod("plain", String.class);
        Object p = plain.invoke(null, "solo");
        String out = (String) join.invoke(null, Collections.singletonList(p));
        assertEquals("solo", out);
    }

    @Test
    void joinPiecesStatic_includes_empty_and_null_text_safely() throws Exception {
        Method join = priv("joinPiecesStatic", List.class);
        Class<?> pieceClz = Class.forName("com.csse3200.game.ui.effects.TextEffects$Piece");
        Method plain = pieceClz.getDeclaredMethod("plain", String.class);

        // Create one with empty text and (if your Piece.plain guards null->"", this will still be "")
        Object pEmpty = plain.invoke(null, "");
        Object pNull = plain.invoke(null, (Object) null); // call with null if factory permits

        String out = (String) join.invoke(null, Arrays.asList(pEmpty, pNull, plain.invoke(null, "X")));
        // If Piece guards null->"", expect just "X" prefixed by two empties
        assertEquals("nullX", out);
    }

    @Test
    void originIndex_boundaries_and_modes() throws Exception {
        Method m = TE_m("originIndex", int.class, ORIGIN());
        // n=0 -> always 0
        assertEquals(0, m.invoke(null, 0, Enum.valueOf((Class) ORIGIN(), "LEFT")));
        assertEquals(0, m.invoke(null, 0, Enum.valueOf((Class) ORIGIN(), "MIDDLE")));
        assertEquals(0, m.invoke(null, 0, Enum.valueOf((Class) ORIGIN(), "RIGHT")));
        // n=1 -> always 0
        assertEquals(0, m.invoke(null, 1, Enum.valueOf((Class) ORIGIN(), "LEFT")));
        assertEquals(0, m.invoke(null, 1, Enum.valueOf((Class) ORIGIN(), "MIDDLE")));
        assertEquals(0, m.invoke(null, 1, Enum.valueOf((Class) ORIGIN(), "RIGHT")));
        // n=5 -> LEFT=0, MIDDLE=2, RIGHT=4
        assertEquals(0, m.invoke(null, 5, Enum.valueOf((Class) ORIGIN(), "LEFT")));
        assertEquals(2, m.invoke(null, 5, Enum.valueOf((Class) ORIGIN(), "MIDDLE")));
        assertEquals(4, m.invoke(null, 5, Enum.valueOf((Class) ORIGIN(), "RIGHT")));
    }

    @Test
    void glyphSpan_all_categories() throws Exception {
        Method m = TE_m("glyphSpan", char.class);
        assertEquals(26, m.invoke(null, 'A'));
        assertEquals(26, m.invoke(null, 'z'));
        assertEquals(10, m.invoke(null, '5'));
        int ringLen = (int) m.invoke(null, '.'); // punct returns ring length
        assertTrue(ringLen >= 1);
        // "other" path (space) returns ring length too
        assertEquals(ringLen, m.invoke(null, ' '));
    }

    @Test
    void initSeedState_sets_curr_and_remaining_per_category() throws Exception {
        // private static method TextEffects.initSeedState(char[], int[], int, char, int, CrazyOpts)
        Method m = TE_m(
                "initSeedState",
                char[].class, int[].class, int.class, char.class, int.class, CRAZY_OPTS()
        );

        // Build CrazyOpts deterministically: from=A
        Object opts = optsFrom("from=A");

        char[] curr = new char[4];
        int[] rem = new int[4];

        // letter
        m.invoke(null, curr, rem, 0, 'C', 7, opts);
        assertTrue(Character.isLetter(curr[0]));
        assertEquals(7, rem[0]);

        // digit
        m.invoke(null, curr, rem, 1, '8', 5, opts);
        assertTrue(Character.isDigit(curr[1]));
        assertEquals(5, rem[1]);

        // punct
        m.invoke(null, curr, rem, 2, '.', 3, opts);
        assertTrue(".,;:!?-_/\\|@#$%^&*()[]{}<>'\"+=~`".indexOf(curr[2]) >= 0);
        assertEquals(3, rem[2]);

        // other (space) -> snap, remaining=0
        m.invoke(null, curr, rem, 3, ' ', 9, opts);
        assertEquals(' ', curr[3]);
        assertEquals(0, rem[3]);
    }


    @Test
    void textEffects_appendEscaped_escapes_bracket_and_passes_other() throws Exception {
        Method m = TE_m("appendEscaped", StringBuilder.class, char.class);
        StringBuilder a = new StringBuilder();
        m.invoke(null, a, '[');
        assertEquals("[[", a.toString());
        StringBuilder b = new StringBuilder();
        m.invoke(null, b, 'X');
        assertEquals("X", b.toString());
    }

    @Test
    void baseDelay_covers_off_and_on_and_zero_edges() throws Exception {
        Method m = TE_m("baseDelay", int.class, int.class, boolean.class, int.class, int.class);
        // distanceDelay off -> returns jitter only
        assertEquals(0, m.invoke(null, 3, 10, false, 0, 99));   // jitter=0 -> 0
        // distanceDelay on, spread=0 -> jitter only
        assertTrue((int) m.invoke(null, 3, 10, true, 1, 0) >= 0);
        // distanceDelay on, spread>0 -> >= dist*spread
        int out = (int) m.invoke(null, 7, 2, true, 0, 5); // dist=5 -> base 25
        assertTrue(out >= 25);
    }

    @Test
    void inverseEdgeSpan_zero_span_returns_zero() throws Exception {
        Method m = TE_m("inverseEdgeSpan", int.class, int.class);
        // n=1, any origin -> edgeSpan==0
        assertEquals(0f, (float) m.invoke(null, 1, 0), 0f);
    }

    @Test
    void ensureOwnStyle_handles_null_style_and_clones_when_present() {
        TextEffects fx = new TextEffects();

        // ---- Build a style with a mocked font (no disk IO) ----
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
        data.markupEnabled = false;
        data.capHeight = 10f;
        data.ascent = -8f;
        data.lineHeight = 12f;

        BitmapFont font = mock(BitmapFont.class);
        when(font.getData()).thenReturn(data);
        when(font.getColor()).thenReturn(Color.WHITE);
        when(font.getCapHeight()).thenReturn(data.capHeight);
        when(font.getAscent()).thenReturn(data.ascent);
        when(font.getLineHeight()).thenReturn(data.lineHeight);
        when(font.usesIntegerPositions()).thenReturn(true);

        BitmapFontCache cache = mock(BitmapFontCache.class);
        when(cache.getFont()).thenReturn(font);
        when(font.newFontCache()).thenReturn(cache);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        // ---- Null-style path: override getStyle() to simulate missing style ----
        Label nullStyleLabel = new Label("", style) {
            @Override
            public LabelStyle getStyle() {
                return null;
            }

            // make setStyle a no-op so parent ctor doesn't trip anything weird
            @Override
            public void setStyle(LabelStyle s) { /* no-op */ }
        };
        assertDoesNotThrow(() -> TextEffects.ensureOwnStyle(nullStyleLabel)); // early return when style is null

        // ---- Clone path: style present -> defensive copy installed ----
        Label hasStyle = new Label("", style);
        TextEffects.ensureOwnStyle(hasStyle);

        assertNotSame(style, hasStyle.getStyle(), "style should be defensively cloned");
        // sanity: basic fields copied over
        assertSame(style.font, hasStyle.getStyle().font);
        assertEquals(style.fontColor, hasStyle.getStyle().fontColor);
    }


    // -------------------------- Gdx.files stub --------------------------
    static class MemFiles implements Files {
        final Map<String, String> mem = new HashMap<>();

        private FileHandle fh(String path) {
            return new FileHandle() {
                @Override
                public String readString() {
                    String c = mem.get(path);
                    if (c == null) throw new RuntimeException("no file");
                    return c;
                }

                @Override
                public String readString(String charset) {
                    return new String(readBytes(), StandardCharsets.UTF_8);
                }

                @Override
                public boolean exists() {
                    return mem.containsKey(path);
                }

                @Override
                public byte[] readBytes() {
                    String c = mem.get(path);
                    if (c == null) throw new RuntimeException("no file");
                    return c.getBytes(StandardCharsets.UTF_8);
                }
            };
        }

        @Override
        public FileHandle getFileHandle(String s, FileType t) {
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

    // -------------------------- parseOpts (private) --------------------------
    @Nested
    class TextEffectsParseOptsTest {

        private static Object parse(String s) throws Exception {
            Method m = priv("parseOpts", String.class);
            return m.invoke(null, s);
        }

        @SuppressWarnings("unchecked")
        private static <T> T get(Object o, String fieldName) throws Exception {
            var f = o.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(o);
        }

        // Typed getters to avoid JUnit assertEquals overload ambiguity
        private static int geti(Object o, String f) throws Exception {
            return ((Number) get(o, f)).intValue();
        }

        private static float getf(Object o, String f) throws Exception {
            return ((Number) get(o, f)).floatValue();
        }

        private static boolean getb(Object o) throws Exception {
            return get(o, "rainbow");
        }

        private static Enum<?> enumConst(Object enumObj, String name) {
            Class<? extends Enum> cls = (Class<? extends Enum>) enumObj.getClass();
            return Enum.valueOf(cls, name);
        }

        @Test
        void null_and_empty_return_defaults() throws Exception {
            Object o1 = parse(null);
            Object o2 = parse("");

            assertNotNull(o1);
            assertNotNull(o2);

            // numbers: use primitive overloads
            assertEquals(geti(o1, "fps"), geti(o2, "fps"));

            // enums: compare as enums (Object,Object overload), not strings/char[]
            Object style1 = get(o1, "style");
            Object style2 = get(o2, "style");
            Object origin1 = get(o1, "origin");
            Object origin2 = get(o2, "origin");
            assertEquals(style1, style2);
            assertEquals(origin1, origin2);
        }


        @Test
        void ignores_tokens_without_equals() throws Exception {
            Object o = parse("foo bar=baz quux"); // "foo" and "quux" ignored; "bar=baz" unknown -> ignored
            Object def = parse("");
            assertEquals(get(def, "flashHexA"), (String) get(o, "flashHexA"));
            assertEquals(geti(def, "fps"), geti(o, "fps"));
        }

        @Test
        void fps_clamped_1_to_240() throws Exception {
            Object lo = parse("fps=0");
            Object hi = parse("fps=999");
            Object ok = parse("fps=60");
            assertEquals(1, geti(lo, "fps"));
            assertEquals(240, geti(hi, "fps"));
            assertEquals(60, geti(ok, "fps"));
        }

        @Test
        void jitter_clamped_0_to_60() throws Exception {
            Object lo = parse("jitter=-5");
            Object hi = parse("jitter=1000");
            Object ok = parse("jitter=7");
            assertEquals(0, geti(lo, "jitter"));
            assertEquals(60, geti(hi, "jitter"));
            assertEquals(7, geti(ok, "jitter"));
        }

        @Test
        void cycles_clamped_0_to_10() throws Exception {
            Object lo = parse("cycles=-1");
            Object hi = parse("cycles=99");
            Object ok = parse("cycles=3");
            assertEquals(0, geti(lo, "cycles"));
            assertEquals(10, geti(hi, "cycles"));
            assertEquals(3, geti(ok, "cycles"));
        }

        @Test
        void from_rand_or_default_A() throws Exception {
            Object r = parse("from=rand");
            Object a = parse("from=a");

            Object rEnum = get(r, "from");
            Object aEnum = get(a, "from");

            assertEquals(enumConst(rEnum, "RAND"), rEnum);
            assertEquals(enumConst(aEnum, "A"), aEnum);
        }

        @Test
        void rainbow_true_false_variants() throws Exception {
            Object t1 = parse("rainbow=true");
            Object t2 = parse("rainbow=1");
            Object f1 = parse("rainbow=false");
            Object f2 = parse("rainbow=0");
            assertTrue(getb(t1));
            assertTrue(getb(t2));
            assertFalse(getb(f1));
            assertFalse(getb(f2));
        }

        @Test
        void rhz_clamped_0p01_to_5() throws Exception {
            Object lo = parse("rhz=0.0001");
            Object hi = parse("rhz=99");
            Object ok = parse("rhz=2.5");
            assertEquals(0.01f, getf(lo, "rhz"), 1e-6f);
            assertEquals(5f, getf(hi, "rhz"), 1e-6f);
            assertEquals(2.5f, getf(ok, "rhz"), 1e-6f);
        }

        @Test
        void rshift_clamped_0_to_360() throws Exception {
            Object lo = parse("rshift=-1");
            Object hi = parse("rshift=999");
            Object ok = parse("rshift=180");
            assertEquals(0f, getf(lo, "rshift"), 1e-6f);
            assertEquals(360f, getf(hi, "rshift"), 1e-6f);
            assertEquals(180f, getf(ok, "rshift"), 1e-6f);
        }

        @Test
        void style_explode_blast_default_normal() throws Exception {
            Object ex = parse("style=explode");
            Object bl = parse("style=blast");
            Object def = parse("style=weird"); // default -> NORMAL

            Object exEnum = get(ex, "style");
            Object blEnum = get(bl, "style");
            Object defEnum = get(def, "style");

            Enum<?> explode = java.util.Arrays.stream(exEnum.getClass().getEnumConstants())
                    .map(c -> (Enum<?>) c)
                    .filter(e -> e.name().equals("EXPLODE"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No EXPLODE"));
            Enum<?> blast = java.util.Arrays.stream(blEnum.getClass().getEnumConstants())
                    .map(c -> (Enum<?>) c)
                    .filter(e -> e.name().equals("BLAST"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No BLAST"));
            Enum<?> normal = java.util.Arrays.stream(defEnum.getClass().getEnumConstants())
                    .map(c -> (Enum<?>) c)
                    .filter(e -> e.name().equals("NORMAL"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No NORMAL"));

            assertEquals(explode, exEnum);
            assertEquals(blast, blEnum);
            assertEquals(normal, defEnum);
        }

        @Test
        void origin_left_right_default_middle() throws Exception {
            Object l = parse("origin=left");
            Object r = parse("origin=right");
            Object m = parse("origin=center");

            // Compare the actual enum values
            Object leftEnum = get(l, "origin");
            Object rightEnum = get(r, "origin");
            Object midEnum = get(m, "origin");

            Enum<?> left = java.util.Arrays.stream(leftEnum.getClass().getEnumConstants())
                    .map(c -> (Enum<?>) c)
                    .filter(e -> e.name().equals("LEFT"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No LEFT"));
            Enum<?> right = java.util.Arrays.stream(rightEnum.getClass().getEnumConstants())
                    .map(c -> (Enum<?>) c)
                    .filter(e -> e.name().equals("RIGHT"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No RIGHT"));
            Enum<?> middle = java.util.Arrays.stream(midEnum.getClass().getEnumConstants())
                    .map(c -> (Enum<?>) c)
                    .filter(e -> e.name().equals("MIDDLE"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No MIDDLE"));

            assertEquals(left, leftEnum);
            assertEquals(right, rightEnum);
            assertEquals(middle, midEnum);
        }

        @Test
        void nonneg_ints_spread_flash_overshoot_and_edgeboost_clamp() throws Exception {
            Object neg = parse("spread=-1 flash=-2 overshoot=-3 edgeboost=-0.5");
            Object hi = parse("edgeboost=2");
            Object ok = parse("spread=7 flash=9 overshoot=11 edgeboost=0.75");
            assertEquals(0, geti(neg, "spread"));
            assertEquals(0, geti(neg, "flashFrames"));
            assertEquals(0, geti(neg, "overshoot"));
            assertEquals(0f, getf(neg, "edgeBoost"), 1e-6f);
            assertEquals(1f, getf(hi, "edgeBoost"), 1e-6f);
            assertEquals(7, geti(ok, "spread"));
            assertEquals(9, geti(ok, "flashFrames"));
            assertEquals(11, geti(ok, "overshoot"));
            assertEquals(0.75f, getf(ok, "edgeBoost"), 1e-6f);
        }

        @Test
        void flash_hexes_strip_hash() throws Exception {
            Object o = parse("flashHexA=#ff00aa flashHexB=00ffcc");
            assertEquals("ff00aa", get(o, "flashHexA"));
            assertEquals("00ffcc", get(o, "flashHexB"));
        }

        @Test
        void numberformat_exception_is_ignored_and_does_not_change_defaults() throws Exception {
            Object def = parse("");
            Object bad = parse("fps=NaN jitter=oops cycles=?? rhz=cat rshift=dog spread=bad flash=no overshoot=lol edgeboost=oops");
            assertEquals(geti(def, "fps"), geti(bad, "fps"));
            assertEquals(geti(def, "jitter"), geti(bad, "jitter"));
            assertEquals(geti(def, "cycles"), geti(bad, "cycles"));
            assertEquals(getf(def, "rhz"), getf(bad, "rhz"), 1e-6f);
            assertEquals(getf(def, "rshift"), getf(bad, "rshift"), 1e-6f);
            assertEquals(geti(def, "spread"), geti(bad, "spread"));
            assertEquals(geti(def, "flashFrames"), geti(bad, "flashFrames"));
            assertEquals(geti(def, "overshoot"), geti(bad, "overshoot"));
            assertEquals(getf(def, "edgeBoost"), getf(bad, "edgeBoost"), 1e-6f);
        }

        @Test
        void mixed_options_all_paths_once() throws Exception {
            Object o = parse("  FPS=300  jitter=61 cycles=10  FROM=rand  rainbow=1  RHZ=0.001 rshift=361 style=explode origin=right spread=0 flash=1 overshoot=2 edgeboost=1.5  flashHexA=#abc flashHexB=def  ");

            // ints
            assertEquals(240, geti(o, "fps"));
            assertEquals(60, geti(o, "jitter"));
            assertEquals(10, geti(o, "cycles"));

            // enums & booleans
            Object fromEnum = get(o, "from");
            Object styleEnum = get(o, "style");
            Object originEnum = get(o, "origin");
            assertEquals(enumConst(fromEnum, "RAND"), fromEnum);
            assertTrue(getb(o));
            assertEquals(enumConst(styleEnum, "EXPLODE"), styleEnum);
            assertEquals(enumConst(originEnum, "RIGHT"), originEnum);

            // floats (with delta)
            assertEquals(0.01f, getf(o, "rhz"), 1e-6f);
            assertEquals(360f, getf(o, "rshift"), 1e-6f);
            assertEquals(1f, getf(o, "edgeBoost"), 1e-6f);

            // remaining ints
            assertEquals(0, geti(o, "spread"));
            assertEquals(1, geti(o, "flashFrames"));
            assertEquals(2, geti(o, "overshoot"));

            // strings
            assertEquals("abc", get(o, "flashHexA"));
            assertEquals("def", get(o, "flashHexB"));
        }
    }


}
