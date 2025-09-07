package com.csse3200.game.components.screens;

public class TutorialStep {
    private final String title;
    private final String description;
    private final String imagePath;

    public TutorialStep(String title, String description, String imagePath) {
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
}