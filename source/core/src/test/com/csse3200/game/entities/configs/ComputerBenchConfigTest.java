package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.entities.configs.benches.ComputerBenchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ComputerBenchConfigTest {
    BenchConfig config;
    @BeforeEach
    void setup() {
        config = new ComputerBenchConfig();
    }

    @Test
    void setupCorrect() {
        assertNotNull(config.promptText);
        assertNotNull(config.texturePath);
        assertEquals(ItemTypes.COMPUTER_BENCH, config.benchType);
    }

}
