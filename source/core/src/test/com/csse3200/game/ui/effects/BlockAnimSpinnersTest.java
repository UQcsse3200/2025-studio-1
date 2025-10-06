package com.csse3200.game.ui.effects;

import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coverage-focused tests for TextEffects.BlockAnim private spinner helpers and currentString()
 * (updated for 7-arg BlockAnim ctor with Visual and BlastExtras)
 */
class BlockAnimSpinnersTest {

    // Mirror TextEffects.PUNCT_RING for invariants
    private static final char[] PUNCT_RING = (
            ".,;:!?-_/\\|@#$%^&*()[]{}<>'\"+=~`"
    ).toCharArray();
    private static final Set<Character> PUNCT_SET = toSet();
    private static final int SAFE_LEN = 128;
    private static final Pattern COLOR_TAG = Pattern.compile("\\[#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})]");

    private static Set<Character> toSet() {
        var s = new HashSet<Character>();
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
        var ctor = BA().getDeclaredConstructor(
                int.class, int[].class, int[].class, char[].class, char[].class,
                BAVIS(), BABLAST()
        );
        ctor.setAccessible(true);
        return ctor;
    }

    private static Object newVisual(boolean rainbow, float rhz, float rshift, String hexA, String hexB) throws Exception {
        var ctor = BAVIS().getDeclaredConstructor(boolean.class, float.class, float.class, String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(rainbow, rhz, rshift, hexA, hexB);
    }

    private static Object newBlast(int[] flashLeft, int[] overshootLeft, int[] postLockHold) throws Exception {
        var ctor = BABLAST().getDeclaredConstructor(int[].class, int[].class, int[].class);
        ctor.setAccessible(true);
        return ctor.newInstance(flashLeft, overshootLeft, postLockHold);
    }

    private static Object field(Object inst, String name) throws Exception {
        Field f = BA().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(inst);
    }

    // *** ADDED: simple wrapper for index normalisation
    private static int idxWrap(int idx) {
        return Math.floorMod(idx, SAFE_LEN);
    }

    private static Method spinner(String name) throws Exception {
        Class<?> cls = BA();
        // Prefer (int,char), but fall back to (int)
        for (Method m : cls.getDeclaredMethods()) {
            if (!m.getName().equals(name)) continue;
            Class<?>[] pt = m.getParameterTypes();
            if (pt.length == 2 && pt[0] == int.class && pt[1] == char.class) {
                m.setAccessible(true);
                return m;
            }
        }
        for (Method m : cls.getDeclaredMethods()) {
            if (!m.getName().equals(name)) continue;
            Class<?>[] pt = m.getParameterTypes();
            if (pt.length == 1 && pt[0] == int.class) {
                m.setAccessible(true);
                return m;
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
        var ctor = BA_CTOR();
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

    // *** ADDED: small helpers used by Visual/Blast coverage tests
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


    // ---------- currentString() paths ----------

    private static Class<?> CO() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts");
    }

    private static Class<?> FROM() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts$From");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object newOpts(String from) throws Exception {
        Class<?> co = Class.forName("com.csse3200.game.ui.effects.TextEffects$CrazyOpts");
        Constructor<?> ctor = co.getDeclaredConstructor();
        ctor.setAccessible(true);                          // <-- key line
        Object o = ctor.newInstance();

        Field f = co.getDeclaredField("from");
        f.setAccessible(true);
        Class<?> enumClass = Class.forName(
                "com.csse3200.game.ui.effects.TextEffects$CrazyOpts$From"
        );
        Object enumVal = Enum.valueOf((Class) enumClass, from); // "A" or "RAND"
        f.set(o, enumVal);
        return o;
    }

    private static void setCurrAt(Object ba, int i, char ch) throws Exception {
        char[] curr = (char[]) field(ba, "curr");
        curr[i] = ch; // same array instance is retained inside BlockAnim
    }

    // ---------- spinner helpers (private) ----------

    private static char getCurrAt(Object ba, int i) throws Exception {
        return ((char[]) field(ba, "curr"))[i];
    }

    private static int[] getIntArr(Object ba, String field) throws Exception {
        Field f = BA().getDeclaredField(field);
        f.setAccessible(true);
        return (int[]) f.get(ba);
    }

    @Test
    void currentString_fastPath_allZero_true_returns_plain_string() throws Exception {
        Class<?> bA = BA();
        Constructor<?> c = BA_CTOR();

        final int N = 8;
        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] flashLeft = new int[N]; // all zeros
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];

        char[] target = "ABCDEFGH".toCharArray();
        char[] curr = "ABCDEFGH".toCharArray();

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, postLockHold);

        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method curStr = bA.getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);

        assertEquals("ABCDEFGH", s);
        assertFalse(s.contains("[#"), "fast path must NOT inject markup");
    }

    @Test
    void currentString_slowPath_flash_nonZero_adds_markup() throws Exception {
        Class<?> bA = BA();
        Constructor<?> c = BA_CTOR();

        final int N = 8;
        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] flashLeft = new int[N];
        flashLeft[0] = 2; // non-zero -> slow path
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];

        char[] target = "ABCDEFGH".toCharArray();
        char[] curr = "ABCDEFGH".toCharArray();

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, postLockHold);

        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method curStr = bA.getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);

        assertNotEquals("ABCDEFGH", s);
        assertTrue(COLOR_TAG.matcher(s).find(), "slow path should inject color tags");
    }

    @Test
    void currentString_rainbow_path_adds_markup_even_if_flash_null() throws Exception {
        Object ba = newBlockAnim(true);
        Method cs = BA().getDeclaredMethod("currentString");
        cs.setAccessible(true);
        String s = (String) cs.invoke(ba);
        assertTrue(COLOR_TAG.matcher(s).find(), "rainbow path should add per-char color tags");
    }

    @Test
    void currentString_fastPath_allZero_null_returns_plain_string() throws Exception {
        Class<?> bA = BA();
        Constructor<?> c = BA_CTOR();

        final int N = 8;

        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];

        char[] target = "ABCDEFGH".toCharArray();
        char[] curr = "ABCDEFGH".toCharArray();

        // flashLeft == null via BlastExtras(null, ...)
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(null, overshootLeft, postLockHold);

        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method curStr = bA.getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);

        assertEquals("ABCDEFGH", s);
        assertFalse(s.contains("[#"), "fast path with flashLeft=null must NOT inject markup");
    }

    @Test
    void spinDigit_digit_and_nonDigit_with_wrapped_indices() throws Exception {
        Object ba = newBlockAnim(false);
        Method spinDigit = spinner("spinDigit");

        // Non-digit input: direct call may still advance; if it changes, it must be a digit.
        Character outNon = invokeSpinnerAndReadCurr(ba, spinDigit, idxWrap(9), 'x');
        if (outNon != null) assertTrue(Character.isDigit(outNon));

        // Digits -> should remain digits (if a change occurs)
        Character d0 = invokeSpinnerAndReadCurr(ba, spinDigit, idxWrap(0), '0');
        Character dBig = invokeSpinnerAndReadCurr(ba, spinDigit, idxWrap(37), '3');
        Character dNeg = invokeSpinnerAndReadCurr(ba, spinDigit, idxWrap(-11), '9');
        if (d0 != null) assertTrue(Character.isDigit(d0));
        if (dBig != null) assertTrue(Character.isDigit(dBig));
        if (dNeg != null) assertTrue(Character.isDigit(dNeg));
    }

    @Test
    void spinLetter_upper_lower_and_nonLetter_with_wrapped_indices() throws Exception {
        Object ba = newBlockAnim(false);
        Method spinLetter = spinner("spinLetter");

        // Non-letter input: direct call may still advance; if it changes, it must be a letter.
        Character outNon = invokeSpinnerAndReadCurr(ba, spinLetter, 9, '_'); // slot 9 is space in seed
        if (outNon != null) assertTrue(Character.isLetter(outNon));

        // Uppercase: use slots that already hold letters (0..7)
        Character up0 = invokeSpinnerAndReadCurr(ba, spinLetter, 0, 'A');
        Character up5 = invokeSpinnerAndReadCurr(ba, spinLetter, 5, 'M');
        Character up7 = invokeSpinnerAndReadCurr(ba, spinLetter, 7, 'Z');
        if (up0 != null) assertTrue(Character.isUpperCase(up0));
        if (up5 != null) assertTrue(Character.isUpperCase(up5));
        if (up7 != null) assertTrue(Character.isUpperCase(up7));

        // Lowercase
        Character lo0 = invokeSpinnerAndReadCurr(ba, spinLetter, 0, 'a');
        Character lo5 = invokeSpinnerAndReadCurr(ba, spinLetter, 5, 'n');
        Character lo7 = invokeSpinnerAndReadCurr(ba, spinLetter, 7, 'z');
        if (lo0 != null) assertTrue(Character.isLowerCase(lo0));
        if (lo5 != null) assertTrue(Character.isLowerCase(lo5));
        if (lo7 != null) assertTrue(Character.isLowerCase(lo7));
    }

    @Test
    void spinPunct_member_and_nonMember_with_wrapped_indices() throws Exception {
        Object ba = newBlockAnim(false);
        Method spinPunct = spinner("spinPunct");

        // Non-member & space: implementation may advance; if it changes, ensure it's in ring
        Character outNon = invokeSpinnerAndReadCurr(ba, spinPunct, idxWrap(5), 'A');
        if (outNon != null) assertTrue(PUNCT_SET.contains(outNon));

        Character outSpace = invokeSpinnerAndReadCurr(ba, spinPunct, idxWrap(5), ' ');
        if (outSpace != null) assertTrue(PUNCT_SET.contains(outSpace));

        // Members -> should remain within ring after spin
        Character out0 = invokeSpinnerAndReadCurr(ba, spinPunct, idxWrap(0), '.');
        Character outF = invokeSpinnerAndReadCurr(ba, spinPunct, idxWrap(123), '!');
        Character outBk = invokeSpinnerAndReadCurr(ba, spinPunct, idxWrap(-3), '`');
        if (out0 != null) assertTrue(PUNCT_SET.contains(out0));
        if (outF != null) assertTrue(PUNCT_SET.contains(outF));
        if (outBk != null) assertTrue(PUNCT_SET.contains(outBk));
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
        char cLetter = s.charAt(0);
        char cDigit = s.charAt(1);
        char cPunct = s.charAt(2);
        char cSpace = s.charAt(3);

        assertTrue(Character.isLetter(cLetter), "slot 0 should be a letter after spin");
        assertTrue(Character.isDigit(cDigit), "slot 1 should be a digit after spin");
        assertTrue(PUNCT_SET.contains(cPunct), "slot 2 should be a punctuation from PUNCT_RING");
        assertEquals(' ', cSpace);
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
    void allZero_null_empty_zeros_and_nonzero() throws Exception {
        Object ba = newBlockAnim(false);
        Method az = BA().getDeclaredMethod("allZero", int[].class);
        az.setAccessible(true);

        assertTrue((Boolean) az.invoke(ba, (Object) null));
        assertTrue((Boolean) az.invoke(ba, (Object) new int[0]));
        assertTrue((Boolean) az.invoke(ba, (Object) new int[]{0, 0}));
        assertFalse((Boolean) az.invoke(ba, (Object) new int[]{0, 1}));
    }

    @Test
    void blockAnim_ctor_fpsClamp_and_arrayValidation() throws Exception {
        // base arrays
        int n = 2;
        int[] delays = new int[n], remaining = new int[n];
        char[] target = new char[n], curr = new char[n];
        for (int i = 0; i < n; i++) target[i] = curr[i] = 'A';

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");

        // 1) fps clamp (0 -> 1)
        Object ba = BA_CTOR().newInstance(0, delays, remaining, target, curr, vis, null);
        assertEquals(1, getInt(ba, "fps"));

        // 2) null required arrays -> throws
        assertThrows(InvocationTargetException.class,
                () -> BA_CTOR().newInstance(60, null, remaining, target, curr, vis, null));
        assertThrows(InvocationTargetException.class,
                () -> BA_CTOR().newInstance(60, delays, null, target, curr, vis, null));
        assertThrows(InvocationTargetException.class,
                () -> BA_CTOR().newInstance(60, delays, remaining, null, curr, vis, null));
        assertThrows(InvocationTargetException.class,
                () -> BA_CTOR().newInstance(60, delays, remaining, target, null, vis, null));

        // 3) length mismatch -> throws
        assertThrows(InvocationTargetException.class, () -> {
            int[] delaysBad = new int[n + 1];
            BA_CTOR().newInstance(60, delaysBad, remaining, target, curr, vis, null);
        });

        // 4) BlastExtras length mismatch -> throws when any non-null length != n
        int[] flashLeft = new int[n - 1];       // wrong
        int[] overshootLeft = new int[n];     // ok
        int[] postLockHold = new int[n];     // ok
        Object blastBad = newBlast(flashLeft, overshootLeft, postLockHold);
        assertThrows(InvocationTargetException.class,
                () -> BA_CTOR().newInstance(60, delays, remaining, target, curr, vis, blastBad));
    }

    @Test
    void spinLetter_wraps_Z_to_A_and_z_to_a_and_noop_on_non_letter() throws Exception {
        Object ba = newBlockAnim(false);
        Method m = spinner("spinLetter");

        // wrap: 'Z' -> 'A'
        setCurrAt(ba, 0, 'Z');
        Character upWrap = invokeSpinnerAndReadCurr(ba, m, 0, 'Z');
        assertNotNull(upWrap);
        assertEquals('A', upWrap.charValue());

        // wrap: 'z' -> 'a'
        setCurrAt(ba, 1, 'z');
        Character loWrap = invokeSpinnerAndReadCurr(ba, m, 1, 'z');
        assertNotNull(loWrap);
        assertEquals('a', loWrap.charValue());

        // guard/no-op on non-letter
        setCurrAt(ba, 2, 'Q');
        Character non = invokeSpinnerAndReadCurr(ba, m, 2, '_'); // non-letter input
        assertNull(non); // If you didn't add guards, replace with:
        // if (non != null) assertTrue(Character.isLetter(non));
        assertEquals('Q', getCurrAt(ba, 2));
    }

    @Test
    void spinDigit_wraps_9_to_0_and_noop_on_non_digit() throws Exception {
        Object ba = newBlockAnim(false);
        Method m = spinner("spinDigit");

        // wrap: '9' -> '0'
        setCurrAt(ba, 3, '9');
        Character wrap = invokeSpinnerAndReadCurr(ba, m, 3, '9');
        assertNotNull(wrap);
        assertEquals('0', wrap.charValue());

        // guard/no-op on non-digit
        setCurrAt(ba, 4, '5');
        Character non = invokeSpinnerAndReadCurr(ba, m, 4, 'x'); // non-digit input
        assertNull(non); // If no guards in impl: if (non != null) assertTrue(Character.isDigit(non));
        assertEquals('5', getCurrAt(ba, 4));
    }

    @Test
    void spinPunct_wraps_last_to_first_and_noop_on_non_punct() throws Exception {
        Object ba = newBlockAnim(false);
        Method m = spinner("spinPunct");

        // wrap: last ring char -> first ring char
        char last = PUNCT_RING[PUNCT_RING.length - 1];
        setCurrAt(ba, 5, last);
        Character wrap = invokeSpinnerAndReadCurr(ba, m, 5, last);
        assertNotNull(wrap);
        assertEquals(PUNCT_RING[0], wrap.charValue());

        // guard/no-op on non-punct input
        setCurrAt(ba, 6, '.');
        Character non = invokeSpinnerAndReadCurr(ba, m, 6, 'A'); // non-punct input
        assertNull(non); // If no guards in impl: if (non != null) assertTrue(PUNCT_SET.contains(non));
        assertEquals('.', getCurrAt(ba, 6));
    }

    @Test
    void spinLetter_normalizes_when_curr_below_or_above_uppercase_range() throws Exception {
        Object ba = newBlockAnim(false);
        Method spinLetter = spinner("spinLetter");

        // BELOW: '@' (64) < 'A' (65) triggers the left side (c < base)
        setCurrAt(ba, 10, '@');
        Character r1 = invokeSpinnerAndReadCurr(ba, spinLetter, 10, 'A'); // t uppercase selects 'A'..'Z' ring
        assertNotNull(r1);
        assertTrue(Character.isUpperCase(r1));

        // ABOVE: '[' (91) > 'Z' (90) triggers the right side (c > upper)
        setCurrAt(ba, 11, '[');
        Character r2 = invokeSpinnerAndReadCurr(ba, spinLetter, 11, 'Z');
        assertNotNull(r2);
        assertTrue(Character.isUpperCase(r2));
    }

    @Test
    void spinLetter_normalizes_when_curr_below_or_above_lowercase_range() throws Exception {
        Object ba = newBlockAnim(false);
        Method spinLetter = spinner("spinLetter");

        // BELOW: '`' (96) < 'a' (97)
        setCurrAt(ba, 12, '`');
        Character r1 = invokeSpinnerAndReadCurr(ba, spinLetter, 12, 'a'); // t lowercase selects 'a'..'z' ring
        assertNotNull(r1);
        assertTrue(Character.isLowerCase(r1));

        // ABOVE: '{' (123) > 'z' (122)
        setCurrAt(ba, 13, '{');
        Character r2 = invokeSpinnerAndReadCurr(ba, spinLetter, 13, 'z');
        assertNotNull(r2);
        assertTrue(Character.isLowerCase(r2));
    }

    @Test
    void handleFlash_sets_postLockHold_when_flashEnds_and_array_present() throws Exception {
        // Build BA with flashLeft[0]=1 and postLockHold[0]=0
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

        // act: one call should bring flashLeft[0] from 1 -> 0 and set postLockHold[0] = 1
        boolean consumed = (Boolean) handleFlash.invoke(ba, 0, 'X');
        assertTrue(consumed);

        assertEquals(0, getIntArr(ba, "flashLeft")[0], "flashLeft must be decremented to 0");
        assertEquals(1, getIntArr(ba, "postLockHold")[0], "postLockHold must be set to 1 when flash ends");
        assertEquals('X', ((char[]) field(ba, "curr"))[0], "curr should show the real glyph during flash");
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
        // postLockHold == null here
        Object blast = newBlast(flashLeft, overshootLeft, null);
        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method handleFlash = BA().getDeclaredMethod("handleFlash", int.class, char.class);
        handleFlash.setAccessible(true);

        boolean consumed = (Boolean) handleFlash.invoke(ba, 0, 'Y');
        assertTrue(consumed);

        assertEquals(0, getIntArr(ba, "flashLeft")[0], "flashLeft must still decrement to 0");
        // No NPE and nothing to assert for postLockHold since it's null
        assertEquals('Y', ((char[]) field(ba, "curr"))[0]);
    }

    @Test
    void handleFlash_returns_false_when_no_pending_flash() throws Exception {
        Constructor<?> c = BA_CTOR();
        int[] delays = new int[1], remaining = new int[1];
        char[] target = new char[]{'A'};
        char[] curr = new char[]{'_'};
        int[] flashLeft = new int[]{0};  // no pending
        int[] overshootLeft = new int[1];
        int[] postHold = new int[1];

        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, postHold);
        Object ba = c.newInstance(60, delays, remaining, target, curr, vis, blast);

        Method handleFlash = BA().getDeclaredMethod("handleFlash", int.class, char.class);
        handleFlash.setAccessible(true);

        boolean consumed = (Boolean) handleFlash.invoke(ba, 0, 'Z');
        assertFalse(consumed, "Should return false when !hasPending(flashLeft, i)");
        assertEquals('_', ((char[]) field(ba, "curr"))[0], "curr should remain unchanged when no flash pending");
    }

    @Test
    void ctor_throws_when_curr_length_mismatch() throws Exception {
        var c = BA_CTOR();
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
        var c = BA_CTOR();
        int n = 2;
        int[] delays = new int[n], remaining = new int[n + 1]; // mismatch
        char[] target = new char[n], curr = new char[n];
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        assertThrows(InvocationTargetException.class,
                () -> c.newInstance(60, delays, remaining, target, curr, vis, null));
    }

    @Test
    void ctor_all_blast_arrays_null_is_ok() throws Exception {
        var c = BA_CTOR();
        int n = 2;
        int[] delays = new int[n], remaining = new int[n];
        char[] target = new char[n], curr = new char[n];
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(null, null, null); // allowed (feature off)
        assertDoesNotThrow(() -> c.newInstance(60, delays, remaining, target, curr, vis, blast));
    }

    @Test
    void ctor_throws_when_overshoot_length_mismatch() throws Exception {
        var c = BA_CTOR();
        int n = 2;
        int[] delays = new int[n], remaining = new int[n];
        char[] target = new char[n], curr = new char[n];
        int[] flashLeft = null;
        int[] overshootLeft = new int[n + 1]; // mismatch
        int[] postLockHold = null;
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, postLockHold);
        assertThrows(InvocationTargetException.class,
                () -> c.newInstance(60, delays, remaining, target, curr, vis, blast));
    }

    @Test
    void ctor_throws_when_postLockHold_length_mismatch() throws Exception {
        var c = BA_CTOR();
        int n = 2;
        int[] delays = new int[n], remaining = new int[n];
        char[] target = new char[n], curr = new char[n];
        int[] flashLeft = null;
        int[] overshootLeft = null;
        int[] postLockHold = new int[n + 1]; // mismatch
        Object vis = newVisual(false, 0.6f, 18f, "ffffff", "ffe066");
        Object blast = newBlast(flashLeft, overshootLeft, postLockHold);
        assertThrows(InvocationTargetException.class,
                () -> c.newInstance(60, delays, remaining, target, curr, vis, blast));
    }

}
