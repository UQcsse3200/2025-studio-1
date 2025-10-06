package com.csse3200.game.components.minigames.slots;

import java.util.List;
import java.util.Random;

public class SlotsText {
    public List<String> messages;
    private final Random random = new Random();

    public String getRandom() {
        if (messages == null || messages.isEmpty()) return null;
        return messages.get(random.nextInt(messages.size()));
    }
}
