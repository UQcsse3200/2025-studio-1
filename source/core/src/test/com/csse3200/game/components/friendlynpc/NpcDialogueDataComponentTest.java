package com.csse3200.game.components.friendlynpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NpcDialogueDataComponentTest {
    @Test
    void storesProvidedValues() {
        String[] lines = {"hello", "world"};
        NpcDialogueDataComponent data =
                new NpcDialogueDataComponent("Mia", "portraits/mia.png", lines);

        assertEquals("Mia", data.getName());
        assertEquals("portraits/mia.png", data.getPortraitPath());
        assertArrayEquals(new String[]{"hello", "world"}, data.getLines());
        assertFalse(data.isEmpty());
    }

    @Test
    void handlesNullsGracefully() {
        NpcDialogueDataComponent data =
                new NpcDialogueDataComponent(null, null, null);

        assertEquals("", data.getName(), "null name should become empty string");
        assertEquals("", data.getPortraitPath(), "null portrait should become empty string");
        assertArrayEquals(new String[0], data.getLines(), "null lines should become empty array");
        assertTrue(data.isEmpty(), "no lines -> isEmpty true");
    }

    @Test
    void constructorDefensiveCopy() {
        String[] input = {"a", "b"};
        NpcDialogueDataComponent data =
                new NpcDialogueDataComponent("Mia", "p.png", input);

        // Modifying the passed in array should not affect internal state
        input[0] = "CHANGED";
        assertArrayEquals(new String[]{"a", "b"}, data.getLines());
    }

    @Test
    void getterDefensiveCopy() {
        String[] lines = {"x", "y"};
        NpcDialogueDataComponent data =
                new NpcDialogueDataComponent("Mia", "p.png", lines);

        String[] fromGetter = data.getLines();
        assertNotSame(lines, fromGetter, "getter should return a copy, not the original");
        fromGetter[0] = "MUTATED";

        // The value should not be affected by external modifications
        assertArrayEquals(new String[]{"x", "y"}, data.getLines());
    }

    @Test
    void emptyWhenZeroLength() {
        NpcDialogueDataComponent data =
                new NpcDialogueDataComponent("Mia", "p.png", new String[0]);

        assertTrue(data.isEmpty());
    }
}
