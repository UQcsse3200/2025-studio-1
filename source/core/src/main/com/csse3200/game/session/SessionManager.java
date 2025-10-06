package com.csse3200.game.session;
import com.csse3200.game.session.LeaderBoardManager;

/**
 * SessionManager is responsible for :
 * = starting a new session when the player begins a run.
 * = Keeping track of the current active session.
 * = Ending a session when the player quits or the game is over.
 */
public class SessionManager {
    private GameSession currentSession; //stores the currently active session.
    private int previousSessionId = 0; //session id of the last session

    /**
     *
     * @return the newly created session
     */
    public GameSession startNewSession() {
        this.currentSession = new GameSession(previousSessionId++);
        return this.currentSession;
    }

    /**
     *
     * @return current active session
     */
    public GameSession getCurrentSession() {
        return this.currentSession;
    }

    /**
     * if : the current session is not null --> reset leaderboard
     * else : set currentSession as null
     */
    public void endSession() {
        if (this.currentSession != null) {
            this.currentSession.getLeaderBoardManager().reset(); // reset leaderboard
        }
        this.currentSession = null;
    }
}
