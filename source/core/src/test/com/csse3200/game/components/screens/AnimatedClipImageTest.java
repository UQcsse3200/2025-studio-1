package com.csse3200.game.components.screens;

import com.csse3200.game.ui.AnimatedClipImage;
import org.junit.Test;

public class AnimatedClipImageTest {
    @Test(expected = IllegalArgumentException.class)
    public void negativeFrameCountThrows() {
        TutorialClip bad = new TutorialClip("images/tutorial/move", "frame_%04d.png", -1, 12f, true);
        new AnimatedClipImage(bad);
    }
}
