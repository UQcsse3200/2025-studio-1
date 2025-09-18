package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.configs.benches.BenchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BenchesTest {
    BenchConfig healthBenchConfig;
    BenchConfig speedBenchConfig;
    BenchConfig computerBenchConfig;

    @BeforeEach
    void benchSetup() {
        healthBenchConfig = Benches.HEALTH_BENCH.getConfig();
        speedBenchConfig = Benches.SPEED_BENCH.getConfig();
        computerBenchConfig = Benches.COMPUTER_BENCH.getConfig();
    }

    @Test
    void healthTest() {
        assertNotNull(healthBenchConfig);
    }

    @Test
    void computerTest() {
        assertNotNull(computerBenchConfig);
    }

    @Test
    void speedTest() {
        assertNotNull(speedBenchConfig);
    }
}
