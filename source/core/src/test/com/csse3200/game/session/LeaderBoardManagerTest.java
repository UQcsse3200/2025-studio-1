package com.csse3200.game.session;

import com.csse3200.game.records.RoundData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LeaderBoardManagerTest {

    private LeaderBoardManager manager;

    @BeforeEach
    void setUp() {
        manager = new LeaderBoardManager();
    }

    @Test
    void constructor_startsWithEmptyLeaderboard() {
        assertNotNull(manager.getLeaderBoard(), "Leaderboard list should be initialized");
        assertTrue(manager.getLeaderBoard().isEmpty(), "Leaderboard should start empty");
    }

    @Test
    void addRound_addsEntryWithCorrectData() {
        manager.addRound(150, 73.5f);

        List<RoundData> lb = manager.getLeaderBoard();
        assertEquals(1, lb.size(), "One round should be recorded");

        RoundData rd = lb.get(0);
        assertEquals(150, rd.getCurrency(), "Currency should match the added value");
        assertEquals(73.5f, rd.getTime(), 1e-6, "Time should match the added value");
    }

    @Test
    void getLeaderBoard_returnsLiveReference_thatCanBeMutated() {
        manager.addRound(10, 1.0f);
        List<RoundData> lb = manager.getLeaderBoard();

        lb.add(new RoundData(20, 2.5f));

        assertEquals(2, manager.getLeaderBoard().size(), "Mutating the returned list should affect the manager's state");
    }

    @Test
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

    @Test
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

    @Test
    void reset_clearsAllEntries() {
        manager.addRound(100, 10.0f);
        manager.addRound(200, 20.0f);
        assertFalse(manager.getLeaderBoard().isEmpty(), "Sanity check: list pre-populated");

        manager.reset();

        assertTrue(manager.getLeaderBoard().isEmpty(), "reset() should clear the leaderboard");
    }
}
