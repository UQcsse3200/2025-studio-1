package com.csse3200.game.components.screens;

/**
 * A page in the tutorial.
 */
public class TutorialStep {
    private final String title;
    private final String description;
    private final TutorialClip clip;

    /**
     * Creates a tutorial step with optional clip.
     */
    public TutorialStep(String title, String description, TutorialClip clip) {
        this.title = title;
        this.description = description;
        this.clip = clip;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TutorialClip getClip() { return clip; }
}