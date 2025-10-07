package com.csse3200.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing a countdown timer in a game.
 * <p>
 *     This service allows you to track elapsed time, check remaining time,
 *      pause and resume the countdown, and determine if the timer has expired.
 * </p>
 */
public class CountdownTimerService {
    private static final Logger logger = LoggerFactory.getLogger(CountdownTimerService.class);

    /**
     * Tracks the current time in the game
     */
    private final GameTime gameTime;

    /**
     * Total duration of the countdown timer in ms
     * This value is set when the timer is created and does not change
     */
    private final long duration;

    /**
     * The timestamp in ms when the countdown timer is started
     * Updated if the timer is resumed after pause
     */
    private long startTime;

    /**
     * The timestamp in ms when the countdown timer is paused
     */
    private long pauseTime;

    /**
     * Indicates whether the countdown timer is pausing
     * True if paused, false if running
     */
    private boolean paused;

    /**
     * Creates a new countdown timer
     *
     * @param gameTime the {@link GameTime} for tracking time
     * @param durationMS the duration of the countdown in ms
     */
    public CountdownTimerService(GameTime gameTime, long durationMS) {
        this.gameTime = gameTime;
        this.duration = durationMS;
        this.startTime = gameTime.getTime();
        this.paused = false;
        this.pauseTime = 0;
        logger.debug("Setting CountdownTimerService with duration {} ms", duration);
    }

    /**
     * Gets the remaining time in ms
     *
     * <p>
     *     If the timer is paused, calculates based on the paused time;
     *     otherwise calculates based on teh current game time
     * </p>
     *
     * @return the remaining game time in ms, minimum 0
     */
    public long getRemainingMs() {
        long remaining;
        if (isPaused()) {
            long elapsed = pauseTime - startTime;
            remaining = duration - elapsed;
            logger.debug("Timer paused: elapsed time {} ms, remaining time {} ms", elapsed, remaining);
        } else {
            long elapsed = gameTime.getTime() - startTime;
            remaining = duration - elapsed;
            logger.debug("Timer runnign: elapsed time {} ms, remaining time {} ms", elapsed, remaining);
        }
        return Math.max(0, remaining);
    }

    /**
     * Checks whether the countdown is finished
     *
     * @return true if the timer has reached 0, false otherwise
     */
    public boolean isTimeUP() {
        logger.debug("Countdown Time is up: {}", getRemainingMs() <= 0);
        return getRemainingMs() <= 0;
    }

    /**
     * Pause the countdown timer.
     *
     * <p>
     *     If the time is paused, this method does nothing
     * </p>
     */
    public void pause() {
        if (!isPaused()) {
            paused = true;
            pauseTime = gameTime.getTime();
            logger.debug("Paused Countdown Timer at: {}ms", pauseTime);
        } else {
            logger.debug("Called Pause but timer is already paused");
        }
    }

    /**
     * Resume the counter timer if it was paused
     *
     * <p>
     *     If the timer is not paused, this method does nothing
     * </p>
     */
    public void resume() {
        if (paused) {
            long pausedDuration = gameTime.getTime() - pauseTime;
            startTime += pausedDuration;
            paused = false;
            logger.debug("Resume Countdown, paused duration: {}ms, new start time: {}ms", pausedDuration, startTime);
        } else {
            logger.debug("Called Resume but timer is already running");
        }
    }

    /**
     * Checks if the timer is currently paused
     *
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        logger.debug("Countdown Timer is now paused: {}", paused);
        return paused;
    }

    /**
     * Get the total duration of the countdown timer
     *
     * @return the duration in ms
     */
    public long getDuration(){
        logger.debug("Countdown Timer duration: {}", duration);
        return duration;
    }
}
