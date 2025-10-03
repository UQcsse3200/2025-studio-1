package com.csse3200.game.session;

/**
 * = GameSession represents one complete run.
 * = It should be viewed as a container that contains the leaderboard
 * for that particular session, identified by sessionID.
 */
public class GameSession {
    private LeaderBoardManager leaderBoardManager; //to keep track of all rounds during the session
    private int sessionId; //unique id to distinguish each session

    /**
     * To create a fresh leaderboard for each session
     * @param sessionId is the unique id assigned to that session.
     */
    public GameSession(int sessionId) {
        this.sessionId = sessionId;
        this.leaderBoardManager = new LeaderBoardManager();
    }

    /**
     *
     * @return returns the leaderboard data for the session.
     */
    public LeaderBoardManager getLeaderBoardManager() {return leaderBoardManager;}

    /**
     *
     * @return sessionID to uniquely identify it from others.
     */
    public int getSessionId() {return sessionId;}
}
