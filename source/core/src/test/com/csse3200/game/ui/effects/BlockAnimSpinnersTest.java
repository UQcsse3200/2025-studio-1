package com.csse3200.game.ui.effects;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coverage-focused tests for TextEffects.BlockAnim private spinner helpers and currentString()
 */
public class BlockAnimSpinnersTest {

    // Mirror TextEffects.PUNCT_RING for invariants
    private static final char[] PUNCT_RING = (
            ".,;:!?-_/\\|@#$%^&*()[]{}<>'\"+=~`"
    ).toCharArray();
    private static final Set<Character> PUNCT_SET = toSet(PUNCT_RING);
    private static final int SAFE_LEN = 128;
    private static final Pattern COLOR_TAG = Pattern.compile("\\[#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})\\]");
    // helper used above (mirror of code ring)
    private static final char[] RING = ".,;:!?-_/\\|@#$%^&*()[]{}<>'\"+=~`".toCharArray();

    private static Set<Character> toSet(char[] arr) {
        var s = new HashSet<Character>();
        for (char c : arr) s.add(c);
        return s;
    }

    private static Class<?> BA() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim");
    }

    private static Object newBlockAnim(
            boolean rainbow,
            int[] flashLeft // may be null; if non-null length must be SAFE_LEN; non-zero at slot 0 forces slow-path flash
    ) throws Exception {
        var ctor = BA().getDeclaredConstructor(
                int.class, int[].class, int[].class, char[].class, char[].class,
                boolean.class, float.class, float.class,
                int[].class, int[].class, int[].class,
                String.class, String.class
        );
        ctor.setAccessible(true);

        int[] delays = new int[SAFE_LEN];
        int[] remaining = new int[SAFE_LEN];
        int[] overshootLeft = new int[SAFE_LEN];
        int[] postLockHold = new int[SAFE_LEN];

        // target/curr small or large: we’ll use SAFE_LEN so any idxWrap fits
        char[] target = new char[SAFE_LEN];
        char[] curr = new char[SAFE_LEN];

        // Seed predictable characters: first 8 letters A..H, rest spaces
        for (int i = 0; i < SAFE_LEN; i++) {
            char ch = (i < 8) ? (char) ('A' + i) : ' ';
            target[i] = ch;
            curr[i] = ch;
        }

        return ctor.newInstance(
                60, delays, remaining, target, curr,
                rainbow, 0.6f, 18f,
                flashLeft, overshootLeft, postLockHold,
                "ffffff", "ffe066"
        );
    }

    private static int idxWrap(int idx) {
        return Math.floorMod(idx, SAFE_LEN);
    }

    private static Method m(String name) throws Exception {
        Method m = BA().getDeclaredMethod(name, int.class, char.class);
        m.setAccessible(true);
        return m;
    }

    // ---------- currentString() fast/slow/rainbow ----------

    private static Character call(Object inst, Method m, int i, char ch) throws Exception {
        return (Character) m.invoke(Modifier.isStatic(m.getModifiers()) ? null : inst, i, ch);
    }

    private static boolean isPunct(char c) {
        for (char p : RING) if (p == c) return true;
        return false;
    }

    @Test
    void currentString_fastPath_allZero_true_returns_plain_string() throws Exception {
        // --- reflect the BlockAnim 13-arg constructor ---
        Class<?> BA = Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim");
        Constructor<?> c = BA.getDeclaredConstructor(
                int.class, int[].class, int[].class, char[].class, char[].class,
                boolean.class, float.class, float.class,
                int[].class, int[].class, int[].class,
                String.class, String.class
        );
        c.setAccessible(true);

        // small buffers are fine; we won’t index out of bounds
        final int N = 8;

        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] flashLeft = new int[N];    // <-- all zeros -> allZero(flashLeft) == true
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];

        // give curr/target deterministic plain content
        char[] target = "ABCDEFGH".toCharArray();
        char[] curr = "ABCDEFGH".toCharArray();

        // rainbow=false so fast-path is eligible; flashLeft is zeroed
        Object ba = c.newInstance(
                60, delays, remaining, target, curr,
                false, 0.6f, 18f,
                flashLeft, overshootLeft, postLockHold,
                "ffffff", "ffe066"
        );

        // call currentString()
        Method curStr = BA.getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);

        // FAST-PATH: should return raw curr (no color tags)
        assertEquals("ABCDEFGH", s);
        assertFalse(s.contains("[#"), "fast path must NOT inject markup");
    }


    // ---------- spinner helpers (private) ----------

    @Test
    void currentString_slowPath_allZero_false_adds_markup() throws Exception {
        Class<?> BA = Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim");
        Constructor<?> c = BA.getDeclaredConstructor(
                int.class, int[].class, int[].class, char[].class, char[].class,
                boolean.class, float.class, float.class,
                int[].class, int[].class, int[].class,
                String.class, String.class
        );
        c.setAccessible(true);

        final int N = 8;
        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] flashLeft = new int[N];
        flashLeft[0] = 2;                 // <-- non-zero so allZero == false
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];

        char[] target = "ABCDEFGH".toCharArray();
        char[] curr = "ABCDEFGH".toCharArray();

        Object ba = c.newInstance(
                60, delays, remaining, target, curr,
                false, 0.6f, 18f,                 // rainbow=false, but flashLeft non-zero => slow-path
                flashLeft, overshootLeft, postLockHold,
                "ffffff", "ffe066"
        );

        Method curStr = BA.getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);

        assertNotEquals("ABCDEFGH", s);
        assertTrue(s.contains("[#"), "slow path should inject color tags");
    }


    @Test
    void currentString_rainbow_path_adds_markup_even_if_flash_null() throws Exception {
        Object ba = newBlockAnim(true, null);
        Method cs = BA().getDeclaredMethod("currentString");
        cs.setAccessible(true);
        String s = (String) cs.invoke(ba);
        assertTrue(COLOR_TAG.matcher(s).find(), "rainbow path should add per-char color tags");
    }

    @Test
    void spinDigit_digit_and_nonDigit_with_wrapped_indices() throws Exception {
        Object ba = newBlockAnim(false, null);
        Method spinDigit = m("spinDigit");
        // non-digit -> null
        assertNull(call(ba, spinDigit, idxWrap(5), 'x'));
        // digits -> digit or null (no-change); never throws
        Character d0 = call(ba, spinDigit, idxWrap(0), '0');
        Character dBig = call(ba, spinDigit, idxWrap(37), '3');
        Character dNeg = call(ba, spinDigit, idxWrap(-11), '9');
        if (d0 != null) assertTrue(Character.isDigit(d0));
        if (dBig != null) assertTrue(Character.isDigit(dBig));
        if (dNeg != null) assertTrue(Character.isDigit(dNeg));
    }

    @Test
    void spinLetter_upper_lower_and_nonLetter_with_wrapped_indices() throws Exception {
        Object ba = newBlockAnim(false, null);
        Method spinLetter = m("spinLetter");
        // non-letter -> null
        assertNull(call(ba, spinLetter, idxWrap(5), '_'));
        // uppercase
        Character up0 = call(ba, spinLetter, idxWrap(0), 'A');
        Character upF = call(ba, spinLetter, idxWrap(52), 'M');
        Character upBk = call(ba, spinLetter, idxWrap(-1), 'Z');
        if (up0 != null) assertTrue(Character.isUpperCase(up0));
        if (upF != null) assertTrue(Character.isUpperCase(upF));
        if (upBk != null) assertTrue(Character.isUpperCase(upBk));
        // lowercase
        Character lo0 = call(ba, spinLetter, idxWrap(0), 'a');
        Character loF = call(ba, spinLetter, idxWrap(79), 'n');
        Character loBk = call(ba, spinLetter, idxWrap(-27), 'z');
        if (lo0 != null) assertTrue(Character.isLowerCase(lo0));
        if (loF != null) assertTrue(Character.isLowerCase(loF));
        if (loBk != null) assertTrue(Character.isLowerCase(loBk));
    }

    @Test
    void spinPunct_member_and_nonMember_with_wrapped_indices() throws Exception {
        Object ba = newBlockAnim(false, null);
        Method spinPunct = m("spinPunct");
        // non-member & space -> null
        assertNull(call(ba, spinPunct, idxWrap(5), 'A'));
        assertNull(call(ba, spinPunct, idxWrap(5), ' '));
        // members -> ring character or null (no-change)
        Character out0 = call(ba, spinPunct, idxWrap(0), '.');
        Character outF = call(ba, spinPunct, idxWrap(123), '!');
        Character outBk = call(ba, spinPunct, idxWrap(-3), '`');
        if (out0 != null) assertTrue(PUNCT_SET.contains(out0));
        if (outF != null) assertTrue(PUNCT_SET.contains(outF));
        if (outBk != null) assertTrue(PUNCT_SET.contains(outBk));
    }

    @Test
    void stepFrame_invokes_letter_digit_punct_spinners() throws Exception {
        // Reflect the BlockAnim ctor
        Class<?> BA = Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim");
        Constructor<?> c = BA.getDeclaredConstructor(
                int.class, int[].class, int[].class, char[].class, char[].class,
                boolean.class, float.class, float.class,
                int[].class, int[].class, int[].class,
                String.class, String.class
        );
        c.setAccessible(true);

        final int N = 4; // small; we’ll test 3 slots (0..2)

        // per-slot control arrays
        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] flashLeft = null;           // null => currentString fast path
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];

        // force a spin on each of the first 3 slots
        remaining[0] = 1; // letter
        remaining[1] = 1; // digit
        remaining[2] = 1; // punct
        // delays all zero, so they hit spinner branches immediately

        // Targets by category: 0=letter, 1=digit, 2=punct, 3=space (no-op)
        char[] target = new char[]{'A', '5', '.', ' '};
        // Start curr as zeros so spinner init paths (curr[i]==0) can run
        char[] curr = new char[N];

        // Build a real BlockAnim (rainbow=false so currentString returns raw curr)
        Object ba = c.newInstance(
                60, delays, remaining, target, curr,
                false, 0.6f, 18f,
                flashLeft, overshootLeft, postLockHold,
                "ffffff", "ffe066"
        );

        // call stepFrame()
        Method step = BA.getDeclaredMethod("stepFrame");
        step.setAccessible(true);
        step.invoke(ba);

        // read curr via currentString() (fast path: rainbow=false & flashLeft==null)
        Method curStr = BA.getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);
        // s now equals new String(curr)

        // Validate categories advanced to something sensible
        assertEquals(4, s.length());
        char cLetter = s.charAt(0);
        char cDigit = s.charAt(1);
        char cPunct = s.charAt(2);
        char cSpace = s.charAt(3);

        // letter branch: must be a letter (uppercase here)
        assertTrue(Character.isLetter(cLetter), "slot 0 should be a letter after spin");
        // digit branch: must be a digit
        assertTrue(Character.isDigit(cDigit), "slot 1 should be a digit after spin");
        // punct branch: must be member of the ring
        assertTrue(isPunct(cPunct), "slot 2 should be a punctuation from PUNCT_RING");
        // space remains space (no spinner)
        assertEquals(' ', cSpace);
    }

    @Test
    void currentString_fastPath_allZero_null_returns_plain_string() throws Exception {
        // Reflect BlockAnim(big-ctor)
        Class<?> BA = Class.forName("com.csse3200.game.ui.effects.TextEffects$BlockAnim");
        Constructor<?> c = BA.getDeclaredConstructor(
                int.class, int[].class, int[].class, char[].class, char[].class,
                boolean.class, float.class, float.class,
                int[].class, int[].class, int[].class,
                String.class, String.class
        );
        c.setAccessible(true);

        final int N = 8;

        int[] delays = new int[N];
        int[] remaining = new int[N];
        int[] overshootLeft = new int[N];
        int[] postLockHold = new int[N];
        int[] flashLeft = null;                 // ← cover arr == null ⇒ true

        char[] target = "ABCDEFGH".toCharArray();
        char[] curr = "ABCDEFGH".toCharArray();

        // rainbow=false so fast-path is eligible; flashLeft=null triggers allZero(null) == true
        Object ba = c.newInstance(
                60, delays, remaining, target, curr,
                false, 0.6f, 18f,
                flashLeft, overshootLeft, postLockHold,
                "ffffff", "ffe066"
        );

        Method curStr = BA.getDeclaredMethod("currentString");
        curStr.setAccessible(true);
        String s = (String) curStr.invoke(ba);

        // FAST-PATH: return raw curr (no markup)
        assertEquals("ABCDEFGH", s);
        assertFalse(s.contains("[#"), "fast path with flashLeft=null must NOT inject markup");
    }
}
