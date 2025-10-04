package com.csse3200.game.components.gamearea;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.ui.UIComponent;

/**
 * Simple UI label showing the current floor name.
 */
public class FloorLabelDisplay extends UIComponent {
    private final String floorName;
    private Table table;

    public FloorLabelDisplay(String floorName) {
        this.floorName = floorName;
    }

    @Override
    public void create() {
        super.create();
        table = new Table();
        table.top().left();
        table.setFillParent(true);
        // Position below game area display with clear spacing
        table.padTop(30f).padLeft(10f);

        Label label = new Label(floorName, skin, "white");
        table.add(label);

        stage.addActor(table);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // No-op: Scene2D handles UI drawing. Kept to satisfy RenderComponent contract.
    }

    @Override
    public void dispose() {
        super.dispose();
        if (table != null) {
            table.clear();
            table.remove();
        }
    }
}


