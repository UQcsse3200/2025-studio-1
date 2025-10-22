package com.csse3200.game.ui.terminal.autocomplete;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BKTreeTest {

    @Test
    void insert_ignoresNullAndEmpty_and_avoidsDuplicate() {
        BKTree tree = new BKTree();
        tree.insert(null);
        tree.insert("");
        tree.insert("help");
        tree.insert("help"); // duplicate ignored

        // threshold 0 → exact only
        List<String> out = tree.searchWithin("help", 0);
        assertEquals(List.of("help"), out);
    }

    @Test
    void searchWithin_nullRoot_or_nullQuery_returnsEmpty() {
        BKTree tree = new BKTree();

        // root is null
        assertTrue(tree.searchWithin("anything", 1).isEmpty());

        // root non-null, but query is null
        tree.insert("help");
        assertTrue(tree.searchWithin(null, 1).isEmpty());
    }

    @Test
    void searchWithin_threshold1_sorted_and_capped_to_5() {
        BKTree tree = new BKTree();

        // Make "help" the root for predictable child distances
        tree.insert("help");
        // All of these are distance 1 from "help" (or 0 for "help" itself)
        // (substitution, insertion, or deletion)
        for (String s : Arrays.asList("hell", "helm", "heap", "held", "yelp", "helpx", "hep")) {
            tree.insert(s);
        }

        List<String> out = tree.searchWithin("help", 1);
        // Should be sorted lexicographically and capped to 5
        assertEquals(5, out.size());
        assertEquals(List.of("heap", "held", "hell", "helm", "help"), out);
    }

    @Test
    void searchWithin_lenDiff_gt2_excluded_by_earlyExit() {
        BKTree tree = new BKTree();
        tree.insert("help"); // root
        // query far longer → |len diff| > 2, early exit in distLE2
        assertTrue(tree.searchWithin("helpaaaa", 1).isEmpty());
    }

    @Test
    void searchWithin_childWindow_prunes_outOfRange_children() {
        BKTree tree = new BKTree();
        tree.insert("help");
        // Insert something far away (likely distance >= 3) so it becomes a child keyed > 2
        tree.insert("zzzz");

        // Searching with threshold 1 should only visit children with key in [d-1, d+1]
        // For d=distance("help","help")=0 at root, that is [-1,1] → skips children keyed 3+
        List<String> out = tree.searchWithin("help", 1);
        assertEquals(List.of("help"), out);
    }

    @Test
    void searchWithin_exact_and_small_variants_with_threshold0_and1() {
        BKTree tree = new BKTree();
        tree.insert("play");
        tree.insert("pray"); // dist 1
        tree.insert("plow"); // dist 2
        tree.insert("ply");  // dist 1 (deletion)

        assertEquals(List.of("play"), tree.searchWithin("play", 0)); // exact only
        assertEquals(List.of("play", "ply", "pray"), tree.searchWithin("play", 1)); // sorted
        assertEquals(List.of("play", "plow", "ply", "pray"), tree.searchWithin("play", 2)); // includes dist 2
    }

    @Test
    void dp_earlyExit_manyMismatches_excluded_under_smallThreshold() {
        BKTree tree = new BKTree();
        tree.insert("abcd");
        tree.insert("wxyz"); // very different

        // With threshold 1, "wxyz" should not be returned when searching for "abcd"
        assertEquals(List.of("abcd"), tree.searchWithin("abcd", 1));
        // And vice versa
        assertEquals(List.of("wxyz"), tree.searchWithin("wxyz", 1));
    }
}
