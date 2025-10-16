package com.csse3200.game.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CountdownTimerService}.
 * These tests cover timer initialization, time progression, pausing/resuming, and time-up conditions.
 */
class CountdownTimerServiceTest {

    private static class FakeGameTime extends GameTime {
        private long currentTime;

        public FakeGameTime(long startTime) {
            this.currentTime = startTime;
        }

        @Override
        public long getTime() {
            return currentTime;
        }

        public void addTime(long ms) {
            currentTime += ms;
        }
    }

    private FakeGameTime gameTime;
    private CountdownTimerService timer;

    @BeforeEach
    void setUp() {
        gameTime = new FakeGameTime(0);
        timer = new CountdownTimerService(gameTime, 10000); // 10 seconds
    }

    @Test
    void testStartTimer(){
        assertFalse(timer.getIsRunning(), "Timer should not be running at start");
        assertEquals(10000, timer.getRemainingMs(), "Full duration before start: 5 seconds");
        timer.startTimer();
        assertTrue(timer.getIsRunning(), "Timer should be running");
    }



    @Test
    void testInitialState() {
        timer.startTimer();
        assertEquals(10000, timer.getDuration());
        assertFalse(timer.isPaused());
        assertEquals(10000, timer.getRemainingMs());
        assertFalse(timer.isTimeUP());
    }

    @Test
    void testTimeProgression() {
        timer.startTimer();
        gameTime.addTime(3000); // 3 seconds later
        assertTrue(timer.getRemainingMs() <= 7000 && timer.getRemainingMs() > 6900);
        assertFalse(timer.isTimeUP());
    }

    @Test
    void testPauseAndResume() {
        timer.startTimer();
        gameTime.addTime(4000);
        timer.pause();
        assertTrue(timer.isPaused());
        long remainingAtPause = timer.getRemainingMs();


        gameTime.addTime(5000);
        assertEquals(remainingAtPause, timer.getRemainingMs());

        // Resume timer
        timer.resume();
        assertFalse(timer.isPaused());

        // increase 3 more seconds
        gameTime.addTime(3000);
        assertTrue(timer.getRemainingMs() < remainingAtPause);
    }

    @Test
    void testPauseWhenAlreadyPausedDoesNothing() {
        timer.startTimer();
        timer.pause();
        long pauseTime = timer.getRemainingMs();
        timer.pause();
        assertEquals(pauseTime, timer.getRemainingMs());
    }

    @Test
    void testResumeWhenNotPausedDoesNothing() {
        timer.startTimer();
        long before = timer.getRemainingMs();
        timer.resume();
        long after = timer.getRemainingMs();
        assertEquals(before, after);
    }

    @Test
    void testTimeUpCondition() {
        timer.startTimer();
        gameTime.addTime(12000);
        assertEquals(0, timer.getRemainingMs());
        assertTrue(timer.isTimeUP());
    }

    @Test
    void testRemainingNeverNegative() {
        timer.startTimer();
        gameTime.addTime(999999);
        assertEquals(0, timer.getRemainingMs());
        assertTrue(timer.isTimeUP());
    }
}

