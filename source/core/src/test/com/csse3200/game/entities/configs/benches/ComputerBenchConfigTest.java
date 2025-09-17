package com.csse3200.game.entities.configs.benches;

import com.csse3200.game.entities.configs.ItemTypes;
import org.junit.Assert;
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
        Assert.assertEquals(ItemTypes.COMPUTER_BENCH, config.benchType);
    }
}
