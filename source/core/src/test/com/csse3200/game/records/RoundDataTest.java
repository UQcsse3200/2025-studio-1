package com.csse3200.game.records;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class verifies correct behavior of:
 *   - Score calculation logic (currency + time cast to int)
 *   - Getter methods for all fields
 *   - String representation via {@link RoundData#toString()}
 *   - Handling of edge and boundary values
 *
 */
public class RoundDataTest {
    /**
     * Ensures that the constructor initializes all fields correctly
     * and calculates score as currency + (int) time.
     */
    @Test
    @DisplayName("Constructor: should correctly initialize currency, time, and score")
    void constructor_initializesFieldsCorrectly() {
        RoundData round = new RoundData(100, 12.7f);

        assertEquals(100, round.getCurrency(), "Currency should match constructor input");
        assertEquals(12.7f, round.getTime(), 1e-6, "Time should match constructor input");
        assertEquals(112, round.getScore(), "Score should be currency + integer part of time");
    }

    /**
     * Verifies that fractional seconds are truncated (not rounded)
     * when calculating score.
     */
    @Test
    @DisplayName("calculateScore(): should truncate decimal part of time when adding to score")
    void calculateScore_truncatesDecimalPartOfTime() {
        RoundData round = new RoundData(50, 9.9f);

        assertEquals(59, round.getScore(), "Score should truncate fractional time (50 + 9)");
    }

    /**
     * Checks behavior when both currency and time are zero.
     */
    @Test
    @DisplayName("calculateScore(): should return 0 when both currency and time are 0")
    void calculateScore_withZeroValues() {
        RoundData round = new RoundData(0, 0.0f);

        assertEquals(0, round.getScore(), "Score should be 0 when both inputs are 0");
    }

    /**
     * Ensures that {@link RoundData#toString()} returns a readable string
     * containing all field values.
     */
    @Test
    @DisplayName("toString(): should return formatted string containing all fields")
    void toString_returnsFormattedString() {
        RoundData round = new RoundData(75, 3.2f);
        String output = round.toString();

        assertTrue(output.contains("Currency: 75"), "String should contain currency value");
        assertTrue(output.contains("Time: 3.2"), "String should contain time value");
        assertTrue(output.contains("Score: 78"), "String should contain calculated score");
    }

    /**
     * Confirms that multiple RoundData instances operate independently
     * and don't interfere with each other's state.
     */
    @Test
    @DisplayName("Multiple instances: should not interfere with each other's values")
    void multipleInstances_independentState() {
        RoundData round1 = new RoundData(10, 5.5f);
        RoundData round2 = new RoundData(20, 2.1f);

        assertEquals(15, round1.getScore(), "First instance score should be independent");
        assertEquals(22, round2.getScore(), "Second instance score should be independent");
    }

    /**
     * Tests boundary condition where time has a very large
     * fractional value close to next integer.
     */
    @Test
    @DisplayName("calculateScore(): should not round up when time is just below integer boundary")
    void calculateScore_withFractionalEdgeCase() {
        RoundData round = new RoundData(30, 9.999f);

        assertEquals(39, round.getScore(), "Score should truncate to 9, not round up to 10");
    }
}
