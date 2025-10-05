package com.csse3200.game.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * These tests verify that a new GameSession correctly initializes
 * its {@link LeaderBoardManager} and retains the correct session ID.
 */

public class GameSessionTest {
    private GameSession session1;
    private GameSession session2;

    /**
     * Sets up two GameSession instances with different session IDs
     * before each test is executed.
     */
    @BeforeEach
    void setUp() {
        session1 = new GameSession(101);
        session2 = new GameSession(202);
    }

    /**
     * Tests that the session ID provided during construction
     * is correctly returned by {@link GameSession#getSessionId()}.
     */
    @Test
    @DisplayName("getSessionId(): should return the correct session ID")
    void testGetSessionId() {
        assertEquals(101, session1.getSessionId(), "Session ID should match the one provided in the constructor");
        assertEquals(202, session2.getSessionId(), "Session ID should match the one provided in the constructor");
    }

    /**
     * Tests that {@link GameSession#getLeaderBoardManager()} returns
     * a non-null {@link LeaderBoardManager} instance.
     */
    @Test
    @DisplayName("getLeaderBoardManager(): should not return null")
    void testLeaderBoardManagerNotNull() {
        assertNotNull(session1.getLeaderBoardManager(), "LeaderBoardManager should not be null");
    }

    /**
     * Tests that each {@link GameSession} has its own unique
     * {@link LeaderBoardManager} instance, ensuring sessions
     * do not share leaderboard data.
     */
    @Test
    @DisplayName("getLeaderBoardManager(): should create a unique LeaderBoardManager per session")
    void testUniqueLeaderBoardManagerPerSession() {
        assertNotSame(session1.getLeaderBoardManager(),
            session2.getLeaderBoardManager(),
            "Each session should have a unique LeaderBoardManager instance");
    }

}
