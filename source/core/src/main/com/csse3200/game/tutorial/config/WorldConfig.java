package com.csse3200.game.tutorial.config;

import java.util.List;
import java.util.Map;

public record WorldConfig(
        int version,
        List<RoomConfig> rooms,
        Map<String, ItemConfig> items
) {
}