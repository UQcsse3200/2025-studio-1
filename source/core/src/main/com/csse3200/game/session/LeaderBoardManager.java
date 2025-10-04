package com.csse3200.game.session;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.records.RoundData;
import com.csse3200.game.components.player.InventoryComponent;
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
     *
     * @param currency earned in the round
     * @param time taken to complete the round
     */
    public void addRound(int currency, float time){
        logger.info("LeaderBoardManager.addRound currency={} time={}", currency, time);
        leaderBoard.add(new RoundData(currency, time));
    }

    /**
     *
     * @return the leaderboard as a list with each round's
     * data until then since the beginning of the session
     */
    public List<RoundData> getLeaderBoard() {return leaderBoard;}

    /**
     * if loadedLeaderboard != null --> leaderboard = loadedLeaderboard
     * else create a new list to avoid NullPointException
     * @param loadedLeaderboard is a list of leaderboard data that is to be loaded
     */
    public void setLeaderboard(List<RoundData> loadedLeaderboard) {
        leaderBoard = loadedLeaderboard != null ? loadedLeaderboard : new ArrayList<>();
    }

    /**
     * clears the leaderboard at the end of each session
     */
    public void reset(){leaderBoard.clear();}
}
