package com.csse3200.game.components.minigames.slots;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class SlotsTest {

    @Test
    public void testGetRandomReturnsNullWhenMessagesNull() {
        SlotsText st = new SlotsText();
        st.messages = null;
        assertNull("Should return null if messages list is null", st.getRandom());
    }

    @Test
    public void testGetRandomReturnsNullWhenEmpty() {
        SlotsText st = new SlotsText();
        st.messages = Collections.emptyList();
        assertNull("Should return null if messages list is empty", st.getRandom());
    }

    @Test
    public void testGetRandomReturnsOnlyElementWhenSingle() {
        SlotsText st = new SlotsText();
        st.messages = Collections.singletonList("only");
        for (int i = 0; i < 20; i++) {
            assertEquals("Should always return the only element", "only", st.getRandom());
        }
    }

    @Test
    public void testGetRandomAlwaysFromList() {
        SlotsText st = new SlotsText();
        st.messages = Arrays.asList("apple", "banana", "cherry");
        for (int i = 0; i < 100; i++) {
            String result = st.getRandom();
            assertTrue("Unexpected result: " + result, st.messages.contains(result));
        }
    }
}
