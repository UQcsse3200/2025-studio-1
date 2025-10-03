package com.csse3200.game.components.screens;

/**
 * Represents a single page in the Tutorial flow.
 * <p>
 * A {@code TutorialStep} consists of a title, descriptive text, and an optional
 * animated clip (see {@link TutorialClip}) to illustrate the concept.
 *
 * @param title       Short heading for the step.
 * @param description Explanatory body text for the step.
 * @param clip        Optional animated clip associated with this step; may be {@code null}.
 */
public record TutorialStep(String title, String description, TutorialClip clip) {
    /**
     * Creates a tutorial step with optional animation.
     *
     * @param title       step title (non-null, non-empty recommended)
     * @param description step description (non-null, non-empty recommended)
     * @param clip        optional animated clip; may be {@code null} if no media is shown
     */
    public TutorialStep {
    }

    /**
     * @return the step title
     */
    @Override
    public String title() {
        return title;
    }

    /**
     * @return the step description text
     */
    @Override
    public String description() {
        return description;
    }

    /**
     * @return the optional animated clip, or {@code null} if none
     */
    @Override
    public TutorialClip clip() {
        return clip;
    }
}
