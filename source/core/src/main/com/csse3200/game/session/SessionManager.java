package com.csse3200.game.session;

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
        currentSession = new GameSession(previousSessionId++);
        return currentSession;
    }

    /**
     *
     * @return current active session
     */
    public GameSession getCurrentSession() {
        return currentSession;
    }

    /**
     * ends the current session
     */
    public void endSession() {
        currentSession = null;
    }
}
