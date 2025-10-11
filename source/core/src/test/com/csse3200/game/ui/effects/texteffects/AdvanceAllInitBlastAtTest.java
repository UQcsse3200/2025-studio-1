package com.csse3200.game.ui.effects.texteffects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.List;

import static com.csse3200.game.ui.effects.texteffects.RenderAndBlocksTest.newBAWithString;
import static org.junit.jupiter.api.Assertions.*;

class AdvanceAllInitBlastAtTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> clampCases() {
        return java.util.stream.Stream.of(
                // name,  n, i, flashFrames, overshoot, expFlash, expOver
                Arguments.of("neg flash, pos over", 5, 3, -4, 3, 0, 6),
                Arguments.of("pos flash, neg over", 5, 0, 7, -1, 7, 0),
                Arguments.of("both neg", 3, 2, -9, -5, 0, 0)
        );
    }

    // ---- tiny helper ----
    private static int[] filled(int n, int v) {
        int[] a = new int[n];
        java.util.Arrays.fill(a, v);
        return a;
    }

    @Test
    void noOp_when_not_blast() throws Exception {
        int n = 5, i = 2;
        int[] flashLeft = filled(n, 111);
        int[] overshootLeft = filled(n, 222);
        int[] postLockHold = filled(n, 333);

        // isBlast=false => should not modify any array
        initBlastAt.invoke(null, false, flashLeft, overshootLeft, postLockHold, i, /*flashFrames*/7, /*overshoot*/4);

        assertArrayEquals(new int[]{111, 111, 111, 111, 111}, flashLeft);
        assertArrayEquals(new int[]{222, 222, 222, 222, 222}, overshootLeft);
        assertArrayEquals(new int[]{333, 333, 333, 333, 333}, postLockHold);
    }

    @Test
    void assigns_when_blast_happy_path() throws Exception {
        int n = 4, i = 1;
        int[] flashLeft = filled(n, 0);
        int[] overshootLeft = filled(n, 0);
        int[] postLockHold = filled(n, 9);

        initBlastAt.invoke(null, true, flashLeft, overshootLeft, postLockHold, i, /*flashFrames*/5, /*overshoot*/3);

        // only index i is set; others remain unchanged
        assertArrayEquals(new int[]{0, 5, 0, 0}, flashLeft);
        assertArrayEquals(new int[]{0, 6, 0, 0}, overshootLeft); // overshoot * 2
        assertArrayEquals(new int[]{9, 0, 9, 9}, postLockHold);  // set to 0 at i
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("clampCases")
    void clamps_negative_flash_and_overshoot(String _name, int n, int i, int flashFrames, int overshoot,
                                             int expFlash, int expOver) throws Exception {
        int[] flashLeft = filled(n, 1);
        int[] overshootLeft = filled(n, 2);
        int[] postLockHold = filled(n, 3);

        initBlastAt.invoke(null, true, flashLeft, overshootLeft, postLockHold, i, flashFrames, overshoot);

        for (int k = 0; k < n; k++) {
            if (k == i) {
                assertEquals(expFlash, flashLeft[k], "flashLeft@" + k);
                assertEquals(expOver, overshootLeft[k], "overshootLeft@" + k);
                assertEquals(0, postLockHold[k], "postLockHold@" + k);
            } else {
                assertEquals(1, flashLeft[k], "flashLeft other@" + k);
                assertEquals(2, overshootLeft[k], "overshootLeft other@" + k);
                assertEquals(3, postLockHold[k], "postLockHold other@" + k);
            }
        }
    }

    @Test
    void boundary_indices_0_and_last() throws Exception {
        int n = 3;
        int[] flashLeft = filled(n, 0);
        int[] overshootLeft = filled(n, 0);
        int[] postLockHold = filled(n, 7);

        // i = 0
        initBlastAt.invoke(null, true, flashLeft, overshootLeft, postLockHold, 0, 2, 1);
        assertArrayEquals(new int[]{2, 0, 0}, flashLeft);
        assertArrayEquals(new int[]{2, 0, 0}, overshootLeft);
        assertArrayEquals(new int[]{0, 7, 7}, postLockHold);

        // i = last (2)
        initBlastAt.invoke(null, true, flashLeft, overshootLeft, postLockHold, 2, 4, 0);
        assertArrayEquals(new int[]{2, 0, 4}, flashLeft);
        assertArrayEquals(new int[]{2, 0, 0}, overshootLeft);
        assertArrayEquals(new int[]{0, 7, 0}, postLockHold);
    }

    @Test
    void advanceAll_shouldStep_true_and_false_and_allDone_false() throws Exception {
        // Build two blocks with different fps, both not done
        // Use your BA_CTOR helper path; fields: subframe(int), fps(int), done(boolean).
        Object bFast = newBAWithString("X"); // default fps 60 via helper
        Object bSlow = newBAWithString("Y");

        // Force fps values explicitly so we can control skip ratio
        Field fFps = ba.getDeclaredField("fps");
        fFps.setAccessible(true);
        fFps.setInt(bFast, 60);
        fFps.setInt(bSlow, 20);

        // Ensure done=false and subframe at 0 so first tick increments to 1
        Field fDone = ba.getDeclaredField("done");
        fDone.setAccessible(true);
        Field fSub = ba.getDeclaredField("subframe");
        fSub.setAccessible(true);
        fDone.setBoolean(bFast, false);
        fDone.setBoolean(bSlow, false);
        fSub.setInt(bFast, 0);
        fSub.setInt(bSlow, 0);

        // With finalFps=60:
        // bFast: skip = round(60/60)=1 -> shouldStep true (since subframe becomes 1 and 1%1==0)
        // bSlow: skip = round(60/20)=3 -> subframe 1 %3 !=0 -> shouldStep false
        boolean allDone = (boolean) advanceAll.invoke(null, List.of(bFast, bSlow), 60);
        assertFalse(allDone, "at least one block not done");

        // We can assert bFast did one step by observing subframe advanced and/or any field that stepFrame touches.
        // Minimum: subframe must be incremented for both:
        assertEquals(1, (int) fSub.get(bFast));
        assertEquals(1, (int) fSub.get(bSlow));
    }

    @Test
    void advanceAll_allDone_true_when_all_blocks_done() throws Exception {
        Object b1 = newBAWithString("A");
        Object b2 = newBAWithString("B");

        Field fDone = ba.getDeclaredField("done");
        fDone.setAccessible(true);
        Field fSub = ba.getDeclaredField("subframe");
        fSub.setAccessible(true);
        fDone.setBoolean(b1, true);
        fDone.setBoolean(b2, true);
        fSub.setInt(b1, 5);
        fSub.setInt(b2, 7);

        boolean allDone = (boolean) advanceAll.invoke(null, List.of(b1, b2), 30);
        assertTrue(allDone, "when all blocks done, advanceAll returns true");
        // subframe still increments even when done was true (the code increments before shouldStep):
        assertEquals(6, (int) fSub.get(b1));
        assertEquals(8, (int) fSub.get(b2));
    }
}