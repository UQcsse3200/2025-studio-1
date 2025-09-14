package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.GdxGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI component for displaying the Win Screen.
 * <p>
 * This screen is shown when the player achieves victory.
 * It displays a "Victory" title, the round number,
 * elapsed time, and provides options to:
 * <ul>
 *   <li>Continue — proceed to the next stage/game</li>
 *   <li>Main Menu — return to the main menu</li>
 * </ul>
 */
public class WinScreenDisplay extends BaseEndScreenDisplay {
    /** Logger instance for the Win Screen. */
    private static final Logger logger = LoggerFactory.getLogger(WinScreenDisplay.class);

    /**
     * Creates a new WinScreenDisplay.
     *
     * @param game the {@link GdxGame} instance used for navigation
     */
    public WinScreenDisplay(GdxGame game) {
        super(game);
    }

    /**
     * {@inheritDoc}
     * @return "Victory"
     */
    @Override
    protected String titleText() { return "Victory"; }

    /**
     * {@inheritDoc}
     * @return green color for the victory title
     */
    @Override
    protected Color titleColor() { return new Color(0f, 1f, 0f, 1f); }

    /**
     * {@inheritDoc}
     * @return "Continue"
     */
    @Override
    protected String primaryButtonText() { return "Continue"; }

    /**
     * {@inheritDoc}
     * Continues the game by switching to the MAIN_GAME screen.
     */
    @Override
    protected void onPrimaryButton() {
        logger.info("Continuing game from WinScreenDisplay");
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);
    }
}
