package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.GdxGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI component for displaying the Death Screen.
 * <p>
 * This screen is shown when the player is defeated.
 * It displays a "DEFEATED" title, the round number,
 * elapsed time, and provides options to:
 * <ul>
 *   <li>Try Again — restart the game</li>
 *   <li>Main Menu — return to the main menu</li>
 * </ul>
 */
public class DeathScreenDisplay extends BaseEndScreenDisplay {
    /** Logger instance for the Death Screen. */
    private static final Logger logger = LoggerFactory.getLogger(DeathScreenDisplay.class);

    /**
     * Creates a new DeathScreenDisplay.
     *
     * @param game the {@link GdxGame} instance used for navigation
     */
    public DeathScreenDisplay(GdxGame game) {
        super(game);
    }

    /**
     * {@inheritDoc}
     * @return "DEFEATED"
     */
    @Override
    protected String titleText() { return "DEFEATED"; }

    /**
     * {@inheritDoc}
     * @return red color for the defeated title
     */
    @Override
    protected Color titleColor() { return new Color(1f, 0f, 0f, 1f); }

    /**
     * {@inheritDoc}
     * @return "Try Again"
     */
    @Override
    protected String primaryButtonText() { return "Try Again"; }

    /**
     * {@inheritDoc}
     * Restarts the game by switching to the MAIN_GAME screen.
     */
    @Override
    protected void onPrimaryButton() {
        logger.info("Restarting game from DeathScreenDisplay");
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);
    }
}
