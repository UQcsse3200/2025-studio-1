package com.csse3200.game.components;

import java.util.HashSet;
import java.util.Set;

public class InventoryComponent extends Component {
    private final Set<String> items = new HashSet<>();

    public void addItem(String itemId) {
        items.add(itemId);
    }

    public boolean hasItem(String itemId) {
        return items.contains(itemId);
    }
}