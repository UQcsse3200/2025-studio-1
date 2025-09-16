package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.screens.TutorialClip;
import com.csse3200.game.components.screens.TutorialScreenDisplay;
import com.csse3200.game.components.screens.TutorialStep;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ServiceLocator;

import java.util.List;

/**
 * Screen shown for tutorial system.
 * <p>
 * This class extends {@link BaseScreen} and provides the
 * specific UI entity for the win scenario. The UI includes:
 * <ul>
 *   <li>A title</li>
 *   <li>A description</li>
 *   <li>An animated clip</li>
 * </ul>
 * <p>
 * Common lifecycle management (services, renderer, asset loading/unloading,
 * and background creation) is handled by {@link BaseScreen}.
 */
public class TutorialScreen extends BaseScreen {
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
    public TutorialScreen(GdxGame game) {
        super(game, "images/background.png");
    }

    /**
     * Provides the UI entity for the tutorial screen.
     * <p>
     * This entity includes:
     * <ul>
     *   <li>{@link TutorialScreenDisplay} — the tutorial UI (title, description, animated clip and buttons)</li>
     *   <li>{@link InputDecorator} — captures and forwards input events to the stage</li>
     * </ul>
     *
     * @return the UI {@link Entity} for the win screen
     */
    @Override
    protected Entity createUIScreen(Stage stage) {
        TutorialClip moveClip = new TutorialClip("images/tutorial/move", "frame_%04d.png", 25, 12f, true);
        List<TutorialStep> steps = List.of(
                new TutorialStep("Welcome!", "Use AD and space to move your character.",
                        moveClip),
                new TutorialStep("Attack", "Aim with cursor and click to attack enemies.",
                        moveClip),
                new TutorialStep("Pick up item", "Walk on an item to pick it up",
                        moveClip)
        );

        // Create UI entity and register
        Entity ui = new Entity();
        ui.addComponent(new TutorialScreenDisplay(game, steps));
        ui.addComponent(new InputDecorator(stage, 10));
        return ui;
    }
}