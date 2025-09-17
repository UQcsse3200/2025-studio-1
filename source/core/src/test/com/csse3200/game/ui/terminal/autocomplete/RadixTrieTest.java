// src/test/java/com/csse3200/game/ui/terminal/autocomplete/RadixTrieTest.java
package com.csse3200.game.ui.terminal.autocomplete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RadixTrieTest {
    RadixTrie trie;

    @BeforeEach
    void setUp() {
        trie = new RadixTrie();
        for (String s : List.of("debug", "deathscreen", "disableDamage", "damageMultiplier", "pickupAll", "waves")) {
            trie.insert(s);
        }
    }

    @Test
    void suggestTopK_basic_allD() {
        // Use "d" when you expect all d* words
        assertEquals(
                List.of("damageMultiplier", "deathscreen", "debug", "disableDamage"),
                trie.suggestTopK("d")
        );
    }

    @Test
    void suggest_insideEdge() {
        // “disab” falls inside the compressed edge to disableDamage
        assertEquals(List.of("disableDamage"), trie.suggestTopK("disab"));
    }

    @Test
    void suggest_de_onlyTwo() {
        // For prefix "de", only these two qualify
        assertEquals(List.of("deathscreen", "debug"), trie.suggestTopK("de"));
    }

    @Test
    void suggest_noMatch() {
        assertTrue(trie.suggestTopK("zzz").isEmpty());
    }
}
