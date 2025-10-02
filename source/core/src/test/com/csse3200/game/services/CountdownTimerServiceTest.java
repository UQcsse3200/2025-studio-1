package com.csse3200.game.services;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class CountdownTimerServiceTest {
    GameTime mockGameTime;
    CountdownTimerService timer;

    @BeforeEach
    void beforeEach() {
        mockGameTime = mock(GameTime.class);
        when(mockGameTime.getTime()).thenReturn(0L);
        timer = new CountdownTimerService(mockGameTime, 5000);
    }

    @Test
    void testStartingGameRemainingTime() {
        assertEquals(5000, timer.getRemainingMs(), "Full duration at start: 5 seconds");
        assertFalse(timer.isTimeUP(), "Timer should report time is not up");
    }
    @Test
    void testCountdown() {
        when(mockGameTime.getTime()).thenReturn(2000L);
        assertEquals(3000, timer.getRemainingMs(), "Remaining time: 3 seconds");
        assertFalse(timer.isTimeUP(), "Timer should report time is not up");
    }

    @Test
    void testTimesUp() {
        when(mockGameTime.getTime()).thenReturn(5000L);
        assertEquals(0, timer.getRemainingMs(), "Remaining time: 0 seconds");
        assertTrue(timer.isTimeUP(), "Timer should report time is up");
    }

    @Test
    void testHandleNegativeRemainingTime() {
        when(mockGameTime.getTime()).thenReturn(6000L);
        assertEquals(0, timer.getRemainingMs(), "Remaining should not be negative");
        assertTrue(timer.isTimeUP(), "Timer should report time is up");
    }

    @Test
    void testPauseAndResumeTimer() {
        when(mockGameTime.getTime()).thenReturn(2000L);
        assertEquals(3000, timer.getRemainingMs(), "Remaining time: 3 seconds");
        timer.pause();

        when(mockGameTime.getTime()).thenReturn(4000L);
        assertEquals(3000, timer.getRemainingMs(), "Timer Paused, Remaining time does not change: 3 seconds");

        timer.resume();
        when(mockGameTime.getTime()).thenReturn(6000L);
        assertEquals(1000, timer.getRemainingMs(), "Timer Resumed, Remaining time: 1 seconds");

    }

    @Test
    void testIsPausedFlag() {
        assertFalse(timer.isPaused(), "Start game not paused");
        timer.pause();
        assertTrue(timer.isPaused(), "Should be paused after pause()");
        timer.resume();
        assertFalse(timer.isPaused(), "Should not be paused after resume()");
    }

    @Test
    void testGetDuration() {
        assertEquals(5000, timer.getDuration(), "Duration should match constructor input");
    }
}
