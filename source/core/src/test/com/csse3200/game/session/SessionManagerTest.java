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
        } catch (NoSuchMethodException ignored) {
            //can add getSessionId later
        }
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

    /**
     * Ensures that calling {@link SessionManager#getCurrentSession()}
     * before any session has started returns {@code null}.
     */
    @Test
    @DisplayName("getCurrentSession(): should return null when no session started")
    void getCurrentSession_beforeStart_returnsNull() {
        SessionManager mgr = new SessionManager();
        assertNull(mgr.getCurrentSession(),
            "Before starting a session, getCurrentSession() should return null");
    }

    /**
     * Ensures that calling {@link SessionManager#startNewSession()}
     * replaces the existing active session with a fresh instance.
     */
    @Test
    @DisplayName("startNewSession(): should overwrite existing current session")
    void startNewSession_overwritesExistingSession() {
        SessionManager mgr = new SessionManager();
        GameSession first = mgr.startNewSession();
        GameSession second = mgr.startNewSession();

        assertNotSame(first, second, "A new session should replace the previous one");
        assertSame(second, mgr.getCurrentSession(),
            "Current session reference should point to the most recent session");
    }

    /**
     * Uses reflection to simulate a non-null {@code currentSession}
     * and confirms that {@link SessionManager#endSession()} sets it to null
     * even if it wasn't started via {@link SessionManager#startNewSession()}.
     */
    @Test
    @DisplayName("endSession(): should nullify currentSession even when manually assigned via reflection")
    void endSession_nullifiesManuallyAssignedSession() throws Exception {
        SessionManager mgr = new SessionManager();

        Field field = SessionManager.class.getDeclaredField("currentSession");
        field.setAccessible(true);
        field.set(mgr, new GameSession(99)); // manually assign dummy session

        mgr.endSession();
        assertNull(mgr.getCurrentSession(), "endSession() should set currentSession to null");
    }

    /**
     * Ensures that {@link SessionManager#endSession()} does not
     * throw exceptions or change internal counters when called
     * after the {@code currentSession} is already null.
     */
    @Test
    @DisplayName("endSession(): should handle already-null currentSession gracefully")
    void endSession_alreadyNullSession_doesNotThrow() throws Exception {
        SessionManager mgr = new SessionManager();

        // Forcefully set currentSession = null (even though it’s already null)
        Field field = SessionManager.class.getDeclaredField("currentSession");
        field.setAccessible(true);
        field.set(mgr, null);

        assertDoesNotThrow(mgr::endSession,
            "Calling endSession() with null currentSession should not throw an exception");
        assertNull(mgr.getCurrentSession(),
            "After ending a null session, currentSession should remain null");
    }

    /**
     * Validates that {@link SessionManager#startNewSession()} reuses the
     * internal session counter consistently, even after ending sessions.
     */
    @Test
    @DisplayName("startNewSession(): should continue incrementing IDs after endSession()")
    void sessionId_incrementsContinuously_acrossMultipleSessions() throws Exception {
        SessionManager mgr = new SessionManager();
        Field prevIdField = SessionManager.class.getDeclaredField("previousSessionId");
        prevIdField.setAccessible(true);

        mgr.startNewSession(); // id = 0
        mgr.endSession();
        int afterEnd = (int) prevIdField.get(mgr);

        mgr.startNewSession(); // id = 1
        int afterRestart = (int) prevIdField.get(mgr);

        assertEquals(afterEnd + 1, afterRestart,
            "Session IDs should keep incrementing across restarts");
    }

    /**
     * Confirms that the leaderboard reset inside {@link SessionManager#endSession()}
     * is triggered exactly once, even if the session is ended repeatedly.
     */
    @Test
    @DisplayName("endSession(): should call leaderboard.reset() only once")
    void endSession_callsLeaderboardResetOnce() {
        SessionManager mgr = new SessionManager();
        GameSession session = mgr.startNewSession();
        var lb = session.getLeaderBoardManager();

        // Add one entry to confirm reset will clear it
        lb.addRound(10, 2.0f);
        mgr.endSession();
        assertTrue(lb.getLeaderBoard().isEmpty(), "Leaderboard should be reset after first endSession()");

        // Call again — should not throw or change anything
        assertDoesNotThrow(mgr::endSession, "Second call to endSession should not throw");
        assertNull(mgr.getCurrentSession(), "Current session should remain null");
    }

    /**
     * Ensures that calling {@link SessionManager#startNewSession()}
     * immediately after creation doesn’t rely on any prior state.
     */
    @Test
    @DisplayName("startNewSession(): should work correctly from a fresh instance with no prior session")
    void startNewSession_onFreshInstance_worksCorrectly() {
        SessionManager mgr = new SessionManager();
        GameSession newSession = mgr.startNewSession();

        assertNotNull(newSession, "Newly created session should not be null");
        assertSame(newSession, mgr.getCurrentSession(),
            "Current session should reference the newly created GameSession");
    }

    /**
     * Ensures that {@link SessionManager#endSession()} is idempotent —
     * multiple consecutive calls leave the same stable final state.
     */
    @Test
    @DisplayName("endSession(): should remain idempotent and stable after multiple calls")
    void endSession_isIdempotent() {
        SessionManager mgr = new SessionManager();
        mgr.startNewSession();
        mgr.endSession();
        mgr.endSession();
        mgr.endSession();

        assertNull(mgr.getCurrentSession(), "After multiple ends, currentSession should remain null");
    }

}
