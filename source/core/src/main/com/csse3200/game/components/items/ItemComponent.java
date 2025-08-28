package com.csse3200.game.components.items;

import com.csse3200.game.components.Component;

public class ItemComponent extends Component {
    private String name;
    private String type;
    private int id;

    public ItemComponent(String name, String type, int id) {
        this.name = name;
        this.type = type;
        this.id = id;
    }

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public int getId() { return id; }

    // Setters (optional)
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setId(int id) { this.id = id; }
}
