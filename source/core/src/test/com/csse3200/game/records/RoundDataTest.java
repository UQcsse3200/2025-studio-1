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
class RoundDataTest {
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

    /**
     * Ensures negative inputs behave correctly (edge arithmetic)
     */
    @Test
    @DisplayName("Negative values: should handle negative currency and time correctly")
    void negativeValues_handledCorrectly() {
        RoundData round = new RoundData(-50, -3.8f);

        assertEquals(-50, round.getCurrency(), "Currency should store negative values correctly");
        assertEquals(-3.8f, round.getTime(), 1e-6, "Time should store negative values correctly");
        assertEquals(-53, round.getScore(), "Score should be -50 + (-3) = -53");
    }

    /**
     * Tests integer overflow edge near Integer.MAX_VALUE
     */
    @Test
    @DisplayName("Very large values: should not overflow easily and compute correctly")
    void largeValues_handledCorrectly() {
        RoundData round = new RoundData(Integer.MAX_VALUE - 5, 4.99f);

        // Score = MAX_VALUE - 5 + 4 = MAX_VALUE - 1
        assertEquals(Integer.MAX_VALUE - 1, round.getScore(), "Score should handle near-overflow correctly");
    }

    /**
     * Verifies truncation when time < 1
     */
    @Test
    @DisplayName("Zero and fractional time less than 1: should truncate to 0")
    void fractionalTimeLessThanOne_truncatesToZero() {
        RoundData round = new RoundData(40, 0.8f);
        assertEquals(40, round.getScore(), "Fractional part <1 should truncate to 0");
    }

    /**
     * Expands toString() coverage with label and numeric checks
     */
    @Test
    @DisplayName("toString(): should include all key labels and numeric values")
    void toString_includesAllLabelsAndValues() {
        RoundData round = new RoundData(5, 2.3f);
        String output = round.toString();

        assertAll(
            () -> assertTrue(output.startsWith("Currency:"), "String should start with 'Currency:'"),
            () -> assertTrue(output.contains("Time:"), "String should contain 'Time:' label"),
            () -> assertTrue(output.contains("Score:"), "String should contain 'Score:' label"),
            () -> assertTrue(output.matches(".*\\d.*"), "String should include numeric values")
        );
    }

    /**
     * Adds consistency checks for deterministic methods
     */
    @Test
    @DisplayName("Repeated toString() calls: should produce consistent output")
    void toString_isConsistentAcrossCalls() {
        RoundData round = new RoundData(12, 4.6f);
        String first = round.toString();
        String second = round.toString();

        assertEquals(first, second, "toString() output should remain consistent");
    }

    /**
     * Ensures (int) cast behaves for whole numbers
     */
    @Test
    @DisplayName("Boundary test: time with exact integer value")
    void timeExactInteger_noTruncationNeeded() {
        RoundData round = new RoundData(25, 10.0f);
        assertEquals(35, round.getScore(), "Score should add time directly when integer");
    }

    /**
     * Covers truncation behavior for small negative floats
     */
    @Test
    @DisplayName("Extreme small negative time: should truncate toward zero correctly")
    void negativeFractionalTime_truncatesTowardZero() {
        RoundData round = new RoundData(10, -0.9f);
        // (int) -0.9 = 0 in Java (truncated toward 0)
        assertEquals(10, round.getScore(), "Negative fractional time truncates to 0, not -1");
    }

    /**
     * Improves getter coverage through repeated access
     */
    @Test
    @DisplayName("Multiple calls to getters should always return same results")
    void getters_areIdempotent() {
        RoundData round = new RoundData(7, 2.9f);

        assertEquals(round.getCurrency(), round.getCurrency());
        assertEquals(round.getTime(), round.getTime());
        assertEquals(round.getScore(), round.getScore());
    }
}
