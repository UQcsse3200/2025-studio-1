package com.csse3200.game.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
        Texture overlayTexture = new Texture("images/menu_background.png");
        Image darkOverlay = new Image(overlayTexture);
        darkOverlay.setFillParent(true);
        darkOverlay.setColor(new Color(0f, 0f, 0f, 0.3f));
        stage.addActor(darkOverlay);
        return new Entity()
                .addComponent(new SettingsMenuDisplay(game))
                .addComponent(new InputDecorator(stage, 10));
    }
}