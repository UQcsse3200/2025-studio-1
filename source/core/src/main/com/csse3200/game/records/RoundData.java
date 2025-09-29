package com.csse3200.game.records;

public class RoundData {
    private int currency;
    private float time;  //in seconds
    private int score;   // score = currency + time

    public  RoundData(int currency, float time) {
        this.currency = currency;
        this.time = time;
        this.score = calculateScore();
    }

    /**
     * this function calculates the score for the current round
     * formula: score = currency + time (as int)
     * @return player's score
     */
    private int calculateScore() {
        return currency + (int)(time);
    }

    public int getCurrency() {return currency;}
    public float getTime() {return time;}
    public int getScore() {return score;}

    @Override
    public String toString() {
        return "Currency: " + currency +
            ", Time: " + time + ", " +
            "Score: " + score;
    }
}
