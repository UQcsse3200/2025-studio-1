package com.csse3200.game.components.screens;


import org.junit.Test;

import static org.junit.Assert.*;

public class TutorialStepTest {
    @Test
    public void gettersReturnSetValues() {
        TutorialClip clip = new TutorialClip("f", "p", 25, 12f, true);
        TutorialStep step = new TutorialStep("Title", "Use WASD", clip);

        assertEquals("Title", step.title());
        assertEquals("Use WASD", step.description());
        assertSame(clip, step.clip());
    }

    @Test
    public void allowNullClip() {
        TutorialStep step = new TutorialStep("OnlyText", "No clip", null);
        assertNull(step.clip());
    }
}
