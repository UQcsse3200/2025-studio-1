package com.csse3200.game.ui.effects;

import org.junit.jupiter.api.DisplayName;
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

    private static int expectedHash(String text, float interval, StringBuilder buf, int[] idx) {
        int h = java.util.Objects.hash(text, interval, System.identityHashCode(buf));
        h = 31 * h + java.util.Arrays.hashCode(idx);
        return h;
    }


    @Test
    @DisplayName("equals: o == this fast-path → true")
    void equals_identityTrue() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        Object a = newTI("t", 1.0f, buf, new int[]{1, 2});
        assertEquals(a, a);
    }

    @Test
    @DisplayName("equals: non-instance and null → false")
    void equals_otherTypeAndNullFalse() throws Exception {
        Object a = newTI("t", 1.0f, new StringBuilder("x"), new int[]{1, 2});
        assertNotEquals("not-a-typinginit", a);
        assertNotEquals(null, a);
    }

    @Test
    @DisplayName("equals: all components equal (incl. null text) → true")
    void equals_allEqual_true() throws Exception {
        StringBuilder buf = new StringBuilder("x");

        // non-null text + same buf identity + same idx contents
        Object a1 = newTI("t", 2.0f, buf, new int[]{1, 2});
        Object a2 = newTI("t", 2.0f, buf, new int[]{1, 2});
        assertEquals(a1, a2);

        // null text + null idx (Objects.equals + Arrays.equals handle nulls)
        Object n1 = newTI(null, 3.0f, buf, null);
        Object n2 = newTI(null, 3.0f, buf, null);
        assertEquals(n1, n2);
    }

    @Test
    @DisplayName("equals: any differing component → false")
    void equals_componentDiffers_false() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        int[] idx = new int[]{1, 2};
        Object base = newTI("t", 4.0f, buf, idx);

        // interval differs (Float.compare != 0)
        assertNotEquals(base, newTI("t", 5.0f, buf, new int[]{1, 2}));

        // text differs (Objects.equals false)
        assertNotEquals(base, newTI("T", 4.0f, buf, new int[]{1, 2}));

        // buf identity differs (same content, different object)
        assertNotEquals(base, newTI("t", 4.0f, new StringBuilder("x"), new int[]{1, 2}));

        // idx contents differ (Arrays.equals false)
        assertNotEquals(base, newTI("t", 4.0f, buf, new int[]{1, 3}));
    }

    @Test
    @DisplayName("equals: record pattern guard → false for other type and null")
    void recordPattern_guard_false() throws Exception {
        Object a = newTI("t", 1.0f, new StringBuilder("x"), new int[]{1, 2});

        assertNotEquals(a, new Object());
        assertNotNull(a);
        assertDoesNotThrow(() -> a.equals(null));
    }

    @Test
    @DisplayName("equals: record pattern match → all components equal → true")
    void recordPattern_all_equal_true() throws Exception {
        StringBuilder buf = new StringBuilder("x"); // SAME identity for both objects
        Object a = newTI("t", 2.0f, buf, new int[]{1, 2});
        Object b = newTI("t", 2.0f, buf, new int[]{1, 2}); // different array, same contents
        assertEquals(a, b); // passes Float.compare, Objects.equals(text), buf identity, Arrays.equals(idx)
    }

    @Test
    @DisplayName("equals: record pattern match → each differing component → false")
    void recordPattern_component_diffs_false() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        Object base = newTI("t", 3.0f, buf, new int[]{1, 2});

        // interval differs
        assertNotEquals(base, newTI("t", 3.0001f, buf, new int[]{1, 2}));

        // text differs
        assertNotEquals(base, newTI("T", 3.0f, buf, new int[]{1, 2}));

        // buf identity differs (content same, identity different)
        assertNotEquals(base, newTI("t", 3.0f, new StringBuilder("x"), new int[]{1, 2}));

        // idx contents differ (drives Arrays.equals(...) → false)
        assertNotEquals(base, newTI("t", 3.0f, buf, new int[]{1, 3}));

        // also cover null vs empty for idx (Arrays.equals(null, new int[0]) → false)
        assertNotEquals(newTI("t", 3.0f, buf, null), newTI("t", 3.0f, buf, new int[]{}));
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
    @DisplayName("hashCode: equal objects (same buf identity, same idx contents) → equal hash")
    void hash_equalObjects_equalHash() throws Exception {
        StringBuilder buf = new StringBuilder("x");
        int[] idxA = new int[]{1, 2};
        Object a = newTI("t", 1.0f, buf, idxA);
        Object b = newTI("t", 1.0f, buf, new int[]{1, 2}); // different array, same contents

        assertEquals(a, b); // equals contract
        assertEquals(a.hashCode(), b.hashCode()); // hash contract

        int expected = expectedHash("t", 1.0f, buf, idxA);
        assertEquals(expected, a.hashCode());
        assertEquals(expected, b.hashCode());
    }

    @Test
    @DisplayName("hashCode: uses buf identity, not content")
    void hash_usesBufIdentity() throws Exception {
        StringBuilder buf1 = new StringBuilder("same");
        StringBuilder buf2 = new StringBuilder("same"); // different identity
        int[] idx = new int[]{1, 2};

        Object a = newTI("t", 2.0f, buf1, idx);
        Object b = newTI("t", 2.0f, buf2, new int[]{1, 2});

        int expA = expectedHash("t", 2.0f, buf1, idx);
        int expB = expectedHash("t", 2.0f, buf2, new int[]{1, 2});

        assertEquals(expA, a.hashCode());
        assertEquals(expB, b.hashCode());

        // Only assert inequality if identities differ (should hold in practice).
        int id1 = System.identityHashCode(buf1);
        int id2 = System.identityHashCode(buf2);
        if (id1 != id2) {
            assertNotEquals(a.hashCode(), b.hashCode());
        }
    }

    @Test
    @DisplayName("hashCode: idx null vs empty yields different hash (Arrays.hashCode(null)=0, empty=1)")
    void hash_idxNullVsEmpty() throws Exception {
        StringBuilder buf = new StringBuilder("y");
        Object n = newTI("t", 3.0f, buf, null);
        Object e = newTI("t", 3.0f, buf, new int[]{});

        int expN = expectedHash("t", 3.0f, buf, null);
        int expE = expectedHash("t", 3.0f, buf, new int[]{});

        assertEquals(expN, n.hashCode());
        assertEquals(expE, e.hashCode());
        assertNotEquals(expN, expE);
    }

    @Test
    @DisplayName("hashCode: interval difference changes hash")
    void hash_intervalDiffers() throws Exception {
        StringBuilder buf = new StringBuilder("z");
        int[] idx = new int[]{4, 5};

        Object a = newTI("t", 1.0f, buf, idx);
        Object b = newTI("t", 1.0001f, buf, new int[]{4, 5});

        int expA = expectedHash("t", 1.0f, buf, idx);
        int expB = expectedHash("t", 1.0001f, buf, new int[]{4, 5});

        assertEquals(expA, a.hashCode());
        assertEquals(expB, b.hashCode());
        assertNotEquals(expA, expB);
    }

    @Test
    @DisplayName("hashCode: null text handled by Objects.hash")
    void hash_nullTextHandled() throws Exception {
        StringBuilder buf = new StringBuilder("w");
        int[] idx = new int[]{7};

        Object a = newTI(null, 4.0f, buf, idx);
        int expected = expectedHash(null, 4.0f, buf, idx);
        assertEquals(expected, a.hashCode());
    }
}