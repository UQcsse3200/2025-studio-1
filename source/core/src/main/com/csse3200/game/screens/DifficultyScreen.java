package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.difficultymenu.DifficultyMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;


public class DifficultyScreen extends BaseScreen {
    public DifficultyScreen(GdxGame game) {
        super(game, "images/menu_background.png");
    }

    @Override
    protected Entity createUIScreen(Stage stage) {
        return new Entity()
                .addComponent(new DifficultyMenuDisplay(game))
                .addComponent(new InputDecorator(stage, 10));
    }
}