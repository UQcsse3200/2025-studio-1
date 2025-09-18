package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.settingsmenu.SettingsMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;


public class SettingsScreen extends BaseScreen {
    public SettingsScreen(GdxGame game) {
        super(game, "images/menu_background.png");
    }

    @Override
    protected Entity createUIScreen(Stage stage) {
        return new Entity()
                .addComponent(new SettingsMenuDisplay(game))
                .addComponent(new InputDecorator(stage, 10));
    }
}