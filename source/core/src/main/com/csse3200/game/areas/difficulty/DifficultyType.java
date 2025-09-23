package com.csse3200.game.areas.difficulty;

/**
 * Enum containing the different difficulties and a respective float
 * scaler that should define the increase of difficulty. It does NOT
 * define the enemies exact difficulty. The scaler should be referred
 * to in the rooms code as an argument to increase the difficulty
 * of the enemies depending on the set difficulty.
 */
public enum DifficultyType {
    EASY(1f),
    NORMAL(2f),
    HARD(3f),
    INSANE(4f);

    private final float diffScaler;

    /**
     * Constructor for an enum, simply
     * sets the difficulty scaler
     * 
     * @param diffScaler
     */
    DifficultyType(float diffScaler) {
        this.diffScaler = diffScaler;
    }

    /**
     * return the respective scaler depending on the
     * enum
     * 
     * @return float a difficulty scaler constant
     */
    public float getScaler() {
        return diffScaler;
    }
}