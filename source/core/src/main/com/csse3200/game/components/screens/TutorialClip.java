package com.csse3200.game.components.screens;

public class TutorialClip {
    public final String folder;
    public final String pattern;
    public final int frameCount;
    public final float fps;
    public final boolean loop;

    public TutorialClip(String folder, String pattern, int frameCount, float fps, boolean loop) {
        this.folder = folder;
        this.pattern = pattern;
        this.frameCount = frameCount;
        this.fps = fps;
        this.loop = loop;
    }
}
