package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the game time
 */
public class GameTime {
    private static final Logger logger = LoggerFactory.getLogger(GameTime.class);
    private final long startTime;
    private float timeScale = 1f;
    private boolean paused = false;
    private long delayedKeycardTime = -1;
    private Runnable delayedKeycardAction;
    public GameTime() {
        startTime = TimeUtils.millis();
        logger.debug("Setting game start time to {}", startTime);
    }

    /**
     * Sets the status of the game to be paused (true) or not (false).
     *
     * @param paused True if the game is currently paused, False otherwise
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * Gets the current pause state of the game
     *
     * @return True if the game is paused, false if not
     */
    public boolean isPaused() {
        return this.paused;
    }

    /**
     * Set the speed of time passing. This affects getDeltaTime()
     *
     * @param timeScale Time scale, where normal speed is 1.0, no time passing is 0.0
     */
    public void setTimeScale(float timeScale) {
        logger.debug("Setting time scale to {}", timeScale);
        this.timeScale = timeScale;
    }

    /**
     * @return time passed since the last frame in seconds, scaled by time scale.
     */
    public float getDeltaTime() {
        if (paused)
            return 0;
        if (delayedKeycardAction != null && getTime() >= delayedKeycardTime) {
            delayedKeycardAction.run();
            delayedKeycardAction = null;
            delayedKeycardTime = -1;
        }
        return Gdx.graphics.getDeltaTime() * timeScale;
    }

    /**
     * @return time passed since the last frame in seconds, not affected by time scale.
     */
    public float getRawDeltaTime() {
        return Gdx.graphics.getDeltaTime();
    }

    /**
     * @return time passed since the game started in milliseconds
     */
    public long getTime() {
        return TimeUtils.timeSinceMillis(startTime);
    }

    public void delayKeycardSpawn(float delaySeconds, Runnable action) {
        delayedKeycardTime = getTime() + (long)(delaySeconds * 1000);
        delayedKeycardAction = action;
    }

    public long getTimeSince(long lastTime) {
        return getTime() - lastTime;
    }
}
