package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.entities.configs.benches.ComputerBenchConfig;
import com.csse3200.game.entities.configs.benches.HealthBenchConfig;
import com.csse3200.game.entities.configs.benches.SpeedBenchConfig;

public enum Benches {
    HEALTH_BENCH(new HealthBenchConfig()),
    COMPUTER_BENCH(new ComputerBenchConfig()),
    SPEED_BENCH(new SpeedBenchConfig());

    private final BenchConfig config;

    Benches(BenchConfig config) {
        this.config = config;
    }

    public BenchConfig getConfig() {
        return config;
    }
}
