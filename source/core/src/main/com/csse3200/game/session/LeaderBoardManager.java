package com.csse3200.game.session;

import com.csse3200.game.records.RoundData;

import java.util.ArrayList;
import java.util.List;

/**
 * this class stores the data for each in the leaderboard as an arraylist
 */

public class LeaderBoardManager {
    private final List<RoundData> leaderBoard;

    /**
     * creates a new arraylist for each session
     */
    public LeaderBoardManager() {
        leaderBoard = new ArrayList<>();
    }

    /**
     *
     * @param currency earned in the round
     * @param time taken to complete the round
     */
    public void addRound(int currency, float time){
        leaderBoard.add(new RoundData(currency, time));
    }

    /**
     *
     * @return the leaderboard as a list with each round's
     * data until then since the beginning of the session
     */
    public List<RoundData> getLeaderBoard() {return leaderBoard;}

    /**
     * clears the leaderboard at the end of each session
     */
    public void reset(){leaderBoard.clear();}
}
