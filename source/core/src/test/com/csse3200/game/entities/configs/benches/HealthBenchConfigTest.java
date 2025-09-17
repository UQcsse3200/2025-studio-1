package com.csse3200.game.entities.configs.benches;

import com.csse3200.game.entities.configs.ItemTypes;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;

public class HealthBenchConfigTest {
    BenchConfig config;

    @BeforeEach
    void setup() {
        config = new HealthBenchConfig();
    }

    @Test
    void setupCorrect() {
        assertNotNull(config.promptText);
        assertNotNull(config.texturePath);
        Assert.assertEquals(ItemTypes.HEALTH_BENCH, config.benchType);
    }

}
