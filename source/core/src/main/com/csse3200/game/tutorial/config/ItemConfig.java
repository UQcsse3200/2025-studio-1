package com.csse3200.game.tutorial.config;

import java.util.Map;

public record ItemConfig(
        String type,
        String sprite,
        Double width,
        Double height,
        Map<String, Object> defaults
) {
}
