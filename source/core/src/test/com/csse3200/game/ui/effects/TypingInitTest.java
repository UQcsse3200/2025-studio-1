package com.csse3200.game.ui.effects;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class TypingInitTest {

    private static Class<?> TI() throws Exception {
        return Class.forName("com.csse3200.game.ui.effects.TextEffects$TypingInit");
    }

    /**
     * Create a TypingInit(text, interval, buf, idx) even if it's a non-static nested record.
     */
    private static Object newTI(String text, float interval, StringBuilder buf, int[] idx) throws Exception {
        Class<?> ti = TI();
        Constructor<?> ctor = null;
        for (Constructor<?> c : ti.getDeclaredConstructors()) {
            if (c.getParameterCount() == 4 || c.getParameterCount() == 5) {
                ctor = c;
                break;
            }
        }
        assertNotNull(ctor, "TypingInit constructor not found");
        ctor.setAccessible(true);

        if (ctor.getParameterCount() == 4) {
            return ctor.newInstance(text, interval, buf, idx);
        } else {
            // non-static inner: first param is outer TextEffects
            TextEffects outer = new TextEffects();
            return ctor.newInstance(outer, text, interval, buf, idx);
        }
    }

    private static String bufHex(StringBuilder b) {
        return Integer.toHexString(System.identityHashCode(b));
    }

    @Test
    void equals_reflexive_and_basic_true_case() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        int[] idx1 = new int[]{1, 2};
        Object a = newTI("t", 1.0f, buf, idx1);
        Object b = newTI("t", 1.0f, buf, new int[]{1, 2}); // same content, different array

        assertEquals(a, a);       // reflexive
        assertEquals(a, b);       // same text/interval/buf identity/idx contents
        assertEquals(a.hashCode(), b.hashCode()); // hash contract
    }

    @Test
    void equals_false_when_buf_identity_differs() throws Exception {
        int[] idx = new int[]{1, 2};
        Object a = newTI("t", 1.0f, new StringBuilder("x"), idx);
        Object b = newTI("t", 1.0f, new StringBuilder("x"), new int[]{1, 2});
        assertNotEquals(a, b); // buf compared by identity
    }

    @Test
    void equals_false_when_idx_content_differs() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        Object a = newTI("t", 1.0f, buf, new int[]{1, 2});
        Object b = newTI("t", 1.0f, buf, new int[]{1, 3});
        assertNotEquals(a, b);
    }

    @Test
    void equals_true_when_both_idx_null() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        Object a = newTI("t", 1.0f, buf, null);
        Object b = newTI("t", 1.0f, buf, null);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_false_when_text_or_interval_differs() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        int[] idx = new int[]{1, 2};
        Object base = newTI("t", 1.0f, buf, idx);
        assertNotEquals(base, newTI("T", 1.0f, buf, new int[]{1, 2}));
        assertNotEquals(base, newTI("t", 1.0001f, buf, new int[]{1, 2})); // Float.compare != 0
    }

    @Test
    void equals_false_against_other_type_and_null() throws Exception {
        Object a = newTI("t", 1.0f, new StringBuilder("x"), new int[]{1, 2});
        assertNotEquals(a, "not a TypingInit");
        assertNotEquals(a, null);
    }

    @Test
    void toString_contains_text_interval_buf_identity_and_idx_contents() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        int[] idx = new int[]{1, 2};
        Object a = newTI("hello", 2.0f, buf, idx);
        String s = a.toString();

        assertTrue(s.startsWith("TypingInit["), "prefix missing");
        assertTrue(s.contains("text=hello"), "text missing");
        assertTrue(s.contains("interval=2.0"), "interval missing");
        assertTrue(s.contains("buf@" + bufHex(buf)), "buf identity hex missing");
        assertTrue(s.contains("idx=[1, 2]"), "idx contents missing");
    }

    @Test
    void equals_reflexive_true() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        Object a = newTI("t", 1.0f, buf, new int[]{1, 2});
        assertTrue(a.equals(a)); // o == this fast-path
    }

    @Test
    void equals_false_otherType_and_null() throws Exception {
        Object a = newTI("t", 1.0f, new StringBuilder("x"), new int[]{1, 2});
        assertFalse(a.equals("not-typinginit"));
        assertFalse(a.equals(null));
    }

    @Test
    void equals_interval_differs_false() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        Object a = newTI("t", 1.0f, buf, new int[]{1, 2});
        Object b = newTI("t", 1.0001f, buf, new int[]{1, 2}); // Float.compare != 0
        assertFalse(a.equals(b));
    }

    @Test
    void equals_text_differs_and_null_vs_null() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        Object base = newTI("t", 2.0f, buf, new int[]{1, 2});
        Object diff = newTI("T", 2.0f, buf, new int[]{1, 2});
        assertFalse(base.equals(diff));

        // both null text -> equal (Objects.equals handles this)
        Object n1 = newTI(null, 3.0f, buf, new int[]{1, 2});
        Object n2 = newTI(null, 3.0f, buf, new int[]{1, 2});
        assertTrue(n1.equals(n2));
    }

    @Test
    void equals_buf_identity_differs_false_even_if_content_same() throws Exception {
        int[] idx = new int[]{1, 2};
        Object a = newTI("t", 4.0f, new StringBuilder("same"), idx);
        Object b = newTI("t", 4.0f, new StringBuilder("same"), new int[]{1, 2});
        assertFalse(a.equals(b)); // buf compared by identity
    }

    @Test
    void equals_idx_equal_contents_true() throws Exception {
        StringBuilder buf = new StringBuilder("y");
        Object a = newTI("t", 5.0f, buf, new int[]{1, 2});
        Object b = newTI("t", 5.0f, buf, new int[]{1, 2}); // different array, same contents
        assertTrue(a.equals(b)); // Arrays.equals -> true
    }

    @Test
    void equals_idx_different_contents_false() throws Exception {
        StringBuilder buf = new StringBuilder("z");
        Object a = newTI("t", 6.0f, buf, new int[]{1, 2});
        Object b = newTI("t", 6.0f, buf, new int[]{1, 3});
        assertFalse(a.equals(b));
    }

    @Test
    void equals_idx_bothNull_true_and_oneNull_false() throws Exception {
        StringBuilder buf = new StringBuilder("w");
        Object bothNull1 = newTI("t", 7.0f, buf, null);
        Object bothNull2 = newTI("t", 7.0f, buf, null);
        assertTrue(bothNull1.equals(bothNull2)); // Arrays.equals(null, null) == true

        Object oneNull = newTI("t", 7.0f, buf, new int[]{});
        assertFalse(bothNull1.equals(oneNull));  // null vs empty -> false
    }
}
