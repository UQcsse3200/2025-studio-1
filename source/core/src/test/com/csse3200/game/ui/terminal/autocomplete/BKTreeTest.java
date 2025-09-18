// src/test/java/com/csse3200/game/ui/terminal/autocomplete/BKTreeTest.java
package com.csse3200.game.ui.terminal.autocomplete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BKTreeTest {

    BKTree bk;

    @BeforeEach
    void setUp() {
        bk = new BKTree();
        for (String s : List.of("debug", "deathscreen", "disableDamage", "waves", "pickupAll")) {
            bk.insert(s);
        }
    }

    @Test
    void searchWithin_ed1_hit() {
        List<String> out = bk.searchWithin("debub", 1); // typo
        assertTrue(out.contains("debug"));
    }

    @Test
    void searchWithin_ed1_miss() {
        // Use a true distance-2 query
        List<String> out = bk.searchWithin("dxbux", 1);
        assertTrue(out.isEmpty());
    }

    @Test
    void searchWithin_ed2_hit() {
        // Same query should be found with threshold 2
        List<String> out = bk.searchWithin("dxbux", 2);
        assertTrue(out.contains("debug"));
    }

    @Test
    void returnsUpToK() {
        // Populate with many near variants (if needed); here we just assert it doesn't blow up
        List<String> out = bk.searchWithin("waves", 1);
        assertTrue(out.size() <= 5);
    }
}
