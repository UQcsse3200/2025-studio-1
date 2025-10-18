package com.csse3200.game.session;


import com.csse3200.game.records.RoundData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * this class stores the data for each in the leaderboard as an arraylist
 */

public class LeaderBoardManager {
    private static final Logger logger = LoggerFactory.getLogger(LeaderBoardManager.class);

    private List<RoundData> leaderBoard;

    /**
     * creates a new arraylist for each session
     */
    public LeaderBoardManager() {
        leaderBoard = new ArrayList<>();
    }

    /**
     * @param currency left in the round
     * @param time left in the round that will be added to the high score
     */
    public void addRound(int currency, float time) {
        logger.info("LeaderBoardManager.addRound currency={} time={}", currency, time);
        leaderBoard.add(new RoundData(currency, time));
        //sort leaderboard from highest to lowest score
        leaderBoard.sort((round1, round2) -> Integer.compare(round2.getScore(), round1.getScore()));
    }

    /**
     * @return the leaderboard as a list with each round's
     * data until then since the beginning of the session
     */
    public List<RoundData> getLeaderBoard() {
        return this.leaderBoard;
    }

    /**
     * if loadedLeaderboard != null --> leaderboard = loadedLeaderboard
     * else create a new list to avoid NullPointException
     *
     * @param loadedLeaderboard is a list of leaderboard data that is to be loaded
     */
    public void setLeaderboard(List<RoundData> loadedLeaderboard) {
        this.leaderBoard = loadedLeaderboard != null ? loadedLeaderboard : new ArrayList<>();
        // Ensure sorting after loading
        this.leaderBoard.sort((round1, round2) -> Integer.compare(round2.getScore(), round1.getScore()));
    }

    /**
     * clears the leaderboard at the end of each session
     */
    public void reset() {
        this.leaderBoard.clear();
    }
}
