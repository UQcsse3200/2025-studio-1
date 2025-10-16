package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;

/**
 * Pause menu overlay shown above the main game and HUD.
 * <p>
 * Renders a full-screen dimmer and a centered panel with actions:
 * <ul>
 *   <li><b>Resume</b> — triggers the entity's {@code "resume"} event</li>
 *   <li><b>Restart</b> — switches to {@link GdxGame.ScreenType#MAIN_GAME}</li>
 *   <li><b>Main Menu</b> — returns to {@link GdxGame.ScreenType#MAIN_MENU}</li>
 * </ul>
 * The dimmer is non-interactive (input disabled) so button clicks reach the menu.
 * ESC is handled once to immediately resume gameplay.
 */
public class PauseMenuDisplay extends BaseScreenDisplay {
    /**
     * Full-screen dimmer image. Kept as a field so we can avoid adding duplicates
     * when the pause overlay is opened multiple times and to remove it in {@link #dispose()}.
     */
    private Image dimmer;

    /**
     * Creates a new pause menu overlay bound to the given game instance.
     *
     * @param game game instance used for screen navigation actions
     */
    public PauseMenuDisplay(GdxGame game) {
        super(game);
    }

    /**
     * Builds the pause UI:
     * <ol>
     *   <li>Add a full-screen, non-interactive dimmer (placed on the Stage)</li>
     *   <li>Bring {@code root} to the front so the panel sits above the dimmer/HUD</li>
     *   <li>Add title and action buttons</li>
     *   <li>Capture the first ESC key press to resume and remove the listener</li>
     * </ol>
     *
     * @param root the fill-parent root table created by {@link BaseScreenDisplay}
     */
    @Override
    protected void buildUI(Table root) {
        // Dimmer via skin (no manual texture management)
        if (dimmer == null) {
            dimmer = new Image(skin.newDrawable("white", new Color(0, 0, 0, 0.6f)));
            dimmer.setFillParent(true);
            dimmer.setTouchable(Touchable.disabled); // let events reach the menu
        }
        // Ensure only one instance on stage
        if (dimmer.getStage() == null) stage.addActor(dimmer);

        // Ensure the menu sits above the dimmer (and HUD)
        root.toFront();

        // Title
        addTitle(root, "Game Paused", 2.0f, Color.WHITE, 24f);

        // Buttons
        Table panel = new Table();
        panel.defaults().pad(10f);
        panel.add(button("Resume", 1.8f, () -> {
            ServiceLocator.getButtonSoundService().playClick();
            entity.getEvents().trigger("resume");
        })).row();

        panel.add(button("Restart", 2f, () -> {
            ServiceLocator.getButtonSoundService().playClick();
            game.setScreen(GdxGame.ScreenType.MAIN_GAME);
        })).row();

        panel.add(button("Main Menu", 2f, () -> {
            ServiceLocator.getButtonSoundService().playClick();
            backMainMenu();
        })).row();

        panel.add(button("Save", 1.8f, () -> {
            ServiceLocator.getButtonSoundService().playClick();
            entity.getEvents().trigger("save");
            backMainMenu();
        })).row();

        root.add(panel).center().expandX().row();

        // Keyboard focus + one-shot ESC to resume
        stage.setKeyboardFocus(root);
        root.setTouchable(Touchable.enabled);

    }

    /**
     * High z-index to ensure the pause overlay renders above other UI layers.
     *
     * @return draw order value for this UI component
     */
    @Override
    public float getZIndex() {
        return 100f;
    }

    /**
     * Removes the dimmer from the Stage and delegates to base disposal.
     * Ensures no stale dimmer instances remain if the overlay is recreated later.
     */
    @Override
    public void dispose() {
        if (dimmer != null) {
            dimmer.remove();
            dimmer = null;
        }
        super.dispose();
    }
}
