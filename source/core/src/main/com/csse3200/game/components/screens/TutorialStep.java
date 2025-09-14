package com.csse3200.game.components.screens;

/**
 * Represents a single page in the Tutorial flow.
 * <p>
 * A {@code TutorialStep} consists of a title, descriptive text, and an optional
 * animated clip (see {@link TutorialClip}) to illustrate the concept.
 */
public class TutorialStep {
    /** Short heading for the step. */
    private final String title;
    /** Explanatory body text for the step. */
    private final String description;
    /** Optional animated clip associated with this step; may be {@code null}. */
    private final TutorialClip clip;

    /**
     * Creates a tutorial step with optional animation.
     *
     * @param title       step title (non-null, non-empty recommended)
     * @param description step description (non-null, non-empty recommended)
     * @param clip        optional animated clip; may be {@code null} if no media is shown
     */
    public TutorialStep(String title, String description, TutorialClip clip) {
        this.title = title;
        this.description = description;
        this.clip = clip;
    }

    /** @return the step title */
    public String getTitle() { return title; }

    /** @return the step description text */
    public String getDescription() { return description; }

    /** @return the optional animated clip, or {@code null} if none */
    public TutorialClip getClip() { return clip; }
}
