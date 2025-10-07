package com.csse3200.game.utils;

public class TimefromSeconds {
    /**
     * Converts total seconds into MM:SS format.
     * Example: 150 -> "02:30"
     */
    public static String toMMSS(int totalSeconds) {
        int m = Math.max(0, totalSeconds) / 60;
        int s = Math.max(0, totalSeconds) % 60;
        return String.format("%02d:%02d", m, s);
    }
}
