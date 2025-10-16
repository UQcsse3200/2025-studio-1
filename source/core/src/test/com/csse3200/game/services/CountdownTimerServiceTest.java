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

    @Test
    void testStartTimer(){
        assertFalse(timer.getIsRunning(), "Timer should not be running at start");
        assertEquals(5000, timer.getRemainingMs(), "Full duration before start: 5 seconds");
        timer.startTimer();
        assertTrue(timer.getIsRunning(), "Timer should be running");
    }

    @Test
    void testStartingGameRemainingTime() {
        timer.startTimer();
        assertEquals(5000, timer.getRemainingMs(), "Full duration at start: 5 seconds");
        assertFalse(timer.isTimeUP(), "Timer should report time is not up");
    }
    @Test
    void testCountdown() {
        timer.startTimer();
        when(mockGameTime.getTime()).thenReturn(2000L);
        assertEquals(3000, timer.getRemainingMs(), "Remaining time: 3 seconds");
        assertFalse(timer.isTimeUP(), "Timer should report time is not up");
    }

    @Test
    void testTimesUp() {
        timer.startTimer();
        when(mockGameTime.getTime()).thenReturn(5000L);
        assertEquals(0, timer.getRemainingMs(), "Remaining time: 0 seconds");
        assertTrue(timer.isTimeUP(), "Timer should report time is up");
    }

    @Test
    void testHandleNegativeRemainingTime() {
        timer.startTimer();
        when(mockGameTime.getTime()).thenReturn(6000L);
        assertEquals(0, timer.getRemainingMs(), "Remaining should not be negative");
        assertTrue(timer.isTimeUP(), "Timer should report time is up");
    }

    @Test
    void testPauseAndResumeTimer() {
        timer.startTimer();
        when(mockGameTime.getTime()).thenReturn(2000L);
        assertEquals(3000, timer.getRemainingMs(), "Remaining time: 3 seconds");
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
    void testIsPausedFlag() {
        timer.startTimer();
        assertFalse(timer.isPaused(), "Start game not paused");
        timer.pause();
        assertEquals(pauseTime, timer.getRemainingMs());
    }

    @Test
    void testResumeWhenNotPausedDoesNothing() {
        long before = timer.getRemainingMs();
        timer.resume();
        long after = timer.getRemainingMs();
        assertEquals(before, after);
    }

    @Test
    void testTimeUpCondition() {
        gameTime.addTime(12000);
        assertEquals(0, timer.getRemainingMs());
        assertTrue(timer.isTimeUP());
    }

    @Test
    void testRemainingNeverNegative() {
        gameTime.addTime(999999);
        assertEquals(0, timer.getRemainingMs());
        assertTrue(timer.isTimeUP());
    }
}

