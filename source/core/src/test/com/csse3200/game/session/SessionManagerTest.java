package com.csse3200.game.session;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SessionManagerTest {

    @Test
    void startNewSession_createsAndSetsCurrentSession() {
        SessionManager mgr = new SessionManager();

        GameSession s1 = mgr.startNewSession();

        assertNotNull(s1, "startNewSession should return a new GameSession");
        assertSame(s1, mgr.getCurrentSession(), "Current session should be set to the newly created one");
    }

    @Test
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

    @Test
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

    @Test
    void endSession_whenNoActiveSession_isNoOp() {
        SessionManager mgr = new SessionManager();

        // No session started; should not throw
        assertDoesNotThrow(mgr::endSession, "Ending a non-existent session should be a no-op");
        assertNull(mgr.getCurrentSession(), "There should still be no active session");
    }

    @Test
    void startNewSession_afterEndingPrevious_createsFreshSession() {
        SessionManager mgr = new SessionManager();

        GameSession first = mgr.startNewSession();
        mgr.endSession();

        GameSession second = mgr.startNewSession();

        assertNotNull(second, "Should create a new session after ending the previous");
        assertNotSame(first, second, "New session instance should differ from the ended one");
        assertSame(second, mgr.getCurrentSession(), "Current session should be the newly created one");
    }
}
