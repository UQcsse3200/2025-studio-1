package com.csse3200.game.ui.effects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Coverage-focused tests for TextEffects.BlockAnim private spinner helpers and currentString()
 * (updated for 7-arg BlockAnim ctor with Visual and BlastExtras)
 * <p>
 * Uses parameterised test matrices to keep things compact while exercising many branches.
 */
class BlockAnimSpinnersTest {

    // Mirror TextEffects.PUNCT_RING for invariants
    private static final char[] PUNCT_RING = (".,;:!?-_/\\|@#$%^&*()[]{}<>'\"+=~`").toCharArray();
    private static final Set<Character> PUNCT_SET = toSet();
    private static final int SAFE_LEN = 128;
    private static final Pattern COLOR_TAG = Pattern.compile("\\[#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})]");

    private static Set<Character> toSet() {
        Set<Character> s = new HashSet<>();
        for (char c : PUNCT_RING) s.add(c);
        return s;
    }

    private static Class<?> BA() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim");
    }

    private static Class<?> BAVIS() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim$Visual");
    }

    private static Class<?> BABLAST() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim$BlastExtras");
    }

    private static Constructor<?> BA_CTOR() throws Exception {
        Constructor<?> ctor = BA().getDeclaredConstructor(
                int.class, int[].class, int[].class, char[].class, char[].class, BAVIS(), BABLAST()
        );
        ctor.setAccessible(true);
        return ctor;
    }

    private static Object newVisual(boolean rainbow, float rhz, float rshift, String a, String b) throws Exception {
        Constructor<?> c = visualClass().getDeclaredConstructor(
                boolean.class, float.class, float.class, String.class, String.class);
        c.setAccessible(true);
        return c.newInstance(rainbow, rhz, rshift, a, b);
    }

    private static Object newBlast(int[] flashLeft, int[] overshootLeft, int[] postLockHold) throws Exception {
        Constructor<?> ctor = BABLAST().getDeclaredConstructor(int[].class, int[].class, int[].class);
        ctor.setAccessible(true);
        return ctor.newInstance(flashLeft, overshootLeft, postLockHold);
    }

    private static Object field(Object inst, String name) throws Exception {
        Field f = BA().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(inst);
    }

    private static int idxWrap(int idx) {
        return Math.floorMod(idx, SAFE_LEN);
    }

    private static Method spinner(String name) throws Exception {
        Class<?> cls = BA();
        // Prefer (int,char)
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                Class<?>[] pt = m.getParameterTypes();
                if (pt.length == 2 && pt[0] == int.class && pt[1] == char.class) {
                    m.setAccessible(true);
                    return m;
                }
            }
        }
        // Fallback (int)
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                Class<?>[] pt = m.getParameterTypes();
                if (pt.length == 1 && pt[0] == int.class) {
                    m.setAccessible(true);
                    return m;
                }
            }
        }
        throw new NoSuchMethodException(cls.getName() + "." + name + "(int[,char])");
    }

    /**
     * Invoke a spinner and return new curr[i] if changed, else null.
     */
    private static Character invokeSpinnerAndReadCurr(Object ba, Method m, int i, char t) throws Exception {
        char[] curr = (char[]) field(ba, "curr");
        char before = curr[i];

        Class<?>[] pt = m.getParameterTypes();
        if (pt.length == 2) {
            m.invoke(Modifier.isStatic(m.getModifiers()) ? null : ba, i, t);
        } else {
            m.invoke(Modifier.isStatic(m.getModifiers()) ? null : ba, i);
        }

        curr = (char[]) field(ba, "curr");
        char after = curr[i];
        return (after != before) ? after : null;
    }

    private static Object newBlockAnim(boolean rainbow) throws Exception {
        Constructor<?> ctor = BA_CTOR();
        int[] delays = new int[SAFE_LEN];
        int[] remaining = new int[SAFE_LEN];
        int[] overshootLeft = new int[SAFE_LEN];
        int[] postLockHold = new int[SAFE_LEN];
        char[] target = new char[SAFE_LEN];
        char[] curr = new char[SAFE_LEN];

        for (int i = 0; i < SAFE_LEN; i++) {
            char ch = (i < 8) ? (char) ('A' + i) : ' ';
            target[i] = ch;
            curr[i] = ch;
        }

        Object vis = newVisual(rainbow, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(null, overshootLeft, postLockHold);

        return ctor.newInstance(60, delays, remaining, target, curr, vis, blast);
    }


    private static Object newBA(Object visual, Object blast, int n) throws Exception {
        int[] delays = new int[n];
        int[] remaining = new int[n];
        char[] target = new char[n];
        char[] curr = new char[n];
        for (int i = 0; i < n; i++) target[i] = curr[i] = 'A';
        return BA_CTOR().newInstance(60, delays, remaining, target, curr, visual, blast);
    }

    private static boolean getBool(Object ba, String field) throws Exception {
        Field f = BA().getDeclaredField(field);
        f.setAccessible(true);
        return f.getBoolean(ba);
    }

    private static float getFloat(Object ba, String field) throws Exception {
        Field f = BA().getDeclaredField(field);
        f.setAccessible(true);
        return f.getFloat(ba);
    }

    private static int getInt(Object ba, String field) throws Exception {
        Field f = BA().getDeclaredField(field);
        f.setAccessible(true);
        return f.getInt(ba);
    }

    private static String getStr(Object ba, String field) throws Exception {
        Field f = BA().getDeclaredField(field);
        f.setAccessible(true);
        return (String) f.get(ba);
    }

    private static Class<?> CO() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object newOpts(String from) throws Exception {
        Class<?> co = CO();
        Constructor<?> ctor = co.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object o = ctor.newInstance();
        Field f = co.getDeclaredField("from");
        f.setAccessible(true);
        Class<?> enumClass = Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts$From");
        Object enumVal = Enum.valueOf((Class) enumClass, from); // "A" or "RAND"
        f.set(o, enumVal);
        return o;
    }

    private static void setCurrAt(Object ba, int i, char ch) throws Exception {
        char[] curr = (char[]) field(ba, "curr");
        curr[i] = ch;
    }

    private static char getCurrAt(Object ba, int i) throws Exception {
        return ((char[]) field(ba, "curr"))[i];
    }

    private static int[] getIntArr(Object ba, String field) throws Exception {
        Field f = BA().getDeclaredField(field);
        f.setAccessible(true);
        return (int[]) f.get(ba);
    }

    private static Class<?> visualClass() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim$Visual");
    }

    private static Object callStatic(String name) throws Exception {
        Method m = visualClass().getDeclaredMethod(name);
        m.setAccessible(true);
        return m.invoke(null);
    }

    private static Object call(Object target, String name) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name);
        m.setAccessible(true);
        return m.invoke(target);
    }

    private static String[] defaultHexes() throws Exception {
        Object d = callStatic("defaults");
        String defA = (String) call(d, "flashHexA");
        String defB = (String) call(d, "flashHexB");
        return new String[]{defA, defB};
    }

    static Stream<Arguments> flashCases() {
        try {
            String[] defs = defaultHexes();
            String defA = defs[0], defB = defs[1];
            return Stream.of(
                    of(null, "", defA, defB, true, 1.2f, 3.4f),   // A=null, B=""
                    of("", null, defA, defB, false, 7.8f, 0.1f),  // A="",   B=null
                    of(null, null, defA, defB, true, 0.6f, 18f),  // both null
                    of("", "", defA, defB, false, 2f, 5f),        // both empty
                    of("ABCDEF", "123456", "ABCDEF", "123456", true, 0.1f, 0.2f),
                    of("   ", "0", "   ", "0", false, 9f, 11f)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    static Stream<Arguments> spinDigitPosBranchCases() {
        return Stream.of(
                // idx, init curr[i], target t, label
                of(9, ' ', '3', "left-false (curr<'0')"),
                of(12, 'A', '4', "left-true/right-false (curr>'9')"),
                of(3, '5', '7', "both-true (curr in '0'..'9')")
        );
    }

    /**
     * Spinner wrap & category-guard matrix: exact wraps + “if-changed, stays-in-category” cases.
     */
    static Stream<Arguments> spinnerCases() {
        char last = PUNCT_RING[PUNCT_RING.length - 1];
        return Stream.of(
                // name,        idx, init,  t,    expectedExact,              expectedCategory
                of("spinLetter", 0, 'Z', 'Z', Character.valueOf('A'), null),
                of("spinLetter", 1, 'z', 'z', Character.valueOf('a'), null),
                of("spinDigit", 3, '9', '9', Character.valueOf('0'), null),
                of("spinPunct", 5, last, last, Character.valueOf(PUNCT_RING[0]), null),

                // “no-op or change but category-preserving”
                of("spinLetter", 2, 'Q', '_', null, "LETTER"),
                of("spinDigit", 4, '5', 'x', null, "DIGIT"),
                of("spinPunct", 6, '.', 'A', null, "PUNCT")
        );
    }

    static Stream<Arguments> spinLetterNormaliseCases() {
        return Stream.of(
                of(10, '@', 'A', "UPPER"), // below 'A'  -> normalise to upper ring
                of(11, '[', 'Z', "UPPER"), // above 'Z'
                of(12, '`', 'a', "LOWER"), // below 'a'
                of(13, '{', 'z', "LOWER")  // above 'z'
        );
    }

    static Stream<Arguments> currentStringCases() {
        return Stream.of(
                of("fast-nullFlash", false, true, 0, false),
                of("fast-zeroFlash", false, false, 0, false),
                of("slow-flash>0", false, false, 2, true),
                of("rainbow-ignores", true, true, 0, true)
        );
    }

    static Stream<Arguments> allZeroCases() {
        return Stream.of(
                of(null, true),
                of(new int[0], true),
                of(new int[]{0, 0}, true),
                of(new int[]{0, 1}, false)
        );
    }

    static Stream<Arguments> blastLengthGuardCases() {
        return Stream.of(
                // name,                 hasFlash, flashMismatch, hasOver, overMismatch, hasPost, postMismatch, expectThrow
                of("flash mismatch only", true, true, true, false, true, false, true),
                of("overshoot mismatch", false, false, true, true, true, false, true),
                of("postHold mismatch", true, false, true, false, true, true, true),
                of("all correct", true, false, true, false, true, false, false),
                of("all null", false, false, false, false, false, false, false)
        );
    }

    static Stream<Arguments> ctorInvalidCases() {
        return Stream.of(
                of("delays=null", (Executable) () -> {
                    int n = 2;
                    int[] remaining = new int[n];
                    char[] t = new char[n], c = new char[n];
                    Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
                    BA_CTOR().newInstance(60, null, remaining, t, c, vis, null);
                }),
                of("remaining=null", (Executable) () -> {
                    int n = 2;
                    int[] delays = new int[n];
                    char[] t = new char[n], c = new char[n];
                    Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
                    BA_CTOR().newInstance(60, delays, null, t, c, vis, null);
                }),
                of("target=null", (Executable) () -> {
                    int n = 2;
                    int[] delays = new int[n], remaining = new int[n];
                    char[] c = new char[n];
                    Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
                    BA_CTOR().newInstance(60, delays, remaining, null, c, vis, null);
                }),
                of("curr=null", (Executable) () -> {
                    int n = 2;
                    int[] delays = new int[n], remaining = new int[n];
                    char[] t = new char[n];
                    Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
                    BA_CTOR().newInstance(60, delays, remaining, t, null, vis, null);
                }),
                of("delays length != n", (Executable) () -> {
                    int n = 2;
                    int[] delays = new int[n + 1], remaining = new int[n];
                    char[] t = new char[n], c = new char[n];
                    Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
                    BA_CTOR().newInstance(60, delays, remaining, t, c, vis, null);
                }),
                of("overshoot length != n", (Executable) () -> {
                    int n = 2;
                    int[] delays = new int[n], remaining = new int[n];
                    char[] t = new char[n], c = new char[n];
                    int[] overshoot = new int[n + 1];
                    Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
                    Object blast = newBlast(null, overshoot, null);
                    BA_CTOR().newInstance(60, delays, remaining, t, c, vis, blast);
                }),
                of("postLockHold length != n", (Executable) () -> {
                    int n = 2;
                    int[] delays = new int[n], remaining = new int[n];
                    char[] t = new char[n], c = new char[n];
                    int[] postHold = new int[n + 1];
                    Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
                    Object blast = newBlast(null, null, postHold);
                    BA_CTOR().newInstance(60, delays, remaining, t, c, vis, blast);
                })
        );
    }

    @ParameterizedTest(name = "blast length guard → {0}")
    @MethodSource("blastLengthGuardCases")
    void ctor_blast_length_guard_matrix(String name,
                                        boolean hasFlash, boolean flashMismatch,
                                        boolean hasOver, boolean overMismatch,
                                        boolean hasPost, boolean postMismatch,
                                        boolean expectThrow) throws Exception {
        final int n = 5;

        int[] delays = new int[n], remaining = new int[n];
        char[] target = new char[n], curr = new char[n];
        Arrays.fill(target, 'A');
        Arrays.fill(curr, 'A');

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");

        int[] flashLeft = hasFlash ? new int[flashMismatch ? n + 1 : n] : null;
        int[] overshootLeft = hasOver ? new int[overMismatch ? n + 1 : n] : null;
        int[] postLockHold = hasPost ? new int[postMismatch ? n + 1 : n] : null;

        Object blast = newBlast(flashLeft, overshootLeft, postLockHold);

        Executable call = () -> BA_CTOR().newInstance(60, delays, remaining, target, curr, vis, blast);

        if (expectThrow) assertThrows(InvocationTargetException.class, call);
        else assertDoesNotThrow(call);
    }

    @ParameterizedTest(name = "spinDigit pos-branch: {3}")
    @MethodSource("spinDigitPosBranchCases")
    void spinDigit_pos_ternary_branches(int idx, char init, char t, String _case) throws Exception {
        Object ba = newBlockAnim(false);
        Method m = spinner("spinDigit");

        // Set up curr[i] explicitly to hit each branch
        setCurrAt(ba, idxWrap(idx), init);

        // Invoke; we don’t need to assert exact next char, just that
        // the spinner runs and (if it changes) it stays a digit.
        Character out = invokeSpinnerAndReadCurr(ba, m, idxWrap(idx), t);
        if (out != null) {
            assertTrue(Character.isDigit(out), "result should stay within digit ring");
        } else {
            // No change is fine; we still executed the line with the desired branch.
            assertEquals(init, getCurrAt(ba, idxWrap(idx)));
        }
    }

    @ParameterizedTest(name = "[{index}] inA={0}, inB={1} -> expA={2}, expB={3}")
    @MethodSource("flashCases")
    void parameterised_constructor_handles_fallbacks_and_primitives(
            String inA, String inB, String expA, String expB,
            boolean rainbow, float rhz, float rshift) throws Exception {

        Object v = newVisual(rainbow, rhz, rshift, inA, inB);

        assertEquals(expA, (String) call(v, "flashHexA"));
        assertEquals(expB, (String) call(v, "flashHexB"));
        assertEquals(rainbow, (Boolean) call(v, "rainbow"));
        assertEquals(rhz, (Float) call(v, "rhz"), 1e-6f);
        assertEquals(rshift, (Float) call(v, "rshift"), 1e-6f);
    }

    @ParameterizedTest(name = "spinner {0}: init='{2}', t='{3}'")
    @MethodSource("spinnerCases")
    void spinner_wrap_and_category_guard_matrix(String name, int idx, char init, char t,
                                                Character expectedExact, String expectedCategory) throws Exception {
        Object ba = newBlockAnim(false);
        Method m = spinner(name);

        setCurrAt(ba, idxWrap(idx), init);
        Character out = invokeSpinnerAndReadCurr(ba, m, idxWrap(idx), t);

        if (expectedExact != null) {
            assertNotNull(out, "expected a change");
            assertEquals(expectedExact.charValue(), out.charValue());
        } else {
            // Either no change OR change but staying in the ring/category.
            if (out == null) {
                assertEquals(init, getCurrAt(ba, idxWrap(idx)));
            } else {
                switch (expectedCategory) {
                    case "LETTER" -> assertTrue(Character.isLetter(out));
                    case "DIGIT" -> assertTrue(Character.isDigit(out));
                    case "PUNCT" -> assertTrue(PUNCT_SET.contains(out));
                    default -> fail("Unexpected category: " + expectedCategory);
                }
            }
        }
    }

    @ParameterizedTest(name = "spinLetter normalise idx={0} init='{1}' t='{2}' → {3}")
    @MethodSource("spinLetterNormaliseCases")
    void spinLetter_normalises_ranges(int idx, char init, char t, String mode) throws Exception {
        Object ba = newBlockAnim(false);
        Method m = spinner("spinLetter");

        setCurrAt(ba, idxWrap(idx), init);
        Character out = invokeSpinnerAndReadCurr(ba, m, idxWrap(idx), t);
        assertNotNull(out, "should normalise into the selected ring");
        if ("UPPER".equals(mode)) assertTrue(Character.isUpperCase(out));
        else assertTrue(Character.isLowerCase(out));
    }

    @ParameterizedTest(name = "currentString {0}")
    @MethodSource("currentStringCases")
    void currentString_paths(String name, boolean rainbow, boolean flashNull, int flash0, boolean expectMarkup) throws Exception {
        final int N = 8;
        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];
        char[] target = "ABCDEFGH".toCharArray();
        char[] curr = "ABCDEFGH".toCharArray();

        int[] flashLeft = flashNull ? null : new int[N];
        if (flashLeft != null) flashLeft[0] = flash0;

        Object vis = newVisual(rainbow, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, postLockHold);
        Object ba = BA_CTOR().newInstance(60, delays, remaining, target, curr, vis, blast);

        Method curStr = BA().getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);

        boolean hasTags = COLOR_TAG.matcher(s).find();
        assertEquals(expectMarkup, hasTags, "markup presence");
        if (!expectMarkup) assertEquals("ABCDEFGH", s);
    }

    @ParameterizedTest(name = "allZero({0}) = {1}")
    @MethodSource("allZeroCases")
    void allZero_matrix(int[] arr, boolean expected) throws Exception {
        Object ba = newBlockAnim(false);
        Method az = BA().getDeclaredMethod("allZero", int[].class);
        az.setAccessible(true);
        boolean got = (Boolean) az.invoke(ba, (Object) arr);
        assertEquals(expected, got);
    }

    @ParameterizedTest(name = "ctor invalid → {0}")
    @MethodSource("ctorInvalidCases")
    void ctor_invalid_throws(String name, Executable call) {
        assertThrows(InvocationTargetException.class, call);
    }

    @Test
    void blockAnim_uses_defaults_when_visual_is_null() throws Exception {
        Object blast = newBlast(null, new int[2], new int[2]);
        Object ba = newBA(null, blast, 2);

        assertFalse(getBool(ba, "rainbow"));
        assertEquals(0.6f, getFloat(ba, "rhz"), 1e-6);
        assertEquals(18f, getFloat(ba, "rshift"), 1e-6);
        assertEquals("ffffff", getStr(ba, "flashHexA"));
        assertEquals("ffe066", getStr(ba, "flashHexB"));
    }

    @Test
    void visual_ctor_applies_default_hex_when_null_or_empty() throws Exception {
        Object vis = newVisual(true, 1.2f, 33f, null, "");
        Object blast = newBlast(null, new int[1], new int[1]);
        Object ba = newBA(vis, blast, 1);

        assertTrue(getBool(ba, "rainbow"));
        assertEquals(1.2f, getFloat(ba, "rhz"), 1e-6);
        assertEquals(33f, getFloat(ba, "rshift"), 1e-6);
        assertEquals("ffffff", getStr(ba, "flashHexA"));
        assertEquals("ffe066", getStr(ba, "flashHexB"));
    }

    @Test
    void visual_ctor_keeps_nonEmpty_hex_values() throws Exception {
        Object vis = newVisual(false, 0.5f, 10f, "123abc", "ABCDEF");
        Object blast = newBlast(null, new int[1], new int[1]);
        Object ba = newBA(vis, blast, 1);

        assertFalse(getBool(ba, "rainbow"));
        assertEquals(0.5f, getFloat(ba, "rhz"), 1e-6);
        assertEquals(10f, getFloat(ba, "rshift"), 1e-6);
        assertEquals("123abc", getStr(ba, "flashHexA"));
        assertEquals("abcdef", getStr(ba, "flashHexB").toLowerCase());
    }

    @Test
    void appendEscaped_escapes_bracket_and_passes_others() throws Exception {
        Method m = BA().getDeclaredMethod("appendEscaped", StringBuilder.class, char.class);
        m.setAccessible(true);

        StringBuilder sb1 = new StringBuilder();
        m.invoke(null, sb1, '[');
        assertEquals("[[", sb1.toString());

        StringBuilder sb2 = new StringBuilder();
        m.invoke(null, sb2, 'X');
        assertEquals("X", sb2.toString());
    }

    @Test
    void seedHelpers_cover_rand_and_fixed_variants() throws Exception {
        Object a = newOpts("A");
        Object rand = newOpts("RAND");

        Method seedLetter = BA().getDeclaredMethod("seedLetter", char.class, CO());
        Method seedDigit = BA().getDeclaredMethod("seedDigit", CO());
        Method seedPunct = BA().getDeclaredMethod("seedPunct", CO());
        seedLetter.setAccessible(true);
        seedDigit.setAccessible(true);
        seedPunct.setAccessible(true);

        char lAUp = (char) seedLetter.invoke(null, 'Z', a);
        char lALow = (char) seedLetter.invoke(null, 'z', a);
        char lRUp = (char) seedLetter.invoke(null, 'Z', rand);
        char lRLow = (char) seedLetter.invoke(null, 'z', rand);
        assertTrue(Character.isUpperCase(lAUp));
        assertTrue(Character.isLowerCase(lALow));
        assertTrue(Character.isUpperCase(lRUp));
        assertTrue(Character.isLowerCase(lRLow));

        char dA = (char) seedDigit.invoke(null, a);
        char dR = (char) seedDigit.invoke(null, rand);
        assertTrue(Character.isDigit(dA));
        assertTrue(Character.isDigit(dR));

        char pA = (char) seedPunct.invoke(null, a);
        char pR = (char) seedPunct.invoke(null, rand);
        assertTrue(PUNCT_SET.contains(pA));
        assertTrue(PUNCT_SET.contains(pR));
    }

    @Test
    void stepFrame_invokes_letter_digit_punct_spinners() throws Exception {
        Class<?> bA = BA();
        Constructor<?> c = BA_CTOR();

        final int N = 4;
        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];

        // Force a spin on each of the first 3 slots
        remaining[0] = 1; // letter
        remaining[1] = 1; // digit
        remaining[2] = 1; // punct

        char[] target = new char[]{'A', '5', '.', ' '};
        char[] curr = new char[N]; // zeros -> spinner init path

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(null, overshootLeft, postLockHold);
        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method step = bA.getDeclaredMethod("stepFrame");
        step.setAccessible(true);
        step.invoke(ba);

        Method curStr = bA.getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);

        assertEquals(4, s.length());
        assertTrue(Character.isLetter(s.charAt(0)), "slot 0 should be a letter after spin");
        assertTrue(Character.isDigit(s.charAt(1)), "slot 1 should be a digit after spin");
        assertTrue(PUNCT_SET.contains(s.charAt(2)), "slot 2 should be a punctuation");
        assertEquals(' ', s.charAt(3));
    }

    @Test
    void handleFlash_sets_postLockHold_when_flashEnds_and_array_present() throws Exception {
        Constructor<?> c = BA_CTOR();
        int[] delays = new int[1], remaining = new int[1];
        char[] target = new char[]{'A'};
        char[] curr = new char[]{'_'};
        int[] flashLeft = new int[]{1};
        int[] overshootLeft = new int[1];
        int[] postLockHold = new int[1];

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, postLockHold);
        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method handleFlash = BA().getDeclaredMethod("handleFlash", int.class, char.class);
        handleFlash.setAccessible(true);

        boolean consumed = (Boolean) handleFlash.invoke(ba, 0, 'X');
        assertTrue(consumed);

        assertEquals(0, getIntArr(ba, "flashLeft")[0]);
        assertEquals(1, getIntArr(ba, "postLockHold")[0]);
        assertEquals('X', ((char[]) field(ba, "curr"))[0]);
    }

    @Test
    void handleFlash_does_not_set_postLockHold_when_array_is_null() throws Exception {
        Constructor<?> c = BA_CTOR();
        int[] delays = new int[1], remaining = new int[1];
        char[] target = new char[]{'A'};
        char[] curr = new char[]{'_'};
        int[] flashLeft = new int[]{1};
        int[] overshootLeft = new int[1];

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, null); // postLockHold null
        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method handleFlash = BA().getDeclaredMethod("handleFlash", int.class, char.class);
        handleFlash.setAccessible(true);

        boolean consumed = (Boolean) handleFlash.invoke(ba, 0, 'Y');
        assertTrue(consumed);

        assertEquals(0, getIntArr(ba, "flashLeft")[0]);
        assertEquals('Y', ((char[]) field(ba, "curr"))[0]);
    }

    @Test
    void handleFlash_returns_false_when_no_pending_flash() throws Exception {
        Constructor<?> c = BA_CTOR();
        int[] delays = new int[1], remaining = new int[1];
        char[] target = new char[]{'A'};
        char[] curr = new char[]{'_'};
        int[] flashLeft = new int[]{0};  // none pending
        int[] overshootLeft = new int[1];
        int[] postHold = new int[1];

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, postHold);
        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method handleFlash = BA().getDeclaredMethod("handleFlash", int.class, char.class);
        handleFlash.setAccessible(true);

        boolean consumed = (Boolean) handleFlash.invoke(ba, 0, 'Z');
        assertFalse(consumed);
        assertEquals('_', ((char[]) field(ba, "curr"))[0]);
    }

    @Test
    void blockAnim_ctor_fpsClamp_and_arrayValidation_positive_fps_clamp() throws Exception {
        // fps clamp (0 -> 1)
        int n = 2;
        int[] delays = new int[n], remaining = new int[n];
        char[] target = new char[n], curr = new char[n];
        for (int i = 0; i < n; i++) target[i] = curr[i] = 'A';
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object ba = BA_CTOR().newInstance(0, delays, remaining, target, curr, vis, null);
        assertEquals(1, getInt(ba, "fps"));
    }

    @Test
    void ctor_throws_when_curr_length_mismatch() throws Exception {
        Constructor<?> c = BA_CTOR();
        int n = 2;
        int[] delays = new int[n], remaining = new int[n];
        char[] target = new char[n];
        char[] curr = new char[n + 1]; // mismatch
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        assertThrows(InvocationTargetException.class,
                () -> c.newInstance(60, delays, remaining, target, curr, vis, null));
    }

    @Test
    void ctor_throws_when_remaining_length_mismatch() throws Exception {
        Constructor<?> c = BA_CTOR();
        int n = 2;
        int[] delays = new int[n], remaining = new int[n + 1]; // mismatch
        char[] target = new char[n], curr = new char[n];
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        assertThrows(InvocationTargetException.class,
                () -> c.newInstance(60, delays, remaining, target, curr, vis, null));
    }

    @Test
    void ctor_all_blast_arrays_null_is_ok() throws Exception {
        Constructor<?> c = BA_CTOR();
        int n = 2;
        int[] delays = new int[n], remaining = new int[n];
        char[] target = new char[n], curr = new char[n];
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(null, null, null); // allowed (feature off)
        assertDoesNotThrow(() -> c.newInstance(60, delays, remaining, target, curr, vis, blast));
    }
}
