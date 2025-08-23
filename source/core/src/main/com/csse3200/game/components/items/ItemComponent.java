package com.csse3200.game.components.items;

import com.csse3200.game.components.Component;

public class ItemComponent extends Component {
    private String name;
    private int id;
    private String type;

    public ItemComponent(String name, int id, String type) {
      this.name = name;
      this.id = id;
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
