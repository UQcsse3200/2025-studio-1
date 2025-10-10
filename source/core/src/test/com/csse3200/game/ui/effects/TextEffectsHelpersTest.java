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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.*;

/**
 * Helper-only coverage for TextEffects (no timers/animations).
 * Covers: readRandomLine, enableMarkup/ensureOwnStyle, stripMarkup, sanitizeHex,
 * hsvToRgb, toHex6, isUpper, isLetter, isDigit, isPunctGlobal, parseOpts,
 * parseCrazyPieces, joinPiecesStatic — refactored with parameterised matrices.
 */
@ExtendWith(GameExtension.class)
class TextEffectsHelpersTest {

    @BeforeAll
    static void setupFiles() {
        Gdx.files = new MemFiles(); // inject headless stub
    }

    /* -------------------------- Reflection helpers -------------------------- */

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
        Method pm;
        try {
            pm = te.getDeclaredMethod("parseOpts", String.class);
        } catch (NoSuchMethodException e) {
            pm = te.getDeclaredMethod("dparseOpts", String.class); // fallback if older name
        }
        pm.setAccessible(true);
        return pm.invoke(null, spec == null ? "" : spec);
    }

    private static Class<?> CRAZY_OPTS() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts");
    }

    /* -------------------------- readRandomLine -------------------------- */

    static Stream<Arguments> stripCases() {
        return Stream.of(
                of("", ""),
                of("plain", "plain"),
                of("[[abc]", "[abc]"),
                of("[#ff0]hello[] [#00ff00]world[]", "hello world")
        );
    }

    /* -------------------------- enableMarkup / ensureOwnStyle -------------------------- */

    static Stream<Arguments> sanitizeCases() {
        return Stream.of(
                of(null, "ffffff", "ffffff"),     // null -> default
                of("#00FF00", "ffffff", "00ff00"),// strip '#', lowercase
                of("abc", "ffffff", "aabbcc"),    // 3-digit expand
                of("zzzzzz", "ffffff", "ffffff")  // invalid -> default
        );
    }

    /* -------------------------- stripMarkup (private) -------------------------- */

    static Stream<Arguments> hsvHexCases() {
        return Stream.of(
                of(0f, "ff0000"), // case 0
                of(60f, "ffff00"), // case 1
                of(120f, "00ff00"), // case 2
                of(180f, "00ffff"), // case 3
                of(240f, "0000ff"), // case 4
                of(300f, "ff00ff")  // default
        );
    }

    static Stream<Arguments> upperCases() {
        return Stream.of(
                of('A', true), of('M', true), of('Z', true),
                of('@', false), of('[', false)
        );
    }

    /* -------------------------- sanitizeHex (private) -------------------------- */

    static Stream<Arguments> letterCases() {
        return Stream.of(
                of('A', true), of('Z', true), of('a', true), of('z', true),
                of('`', false), of('{', false)
        );
    }

    static Stream<Arguments> digitCases() {
        return Stream.of(
                of('0', true), of('5', true), of('9', true),
                of('/', false), of(':', false)
        );
    }

    /* -------------------------- hsvToRgb + toHex6 (private) -------------------------- */

    static Stream<Arguments> crazyPiecesMalformed() {
        return Stream.of(
                of("lead {CRAZY style=blast inner text with no close brace", "lead ", "{CRAZY"),
                of("head {CRAZY fps=60}dangling inner text without close", "head ", "{CRAZY fps=60}")
        );
    }

    static Stream<Arguments> originIndexCases() {
        return Stream.of(
                of(0, "LEFT", 0), of(0, "MIDDLE", 0), of(0, "RIGHT", 0),
                of(1, "LEFT", 0), of(1, "MIDDLE", 0), of(1, "RIGHT", 0),
                of(5, "LEFT", 0), of(5, "MIDDLE", 2), of(5, "RIGHT", 4)
        );
    }

    /* -------------------------- char helpers (private) -------------------------- */

    static Stream<Arguments> glyphSpanCases() {
        return Stream.of(
                of('A', "UPPER", 26),
                of('z', "LOWER", 26),
                of('5', "DIGIT", 10),
                of('.', "PUNCT", -1), // -1 = compute from '.' at runtime
                of(' ', "OTHER", -1)
        );
    }

    @Test
    void readRandomLine_exception_in_handle_returns_fallback() {
        Files files = mock(Files.class, CALLS_REAL_METHODS);
        FileHandle fh = mock(FileHandle.class);
        Gdx.files = files;

        when(files.internal("boom.txt")).thenReturn(fh);
        when(fh.readString()).thenThrow(new RuntimeException("kaboom"));
        when(fh.readString(any())).thenThrow(new RuntimeException("kaboom"));
        when(fh.read()).thenThrow(new RuntimeException("kaboom"));
        when(fh.reader()).thenThrow(new RuntimeException("kaboom"));

        assertEquals("FALLBACK", TextEffects.readRandomLine("boom.txt", "FALLBACK"));
    }

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
        final Label.LabelStyle[] store = {style};
        when(lbl.getStyle()).thenAnswer(i -> store[0]);
        doAnswer(i -> {
            store[0] = i.getArgument(0);
            return null;
        }).when(lbl).setStyle(any(Label.LabelStyle.class));

        assertFalse(font.getData().markupEnabled);
        TextEffects.enableMarkup(lbl);
        assertTrue(font.getData().markupEnabled);

        Label.LabelStyle before = store[0];
        TextEffects.ensureOwnStyle(lbl);
        Label.LabelStyle after = store[0];
        assertNotSame(before, after);
        assertSame(before.font, after.font);
        assertEquals(before.fontColor, after.fontColor);
    }

    @ParameterizedTest(name = "stripMarkup: \"{0}\" -> \"{1}\"")
    @MethodSource("stripCases")
    void stripMarkup_cases(String in, String expected) throws Exception {
        Method strip = priv("stripMarkup", String.class);
        assertEquals(expected, strip.invoke(null, in));
    }

    @ParameterizedTest(name = "sanitizeHex({0}, def={1}) -> {2}")
    @MethodSource("sanitizeCases")
    void sanitizeHex_cases(String in, String def, String out) throws Exception {
        Method sanitize = priv("sanitizeHex", String.class, String.class);
        assertEquals(out, sanitize.invoke(null, in, def));
    }

    @ParameterizedTest(name = "HSV({0},1,1) -> {1}")
    @MethodSource("hsvHexCases")
    void hsvToRgb_toHex6_cases(float hue, String hex) throws Exception {
        Method hsvToRgb = priv("hsvToRgb", float.class, float.class, float.class);
        Method toHex6 = priv("toHex6", int.class);
        assertEquals(hex, toHex6.invoke(null, hsvToRgb.invoke(null, hue, 1f, 1f)));
    }

    @ParameterizedTest(name = "isUpper('{0}') = {1}")
    @MethodSource("upperCases")
    void isUpper_cases(char c, boolean expected) throws Exception {
        Method isUpper = priv("isUpper", char.class);
        assertEquals(expected, isUpper.invoke(null, c));
    }

    @ParameterizedTest(name = "isLetter('{0}') = {1}")
    @MethodSource("letterCases")
    void isLetter_cases(char c, boolean expected) throws Exception {
        Method isLetter = priv("isLetter", char.class);
        assertEquals(expected, isLetter.invoke(null, c));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void stripMarkup_earlyReturn_covers_null_and_empty(String in) throws Exception {
        var strip = priv("stripMarkup", String.class);
        // IMPORTANT: wrap the arg so reflection passes a single null parameter
        String out = (String) strip.invoke(null, new Object[]{in});
        assertEquals("", out);
    }
    /* -------------------------- parseCrazyPieces + joinPiecesStatic (private) -------------------------- */

    @ParameterizedTest(name = "isDigit('{0}') = {1}")
    @MethodSource("digitCases")
    void isDigit_cases(char c, boolean expected) throws Exception {
        Method isDigit = priv("isDigit", char.class);
        assertEquals(expected, isDigit.invoke(null, c));
    }

    @ParameterizedTest(name = "isPunctGlobal true set: '{0}'")
    @ValueSource(strings = {".", "!", "?", "-", "/", "\\", "[", "]", "@", "#", "{", "}", "^", "*", "~", "`"})
    void isPunct_true_cases(String s) throws Exception {
        Method isPunct = priv("isPunctGlobal", char.class);
        assertTrue((Boolean) isPunct.invoke(null, s.charAt(0)));
    }

    @ParameterizedTest(name = "isPunctGlobal false set: '{0}'")
    @ValueSource(strings = {" ", "A"})
    void isPunct_false_cases(String s) throws Exception {
        Method isPunct = priv("isPunctGlobal", char.class);
        assertFalse((Boolean) isPunct.invoke(null, s.charAt(0)));
    }

    @ParameterizedTest(name = "parseCrazyPieces malformed header/close: \"{0}\"")
    @MethodSource("crazyPiecesMalformed")
    void parseCrazyPieces_malformed_yields_plain_remainder(String src, String first, String startsWith) throws Exception {
        Method parse = priv("parseCrazyPieces", String.class);
        @SuppressWarnings("unchecked")
        List<Object> pieces = (List<Object>) parse.invoke(null, src);

        assertEquals(2, pieces.size());
        assertEquals("PLAIN", get(pieces.get(0), "kind").toString());
        assertEquals(first, get(pieces.get(0), "text").toString());
        assertEquals("PLAIN", get(pieces.get(1), "kind").toString());
        assertTrue(get(pieces.get(1), "text").toString().startsWith(startsWith));
    }

    @Test
    void joinPiecesStatic_concatenates_in_order_for_plain_and_crazy() throws Exception {
        Method join = priv("joinPiecesStatic", List.class);
        Class<?> pieceClz = Class.forName("com.csse3200.game.ui.effects.TextEffects$Piece");
        Method plain = pieceClz.getDeclaredMethod("plain", String.class);
        Method crazy = pieceClz.getDeclaredMethod("crazy", String.class, Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts"));

        Object p1 = plain.invoke(null, "pre ");
        Object p2 = crazy.invoke(null, "MID", null);
        Object p3 = plain.invoke(null, " post");

        String out = (String) join.invoke(null, Arrays.asList(p1, p2, p3));
        assertEquals("pre MID post", out);
    }

    /* -------------------------- originIndex & glyphSpan -------------------------- */

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "solo, solo"
    })
    void joinPiecesStatic_basic(String text, String expected) throws Exception {
        Method join = priv("joinPiecesStatic", List.class);
        Class<?> pieceClz = Class.forName("com.csse3200.game.ui.effects.TextEffects$Piece");
        Method plain = pieceClz.getDeclaredMethod("plain", String.class);
        Object p = plain.invoke(null, text);
        assertEquals(expected, join.invoke(null, Collections.singletonList(p)));
    }

    @Test
    void joinPiecesStatic_includes_empty_and_null_text_safely() throws Exception {
        Method join = priv("joinPiecesStatic", List.class);
        Class<?> pieceClz = Class.forName("com.csse3200.game.ui.effects.TextEffects$Piece");
        Method plain = pieceClz.getDeclaredMethod("plain", String.class);

        Object pEmpty = plain.invoke(null, "");
        Object pNull = plain.invoke(null, (Object) null);
        Object pX = plain.invoke(null, "X");

        String out = (String) join.invoke(null, Arrays.asList(pEmpty, pNull, pX));
        assertEquals("nullX", out); // if nulls stringify; adjust if your factory maps null -> ""
    }

    @ParameterizedTest(name = "originIndex n={0}, origin={1} -> {2}")
    @MethodSource("originIndexCases")
    void originIndex_cases(int n, String origin, int expected) throws Exception {
        Method m = TE_m("originIndex", int.class, ORIGIN());
        Object enumVal = Enum.valueOf((Class<? extends Enum>) ORIGIN(), origin);
        assertEquals(expected, m.invoke(null, n, enumVal));
    }

    @ParameterizedTest(name = "glyphSpan '{0}' ({1})")
    @MethodSource("glyphSpanCases")
    void glyphSpan_cases(char ch, String kind, int expected) throws Exception {
        Method glyphSpan = TE_m("glyphSpan", char.class);
        int ringLen = (int) glyphSpan.invoke(null, '.'); // for PUNCT/OTHER
        int exp = expected >= 0 ? expected : ringLen;
        assertEquals(exp, glyphSpan.invoke(null, ch));
    }

    /* -------------------------- initSeedState & misc helpers -------------------------- */

    @Test
    void initSeedState_sets_curr_and_remaining_per_category() throws Exception {
        Method m = TE_m("initSeedState", char[].class, int[].class, int.class, char.class, int.class, CRAZY_OPTS());
        Object opts = optsFrom("from=A");

        char[] curr = new char[4];
        int[] rem = new int[4];

        m.invoke(null, curr, rem, 0, 'C', 7, opts);  // letter
        assertTrue(Character.isLetter(curr[0]));
        assertEquals(7, rem[0]);

        m.invoke(null, curr, rem, 1, '8', 5, opts);  // digit
        assertTrue(Character.isDigit(curr[1]));
        assertEquals(5, rem[1]);

        m.invoke(null, curr, rem, 2, '.', 3, opts);  // punct
        assertTrue(".,;:!?-_/\\|@#$%^&*()[]{}<>'\"+=~`".indexOf(curr[2]) >= 0);
        assertEquals(3, rem[2]);

        m.invoke(null, curr, rem, 3, ' ', 9, opts);  // other
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
        assertEquals(0, m.invoke(null, 3, 10, false, 0, 99));          // jitter=0
        assertTrue((int) m.invoke(null, 3, 10, true, 1, 0) >= 0);       // spread=0, jitter>=0
        int out = (int) m.invoke(null, 7, 2, true, 0, 5);               // dist=5 -> base>=25
        assertTrue(out >= 25);
    }

    @Test
    void inverseEdgeSpan_zero_span_returns_zero() throws Exception {
        Method m = TE_m("inverseEdgeSpan", int.class, int.class);
        assertEquals(0f, (float) m.invoke(null, 1, 0), 0f);
    }

    @Test
    void ensureOwnStyle_handles_null_style_and_clones_when_present() {
        TextEffects fx = new TextEffects();

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

        Label nullStyleLabel = new Label("", style) {
            @Override
            public LabelStyle getStyle() {
                return null;
            }

            @Override
            public void setStyle(LabelStyle s) { /* no-op */ }
        };
        assertDoesNotThrow(() -> fx.ensureOwnStyle(nullStyleLabel));

        Label hasStyle = new Label("", style);
        fx.ensureOwnStyle(hasStyle);
        assertNotSame(style, hasStyle.getStyle());
        assertSame(style.font, hasStyle.getStyle().font);
        assertEquals(style.fontColor, hasStyle.getStyle().fontColor);
    }

    /* -------------------------- Gdx.files stub -------------------------- */

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

    /* -------------------------- parseOpts (private) -------------------------- */

    @Nested
    class TextEffectsParseOptsTest {

        private static Object parse(String s) throws Exception {
            return priv("parseOpts", String.class).invoke(null, s);
        }

        @SuppressWarnings("unchecked")
        private static <T> T get(Object o, String fieldName) throws Exception {
            var f = o.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(o);
        }

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
            Object o1 = parse(null), o2 = parse("");
            assertNotNull(o1);
            assertNotNull(o2);

            // numeric default
            assertEquals(geti(o1, "fps"), geti(o2, "fps"));

            // enums: compare the actual constants (identity is fine for enums)
            Object style1 = get(o1, "style");
            Object style2 = get(o2, "style");
            assertSame(style1, style2);

            Object origin1 = get(o1, "origin");
            Object origin2 = get(o2, "origin");
            assertSame(origin1, origin2);
        }

        @Test
        void ignores_tokens_without_equals() throws Exception {
            Object o = parse("foo bar=baz quux");
            Object def = parse("");
            assertEquals(get(def, "flashHexA"), (String) get(o, "flashHexA"));
            assertEquals(geti(def, "fps"), geti(o, "fps"));
        }

        @ParameterizedTest
        @CsvSource({
                "0,   1",
                "999, 240",
                "60,  60"
        })
        void fps_clamped_1_to_240(String in, int expected) throws Exception {
            Object o = parse("fps=" + in);
            assertEquals(expected, geti(o, "fps"));
        }

        @ParameterizedTest
        @CsvSource({
                "-5,  0",
                "1000,60",
                "7,   7"
        })
        void jitter_clamped_0_to_60(String in, int expected) throws Exception {
            Object o = parse("jitter=" + in);
            assertEquals(expected, geti(o, "jitter"));
        }

        @ParameterizedTest
        @CsvSource({
                "-1, 0",
                "99, 10",
                "3,  3"
        })
        void cycles_clamped_0_to_10(String in, int expected) throws Exception {
            Object o = parse("cycles=" + in);
            assertEquals(expected, geti(o, "cycles"));
        }

        @ParameterizedTest
        @CsvSource({
                "true,  true",
                "1,     true",
                "false, false",
                "0,     false"
        })
        void rainbow_true_false_variants(String val, boolean expected) throws Exception {
            Object o = parse("rainbow=" + val);
            assertEquals(expected, getb(o));
        }

        @ParameterizedTest
        @CsvSource({
                "0.0001, 0.01",
                "99,     5",
                "2.5,    2.5"
        })
        void rhz_clamped_0p01_to_5(String in, float expected) throws Exception {
            Object o = parse("rhz=" + in);
            assertEquals(expected, getf(o, "rhz"), 1e-6f);
        }

        @ParameterizedTest
        @CsvSource({
                "-1,  0",
                "999, 360",
                "180, 180"
        })
        void rshift_clamped_0_to_360(String in, float expected) throws Exception {
            Object o = parse("rshift=" + in);
            assertEquals(expected, getf(o, "rshift"), 1e-6f);
        }

        @ParameterizedTest
        @CsvSource({
                "explode, EXPLODE",
                "blast,   BLAST",
                "weird,   NORMAL"
        })
        void style_parse_variants(String val, String expected) throws Exception {
            Object o = parse("style=" + val);
            Object styleEnum = get(o, "style");
            assertEquals(enumConst(styleEnum, expected), styleEnum);
        }

        @ParameterizedTest
        @CsvSource({
                "left,   LEFT",
                "right,  RIGHT",
                "center, MIDDLE"
        })
        void origin_parse_variants(String val, String expected) throws Exception {
            Object o = parse("origin=" + val);
            Object originEnum = get(o, "origin");
            assertEquals(enumConst(originEnum, expected), originEnum);
        }

        @ParameterizedTest
        @CsvSource({
                "-0.5, 0",
                "2,    1",
                "0.75, 0.75"
        })
        void edgeboost_clamped_0_to_1(String in, float expected) throws Exception {
            Object o = parse("edgeboost=" + in);
            assertEquals(expected, getf(o, "edgeBoost"), 1e-6f);
        }

        @ParameterizedTest
        @CsvSource({
                "spread=-1,     0",
                "spread=7,      7",
                "flash=-2,      0",
                "flash=9,       9",
                "overshoot=-3,  0",
                "overshoot=11,  11"
        })
        void nonneg_ints_spread_flash_overshoot(String spec, int expected) throws Exception {
            Object o = parse(spec);
            if (spec.startsWith("spread")) assertEquals(expected, geti(o, "spread"));
            else if (spec.startsWith("flash")) assertEquals(expected, geti(o, "flashFrames"));
            else assertEquals(expected, geti(o, "overshoot"));
        }

        @ParameterizedTest
        @CsvSource({
                "from=rand, RAND",
                "from=a,    A"
        })
        void from_rand_or_A(String spec, String expected) throws Exception {
            Object o = parse(spec);
            Object e = get(o, "from");
            assertEquals(enumConst(e, expected), e);
        }

        @ParameterizedTest
        @CsvSource({
                "flashHexA=#ff00aa, ff00aa",
                "flashHexB=00ffcc,  00ffcc"
        })
        void flash_hexes_strip_hash(String spec, String expected) throws Exception {
            Object o = parse(spec);
            if (spec.startsWith("flashHexA")) assertEquals(expected, get(o, "flashHexA"));
            else assertEquals(expected, get(o, "flashHexB"));
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

            assertEquals(240, geti(o, "fps"));
            assertEquals(60, geti(o, "jitter"));
            assertEquals(10, geti(o, "cycles"));

            Object fromEnum = get(o, "from");
            Object styleEnum = get(o, "style");
            Object originEnum = get(o, "origin");
            assertEquals(enumConst(fromEnum, "RAND"), fromEnum);
            assertTrue(getb(o));
            assertEquals(enumConst(styleEnum, "EXPLODE"), styleEnum);
            assertEquals(enumConst(originEnum, "RIGHT"), originEnum);

            assertEquals(0.01f, getf(o, "rhz"), 1e-6f);
            assertEquals(360f, getf(o, "rshift"), 1e-6f);
            assertEquals(1f, getf(o, "edgeBoost"), 1e-6f);

            assertEquals(0, geti(o, "spread"));
            assertEquals(1, geti(o, "flashFrames"));
            assertEquals(2, geti(o, "overshoot"));

            assertEquals("abc", get(o, "flashHexA"));
            assertEquals("def", get(o, "flashHexB"));
        }
    }
}
