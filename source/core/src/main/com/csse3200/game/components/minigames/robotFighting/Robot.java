package com.csse3200.game.components.minigames.robotFighting;

public enum Robot {
    GHOST_GPT("images/ghostGPT.atlas"),
    DEEP_SPIN("images/Deepspin.atlas");

    private final String atlas;
    Robot(String atlas) {
        this.atlas = atlas;
    }
    public String getAtlas() {
        return atlas;
    }
}
