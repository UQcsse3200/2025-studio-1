package com.csse3200.game.areas;

import com.csse3200.game.areas.difficulty.Difficulty;
import com.csse3200.game.areas.difficulty.DifficultyType;
import com.csse3200.game.extensions.GameExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class DifficultyTest {

    private Difficulty difficultyEasy;
    private Difficulty difficultyNormal;
    private Difficulty difficultyHard;
    private Difficulty difficultyInsane;

    @BeforeEach
    void beforeEach() {
        // Default to Normal
        difficultyEasy = new Difficulty(DifficultyType.EASY);
        difficultyNormal = new Difficulty(DifficultyType.NORMAL);
        difficultyHard = new Difficulty(DifficultyType.HARD);
        difficultyInsane = new Difficulty(DifficultyType.INSANE);
    }

    /**
     * Check that the difficulty value increases as the rooms constant
     * increases, and remains in the reasonable range 0 to 5
     */
    @Test
    void roomProgression() {
        assertTrue(0f < difficultyNormal.getRoomDifficulty(1f));
        assertTrue(difficultyNormal.getRoomDifficulty(1f) < difficultyNormal.getRoomDifficulty(2f));
        assertTrue(difficultyNormal.getRoomDifficulty(2f) < difficultyNormal.getRoomDifficulty(5f));
        assertTrue(difficultyNormal.getRoomDifficulty(5f) < difficultyNormal.getRoomDifficulty(9f));
        assertTrue(difficultyNormal.getRoomDifficulty(9f) < difficultyNormal.getRoomDifficulty(10f));
        assertTrue(difficultyNormal.getRoomDifficulty(10f) < 5f);
    }

    /**
     * Check that the difficulty constant increases the difficulty
     */
    @Test
    void diffProgression() {
        float diff1 = difficultyEasy.getRoomDifficulty(2f);
        float diff2 = difficultyNormal.getRoomDifficulty(2f);
        float diff3 = difficultyHard.getRoomDifficulty(2f);
        float diff4 = difficultyInsane.getRoomDifficulty(2f);

        assertTrue(0f < diff1
                && diff1 < diff2
                && diff2 < diff3
                && diff3 < diff4
                && diff4 < 5f);
    }

    /**
     * Check that the total bounds are reasonable
     */
    @Test
    void testExtremes() {
        float minDiff = difficultyEasy.getRoomDifficulty(1f);
        float maxDiff = difficultyInsane.getRoomDifficulty(10f);

        assertTrue(0 < minDiff
                && minDiff < maxDiff
                && maxDiff < 5f);
    }
}
