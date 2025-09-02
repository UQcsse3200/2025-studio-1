package com.csse3200.game.components;

import java.util.HashSet;
import java.util.Set;

public class InventoryComponent extends Component {
    private final Set<String> items = new HashSet<>();
    private int keycardLevel = 0; // 0 = no keycard


    public void addItem(String itemId) {
        items.add(itemId);
    }

    public boolean hasItem(String itemId) {
        return items.contains(itemId);
    }


    public void setKeycardLevel(int level) {
        this.keycardLevel = level;
    }

    public int getKeycardLevel() {
        return keycardLevel;
    }

    public boolean hasRequiredKeycard(int requiredLevel) {
        return keycardLevel >= requiredLevel;
    }
}