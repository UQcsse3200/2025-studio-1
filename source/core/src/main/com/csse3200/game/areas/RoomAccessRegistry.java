package com.csse3200.game.areas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomAccessRegistry {
    private static final Map<String, Integer> roomAccessLevels = new HashMap<>();

    static {
        roomAccessLevels.put("Floor1", 0); // Starting floor, no keycard needed
        roomAccessLevels.put("Floor2", 1);
        roomAccessLevels.put("Floor3", 1);
        roomAccessLevels.put("Floor4", 1);
        roomAccessLevels.put("Floor5", 1);
        roomAccessLevels.put("Floor6", 1);
        roomAccessLevels.put("Floor7", 1);
    }

    public static int getRequiredKeycardLevel(String roomName) {
        return roomAccessLevels.getOrDefault(roomName, 0);
    }

    public static List<String> getRoomsAccessibleBy(int keycardLevel) {
        List<String> accessible = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : roomAccessLevels.entrySet()) {
            if (entry.getValue() <= keycardLevel) {
                accessible.add(entry.getKey());
            }
        }
        return accessible;
    }
}