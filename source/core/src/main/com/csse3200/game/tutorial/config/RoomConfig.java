package com.csse3200.game.tutorial.config;

import java.util.List;
import java.util.Map;

public record RoomConfig(
        String id,
        String music,
        List<SpawnPoint> spawns,
        List<ItemRef> items,
        List<DoorConfig> doors
) {
    public record SpawnPoint(String name, double x, double y) {
    }

    public record ItemRef(
            String ref,
            double x, double y,
            Double rot,
            Double scale,
            Integer layer,
            Map<String, Object> params
    ) {
    }

    public record DoorConfig(
            String name,
            Aabb trigger,
            String targetRoom,
            String targetSpawn
    ) {
        public record Aabb(double x, double y, double w, double h) {
        }
    }
}