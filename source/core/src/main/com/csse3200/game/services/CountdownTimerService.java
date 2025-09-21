package com.csse3200.game.services;

public class CountdownTimerService {
    private final GameTime gameTime;
    private long duration;
    private long startTime;

    public CountdownTimerService(GameTime gameTime, long durationMS) {
        this.gameTime = gameTime;
        this.duration = durationMS;
        this.startTime = gameTime.getTime();
    }


    public long getRemainingMs() {
        long elapsed = gameTime.getTime() - startTime;
        long remaining = duration - elapsed;
        return Math.max(0, remaining);
    }

    public boolean isTimeUP() {
        return getRemainingMs() <= 0;
    }
}
