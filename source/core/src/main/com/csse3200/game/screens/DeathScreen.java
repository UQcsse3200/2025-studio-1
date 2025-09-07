package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.DeathScreenDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing the death screen.
 */
public class DeathScreen extends BaseEndScreen {
    private static final Logger logger = LoggerFactory.getLogger(DeathScreen.class);

    /**
     * Creates a new DeathScreen instance
     * Registers services, creates the renderer, loads assets, and builds the UI.
     *
     * @param game the {@link GdxGame} instance
     */
    public DeathScreen(GdxGame game) {
        super(game);
    }

    @Override
    protected Entity createUIScreen() {
        Stage stage = com.csse3200.game.services.ServiceLocator.getRenderService().getStage();
        return new Entity()
                .addComponent(new DeathScreenDisplay(game))
                .addComponent(new InputDecorator(stage, 10));
    }
}
