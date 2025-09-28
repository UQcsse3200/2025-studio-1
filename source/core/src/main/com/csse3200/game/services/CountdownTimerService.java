package com.csse3200.game.services;

public class CountdownTimerService {
    private final GameTime gameTime;
    private long duration;
    private long startTime;
    private long pauseTime;
    private boolean paused;

    public CountdownTimerService(GameTime gameTime, long durationMS) {
        this.gameTime = gameTime;
        this.duration = durationMS;
        this.startTime = gameTime.getTime();
        this.paused = false;
        this.pauseTime = 0;
    }


    public long getRemainingMs() {
        if (isPaused()) {
            long elapsed = pauseTime - startTime;
            long remaining = duration - elapsed;
            return Math.max(0, remaining);
        } else {
            long elapsed = gameTime.getTime() - startTime;
            long remaining = duration - elapsed;
            return Math.max(0, remaining);
        }
    }

    public boolean isTimeUP() {
        return getRemainingMs() <= 0;
    }

    public void pause() {
        if (!isPaused()) {
            paused = true;
            pauseTime = gameTime.getTime();
        }
    }

    public void resume() {
        if (paused) {
            long pausedDuration = gameTime.getTime() - pauseTime;
            startTime += pausedDuration;
            paused = false;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public long getDuration(){
        return duration;
    }
}
