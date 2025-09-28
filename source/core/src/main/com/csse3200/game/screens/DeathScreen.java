package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.BaseEndScreenDisplays;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ServiceLocator;

/**
 * Screen shown when the player is defeated.
 * <p>
 * This class extends {@link BaseScreen} and provides the
 * specific UI entity for the death scenario. The UI includes:
 * <ul>
 *   <li>A "Defeated" title</li>
 *   <li>Round and elapsed time labels</li>
 *   <li>Buttons for retrying the game or returning to the main menu</li>
 * </ul>
 * <p>
 * Common lifecycle management (service registration, renderer setup,
 * asset loading/unloading, and background creation) is handled by {@link BaseScreen}.
 */
public class DeathScreen extends BaseScreen {

    /**
     * Constructs a new DeathScreen instance.
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
    public DeathScreen(GdxGame game) {
        super(game, "images/menu_background.png");
    }

    /**
     * Provides the UI entity for the death screen.
     * <p>
     * This entity includes:
     * <ul>
     *   <li>{@link BaseEndScreenDisplays} — the defeat UI (title, round/time labels, buttons)</li>
     *   <li>{@link InputDecorator} — captures and forwards input events to the stage</li>
     * </ul>
     *
     * @return the UI {@link Entity} for the death screen
     */
    @Override
    protected Entity createUIScreen(Stage stage) {
        return new Entity()
                .addComponent(BaseEndScreenDisplays.defeated(game).withLeaderboard(() -> game.setScreen(GdxGame.ScreenType.LEADERBOARD)))
                .addComponent(new InputDecorator(stage, 10));
    }
}
