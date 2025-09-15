package com.csse3200.game.components.screens;

import org.junit.Test;
import static org.junit.Assert.*;

public class TutorialClipTest {
    @Test
    public void storesFields() {
        TutorialClip clip = new TutorialClip("images/tutorial/move", "frame_%04d.png", 25, 12f, true);

        assertEquals("images/tutorial/move", clip.folder);
        assertEquals("frame_%04d.png", clip.pattern);
        assertEquals(25, clip.frameCount);
        assertEquals(12f, clip.fps, 0.0001f);
        assertTrue(clip.loop);
    }
}
