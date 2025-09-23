package com.csse3200.game.areas.difficulty;

/**
 * Difficulty class that should be registered in ServiceLocator and 
 * computes the float ready to be input into NPC Factory when
 * spawning enemies in rooms
 */
public class Difficulty {
    private DifficultyType diffType;

    /**
     * Constructor, requires a set diffType from the 
     * difficultyType enum
     */
    public Difficulty(DifficultyType diffType) {
        this.diffType = diffType;
    }

    /**
     * Get a float scaler to be input directly into NPC Factory
     * functions. Requires a roomDifficulty float that expects the
     * float as a whole number from the range 1 - 10 (not required
     * to be a whole number or in this range but the formula works
     * best when done like this)
     * @param roomDifficulty float, expected whole number 1 - 10
     * @return float that should be input directly into NPC factory
     */
    public float getRoomDifficulty(float roomDifficulty) {
        return (0.1f * roomDifficulty + (diffType.getScaler() - 1));
    }
}
