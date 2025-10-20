package com.csse3200.game.components.gamearea;

import com.badlogic.gdx.graphics.Color;
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
        table.padTop(30f).padLeft(10f);

        Label label = new Label(floorName, skin, "white");

        // Create panel
        Table panel = new Table();
        panel.left();
        panel.setBackground(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.3f)));
        panel.pad(4f, 4f, 0f, 8f);
        panel.add(label).left();

        table.add(panel).width(320f);
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


