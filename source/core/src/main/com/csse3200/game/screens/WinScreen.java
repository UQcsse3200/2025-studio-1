package com.csse3200.game.screens;


import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.BaseEndScreenDisplays;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ServiceLocator;

/**
 * Screen shown when the player achieves victory.
 * <p>
 * This class extends {@link BaseScreen} and provides the
 * specific UI entity for the win scenario. The UI includes:
 * <ul>
 *   <li>A "Victory" title</li>
 *   <li>Round and elapsed time labels</li>
 *   <li>Buttons for continuing the game or returning to the main menu</li>
 * </ul>
 * <p>
 * Common lifecycle management (services, renderer, asset loading/unloading,
 * and background creation) is handled by {@link BaseScreen}.
 */
public class WinScreen extends BaseScreen {
    /**
     * Constructs a new WinScreen instance.
     * <p>
     * This will:
     * <ul>
     *   <li>Register services with {@link ServiceLocator}</li>
     *   <li>Create a renderer and position its camera</li>
     *   <li>Load required assets</li>
     *   <li>Build the UI (via {@link #createUIScreen(Stage stage)})</li>
     * </ul>
     *
     * @param game the {@link GdxGame} instance, used for screen navigation
     */
    public WinScreen(GdxGame game) {
        super(game, "images/win_screen_background.png");
    }

    /**
     * Provides the UI entity for the win screen.
     * <p>
     * This entity includes:
     * <ul>
     *   <li>{@link BaseEndScreenDisplays} — the victory UI (title, round/time labels, buttons)</li>
     *   <li>{@link InputDecorator} — captures and forwards input events to the stage</li>
     * </ul>
     *
     * @return the UI {@link Entity} for the win screen
     */
    @Override
    protected Entity createUIScreen(Stage stage) {
        return new Entity()
                .addComponent(BaseEndScreenDisplays.victory(game))
                .addComponent(new InputDecorator(stage, 10));
    }
}

