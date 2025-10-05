package com.csse3200.game.session;

import com.csse3200.game.records.RoundData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests verify the correct functionality of the
 * leaderboard data management system, including initialization,
 * adding rounds, sorting, replacing data, and resetting.
 */
class LeaderBoardManagerTest {

    private LeaderBoardManager manager;

    @BeforeEach
    void setUp() {
        manager = new LeaderBoardManager();
    }

    /**
     * Ensure that the constructor initializes the leaderboard as an
     * empty, non-null list
     */
    @Test
    @DisplayName("Constructor initializes an empty leaderboard")
    void constructor_startsWithEmptyLeaderboard() {
        assertNotNull(manager.getLeaderBoard(), "Leaderboard list should be initialized");
        assertTrue(manager.getLeaderBoard().isEmpty(), "Leaderboard should start empty");
    }

    /**
     * Verifies that adding a round correctly records the currency and
     * time values in the leaderboard
     */
    @Test
    @DisplayName("addRound() adds entry with correct currency and time")
    void addRound_addsEntryWithCorrectData() {
        manager.addRound(150, 73.5f);

        List<RoundData> lb = manager.getLeaderBoard();
        assertEquals(1, lb.size(), "One round should be recorded");

        RoundData rd = lb.get(0);
        assertEquals(150, rd.getCurrency(), "Currency should match the added value");
        assertEquals(73.5f, rd.getTime(), 1e-6, "Time should match the added value");
    }

    /**
     * Verifies that the list returned by
     * {@link LeaderBoardManager#getLeaderBoard()} is live,
     * meaning mutations on it affect the manager's internal state.
     */
    @Test
    @DisplayName("getLeaderBoard() returns a live reference to the internal list")
    void getLeaderBoard_returnsLiveReference_thatCanBeMutated() {
        manager.addRound(10, 1.0f);
        List<RoundData> lb = manager.getLeaderBoard();

        lb.add(new RoundData(20, 2.5f));

        assertEquals(2, manager.getLeaderBoard().size(), "Mutating the returned list should affect the manager's state");
    }

    /**
     * Verifies that {@link LeaderBoardManager#setLeaderboard(List)}
     * replaces the leaderboard with the provided list reference and
     * correctly sorts it in descending order by score.
     */
    @Test
    @DisplayName("setLeaderboard() replaces existing list and sorts descending by score")
    void setLeaderboard_replacesWithProvidedList_referenceIsUsed() {
        List<RoundData> external = new ArrayList<>();
        external.add(new RoundData(5, 3.0f));
        external.add(new RoundData(7, 4.5f));

        manager.setLeaderboard(external);

        assertSame(external, manager.getLeaderBoard(), "Manager should use the provided list reference");

        // Assert: leaderboard is sorted by score (highest first)
        List<RoundData> lb = manager.getLeaderBoard();
        assertEquals(2, lb.size());

        // After sorting, the round with score 11 comes first
        assertEquals(7, lb.get(0).getCurrency());
        assertEquals(4.5f, lb.get(0).getTime(), 1e-6);

        // Then the round with score 8
        assertEquals(5, lb.get(1).getCurrency());
        assertEquals(3.0f, lb.get(1).getTime(), 1e-6);
    }

    /**
     * Verifies that passing
     * {@code null} to {@link LeaderBoardManager#setLeaderboard(List)}
     * resets the leaderboard to a new, empty list to prevent
     * {@link NullPointerException}.
     */
    @Test
    @DisplayName("setLeaderboard(null) reinitializes with an empty list safely")
    void setLeaderboard_withNull_createsNewEmptyList() {
        // Pre-populate
        manager.addRound(99, 9.9f);

        manager.setLeaderboard(null);

        List<RoundData> lb = manager.getLeaderBoard();
        assertNotNull(lb, "List should not be null after setting null");
        assertTrue(lb.isEmpty(), "List should be cleared and reinitialized when null is provided");

        // Ensure it's a new instance and not the old pre-populated one
        lb.add(new RoundData(1, 1.0f));
        assertEquals(1, manager.getLeaderBoard().size(), "New list should be independent and mutable");
    }

