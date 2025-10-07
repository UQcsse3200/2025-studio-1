package com.csse3200.game.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimefromSecondsTest {

    @Test
    void testToMMSS() {
        assertEquals("00:00", TimefromSeconds.toMMSS(0));
        assertEquals("00:59", TimefromSeconds.toMMSS(59));
        assertEquals("01:00", TimefromSeconds.toMMSS(60));
        assertEquals("02:30", TimefromSeconds.toMMSS(150));
        assertEquals("10:05", TimefromSeconds.toMMSS(605));
        assertEquals("00:00", TimefromSeconds.toMMSS(-5)); // edge case negative input
    }
}
