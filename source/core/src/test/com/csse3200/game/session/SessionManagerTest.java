package com.csse3200.game.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The SessionManagerTests tests the core functionality
 * for session lifecycle including:
 * - creating and retrieving sessions.
 * - ensuring session IDs increment properly.
 * - resetting leaderboards between sessions.
 * - handling null and repeated ens-session calls safely.
 */

class SessionManagerTest {

    /**
     * Ensures that starting a new session creates a
     * non-null {@link GameSession} and assigns it as the active session.
     */
    @Test
    @DisplayName("startNewSession(): should create and set a new active GameSession")
    void startNewSession_createsAndSetsCurrentSession() {
        SessionManager mgr = new SessionManager();

        GameSession s1 = mgr.startNewSession();

        assertNotNull(s1, "startNewSession should return a new GameSession");
        assertSame(s1, mgr.getCurrentSession(), "Current session should be set to the newly created one");
    }

    /**
     * Verifies that multiple calls to {@link SessionManager#startNewSession()}
     * create distinct sessions, and if {@link GameSession#getSessionId()} is available,
     * IDs should increment sequentially.
     */
    @Test
    @DisplayName("startNewSession(): should create unique sessions and increment session IDs")
    void startNewSession_calledTwice_returnsDifferentSessions_andOptionallyChecksIncrementingId() throws Exception {
        SessionManager mgr = new SessionManager();

        GameSession s1 = mgr.startNewSession();
        GameSession s2 = mgr.startNewSession();

        assertNotNull(s1);
        assertNotNull(s2);
        assertNotSame(s1, s2, "Two starts should yield different session instances");
        assertSame(s2, mgr.getCurrentSession(), "Current session should be the most recently started one");

        Method getId = null;
        try {
            getId = GameSession.class.getMethod("getSessionId");
        } catch (NoSuchMethodException ignored) {}
        assumeTrue(getId != null, "GameSession doesn't expose getSessionId(); skipping ID assertions");
        int id1 = (int) getId.invoke(s1);
        int id2 = (int) getId.invoke(s2);
        assertEquals(id1 + 1, id2, "Session IDs should increment on each new session");
    }

    /**
     * Tests that {@link SessionManager#endSession()}:
     * - Clears the current session reference.
     * - Resets the leaderboard of the previous session.
     */
    @Test
    @DisplayName("endSession(): should clear current session and reset its leaderboard")
    void endSession_resetsPreviousSessionsLeaderboard_andClearsCurrent() {
        SessionManager mgr = new SessionManager();

        GameSession session = mgr.startNewSession();
        // add some leaderboard data to the live session
        session.getLeaderBoardManager().addRound(123, 45.6f);
        assertFalse(session.getLeaderBoardManager().getLeaderBoard().isEmpty(), "Precondition: leaderboard has data");

        GameSession previous = mgr.getCurrentSession();

        mgr.endSession();

        // The manager should no longer have an active session
        assertNull(mgr.getCurrentSession(), "endSession should clear the current session");

        // The previous session's leaderboard should have been reset
        assertTrue(previous.getLeaderBoardManager().getLeaderBoard().isEmpty(),
                "endSession should reset the previous session's leaderboard");
    }

    /**
     * Ensures that calling {@link SessionManager#endSession()}
     * when no session is active does not throw an exception
     * or alter state.
     */
    @Test
    @DisplayName("endSession(): should safely handle case when no active session exists")
    void endSession_whenNoActiveSession_isNoOp() {
        SessionManager mgr = new SessionManager();

        // No session started; should not throw
        assertDoesNotThrow(mgr::endSession, "Ending a non-existent session should be a no-op");
        assertNull(mgr.getCurrentSession(), "There should still be no active session");
    }

    /**
     * Verifies that after ending a previous session, starting a new
     * one creates a completely fresh {@link GameSession} instance.
     */
    @Test
    @DisplayName("startNewSession() after endSession(): should create a fresh GameSession")
    void startNewSession_afterEndingPrevious_createsFreshSession() {
        SessionManager mgr = new SessionManager();

        GameSession first = mgr.startNewSession();
        mgr.endSession();

        GameSession second = mgr.startNewSession();

        assertNotNull(second, "Should create a new session after ending the previous");
        assertNotSame(first, second, "New session instance should differ from the ended one");
        assertSame(second, mgr.getCurrentSession(), "Current session should be the newly created one");
    }

    /**
     * Tests that calling {@link SessionManager#endSession()} twice
     * consecutively does not throw exceptions or change the state
     * after the first call.
     */
    @Test
    @DisplayName("endSession() twice: should not throw or alter state")
    void endSession_twice_shouldNotThrow_orChangeState() {
        SessionManager mgr = new SessionManager();
        mgr.startNewSession();
        mgr.endSession();

        // second call should do nothing and not throw
        assertDoesNotThrow(mgr::endSession, "Calling endSession twice should not throw an exception");
        assertNull(mgr.getCurrentSession(), "Current session should still be null");
    }

    /**
     * Validates that a new session after ending a previous one starts
     * with a completely empty leaderboard.
     */
    @Test
    @DisplayName("New session: should start with an empty leaderboard")
    void newSession_afterPreviousEnd_shouldHaveEmptyLeaderboard() {
        SessionManager mgr = new SessionManager();
        GameSession first = mgr.startNewSession();
        first.getLeaderBoardManager().addRound(10, 50.0f);

        mgr.endSession();
        GameSession second = mgr.startNewSession();

        assertTrue(second.getLeaderBoardManager().getLeaderBoard().isEmpty(),
            "A new session should start with an empty leaderboard");
    }

    /**
     * Uses reflection to confirm that the private field
     * {@code previousSessionId} increments with each new session start.
     */
    @Test
    @DisplayName("Internal counter: previousSessionId should increment for each new session")
    void sessionId_shouldIncrementInternallyEachTime() throws Exception {
        SessionManager mgr = new SessionManager();
        Field prevIdField = SessionManager.class.getDeclaredField("previousSessionId");
        prevIdField.setAccessible(true);

        mgr.startNewSession();
        int afterFirst = (int) prevIdField.get(mgr);

        mgr.startNewSession();
        int afterSecond = (int) prevIdField.get(mgr);

        assertEquals(afterFirst + 1, afterSecond,
            "previousSessionId should increment with each new session start");
    }

}
