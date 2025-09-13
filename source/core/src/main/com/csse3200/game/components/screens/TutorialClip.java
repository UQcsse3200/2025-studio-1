package com.csse3200.game.components.screens;

/**
 * Describes an animated tutorial clip built from numbered PNG frames.
 */
public class TutorialClip {
    public final String folder;
    public final String pattern;
    public final int frameCount;
    public final float fps;
    public final boolean loop;

    /**
     * Creates a clip description for building an animation from frames.
     */
    public TutorialClip(String folder, String pattern, int frameCount, float fps, boolean loop) {
        this.folder = folder;
        this.pattern = pattern;
        this.frameCount = frameCount;
        this.fps = fps;
        this.loop = loop;
    }
}
