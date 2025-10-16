package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.TutorialClip;
import com.csse3200.game.components.screens.TutorialScreenDisplay;
import com.csse3200.game.components.screens.TutorialStep;
import com.csse3200.game.entities.Entity;
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
    public static final String FRAME_PATTERN = "frame_%04d.png";
    public static final float  DEFAULT_FPS    = 12f;


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
        TutorialClip movementClip = new TutorialClip("images/tutorial/movement", FRAME_PATTERN, 60, DEFAULT_FPS, true);
        TutorialClip pauseClip = new TutorialClip("images/tutorial/pause", FRAME_PATTERN, 43, DEFAULT_FPS, true);
        TutorialClip pickupClip = new TutorialClip("images/tutorial/pickup", FRAME_PATTERN, 46, DEFAULT_FPS, true);
        TutorialClip inventoryClip = new TutorialClip("images/tutorial/inventory", FRAME_PATTERN, 33, DEFAULT_FPS, true);
        TutorialClip miniMapClip = new TutorialClip("images/tutorial/mini_map", FRAME_PATTERN, 93, DEFAULT_FPS, true);
        TutorialClip teleportClip = new TutorialClip("images/tutorial/teleport", FRAME_PATTERN, 61, DEFAULT_FPS, true);
        List<TutorialStep> steps = List.of(
                new TutorialStep("Basic Movement", "Use 'A/S/D' to move around, and 'Space' to jump",
                        movementClip),
                new TutorialStep("Game Pause", "Press 'ESC' to pause the game",
                        pauseClip),
                new TutorialStep("Item Pickup", "Press 'E' to pick up an item",
                        pickupClip),
                new TutorialStep("Inventory", "Press 'I' to toggle inventory bar",
                        inventoryClip),
                new TutorialStep("Mini Map", "Press 'TAB' to open mini map, 'Ctrl +/-' to resize",
                        miniMapClip),
                new TutorialStep("Teleporter", "Press 'T' to open teleporter menu",
                        teleportClip)
        );

        // Create UI entity and register
        Entity ui = new Entity();
        ui.addComponent(new TutorialScreenDisplay(game, steps));
        ui.addComponent(new InputDecorator(stage, 10));
        return ui;
    }
}