    /**
     * Verifies that {@link LeaderBoardManager#reset()}
     * clears all leaderboard entries.
     */
    @Test
    @DisplayName("reset() clears all entries in the leaderboard")
    void reset_clearsAllEntries() {
        manager.addRound(100, 10.0f);
        manager.addRound(200, 20.0f);
        assertFalse(manager.getLeaderBoard().isEmpty(), "Sanity check: list pre-populated");

        manager.reset();

        assertTrue(manager.getLeaderBoard().isEmpty(), "reset() should clear the leaderboard");
    }

    /**
     * Ensures that adding multiple rounds sorts the
     * leaderboard in descending order by score.
     */
    @Test
    @DisplayName("addRound() automatically sorts leaderboard from highest to lowest score")
    void addRound_sortsLeaderboardDescendingByScore() {
        // Add rounds in random order
        manager.addRound(10, 1.0f); // score = 11
        manager.addRound(20, 2.0f); // score = 22
        manager.addRound(15, 0.5f); // score = 15

        List<RoundData> lb = manager.getLeaderBoard();

        assertEquals(3, lb.size(), "Three rounds should be recorded");

        // Verify sorting (highest to lowest)
        assertEquals(22, lb.get(0).getScore(), "Highest score should appear first");
        assertEquals(15, lb.get(1).getScore(), "Second highest score should appear second");
        assertEquals(11, lb.get(2).getScore(), "Lowest score should appear last");
    }

    /**
     * Tests that rounds with identical scores
     * maintain insertion order after sorting.
     */
    @Test
    @DisplayName("addRound() keeps stable order when scores are identical")
    void addRound_handlesDuplicateScoresConsistently() {
        manager.addRound(10, 5.0f); // score = 15
        manager.addRound(8, 7.0f);  // score = 15

        List<RoundData> lb = manager.getLeaderBoard();

        // Both scores are equal, order should remain as added
        assertEquals(2, lb.size(), "Both rounds should be added");
        assertEquals(10, lb.get(0).getCurrency(), "First round should stay first if scores tie");
        assertEquals(8, lb.get(1).getCurrency(), "Second round should stay second if scores tie");
    }

    /**
     * Verifies that loading an unsorted leaderboard
     * results in a correctly sorted list(descending by score).
     */
    @Test
    @DisplayName("setLeaderboard() sorts input list descending even if unsorted initially")
    void setLeaderboard_sortsDescendingEvenIfUnsortedInput() {
        List<RoundData> unsorted = new ArrayList<>();
        unsorted.add(new RoundData(5, 3.0f)); // score = 8
        unsorted.add(new RoundData(20, 1.0f)); // score = 21
        unsorted.add(new RoundData(10, 5.0f)); // score = 15

        manager.setLeaderboard(unsorted);

        List<RoundData> lb = manager.getLeaderBoard();

        assertEquals(3, lb.size(), "All entries should remain present after sorting");
        assertEquals(21, lb.get(0).getScore(), "Highest score should be first");
        assertEquals(15, lb.get(1).getScore(), "Second highest score should be second");
        assertEquals(8, lb.get(2).getScore(), "Lowest score should be last");
    }

    /**
     * Ensures that calling {@link LeaderBoardManager#reset()}
     * on an empty leaderboard does not throw any exceptions
     * and keeps the list empty.
     */
    @Test
    @DisplayName("reset() does not throw when leaderboard is already empty")
    void reset_onEmptyLeaderboard_doesNotThrow() {
        assertDoesNotThrow(() -> manager.reset(), "reset() should not throw on empty leaderboard");
        assertTrue(manager.getLeaderBoard().isEmpty(), "Leaderboard should remain empty after reset");
    }


}
