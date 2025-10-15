package com.csse3200.game.records;

/**
 * this class :
 * stores the data relevant to calculate score.
 * calculates the score.
 */
public class RoundData {
    private final int currency;
    private final float time;  //in seconds
    private final int score;   // score = currency + time

    /**
     * @param currency earned in that round
     * @param time     taken to complete the round
     */
    public RoundData(int currency, float time) {
        this.currency = currency;
        this.time = time;
        this.score = calculateScore();
    }

    /**
     * this function calculates the score for the current round
     * formula: score = currency + time (as int)
     *
     * @return player's score
     */
    private int calculateScore() {
        return currency + (int) (time);
    }


    /**
     * @return the currency earned during the round
     */
    public int getCurrency() {
        return this.currency;
    }

    /**
     * @return the time taken to complete the round
     */
    public float getTime() {
        return this.time;
    }

    /**
     * @return the score for the round played
     */
    public int getScore() {
        return this.score;
    }

    @Override
    /**
     * to convert & display RoundData objects to string
     */
    public String toString() {
        return "Currency: " + currency +
                ", Time: " + time + ", " +
                "Score: " + score;
    }
}
