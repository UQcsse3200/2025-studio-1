package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.GdxGame;

/**
 * End-of-run UI shown when the player is defeated.
 * <p>
 * This subclass configures {@link BaseEndScreenDisplay} with:
 * <ul>
 *   <li>Title: <b>"DEFEATED"</b> in red</li>
 *   <li>Primary button: <b>"Try Again"</b> → restarts the game (switches to {@link GdxGame.ScreenType#MAIN_GAME})</li>
 *   <li>Secondary button: <b>"Main Menu"</b> → handled by the base class (returns to main menu)</li>
 * </ul>
 */
public class DeathScreenDisplay extends BaseEndScreenDisplay {

    /**
     * Creates a defeat end-screen display.
     *
     * @param game the {@link GdxGame} instance used for navigation
     */
    public DeathScreenDisplay(GdxGame game) {
        super(
                game,
                "DEFEATED",
                new Color(1f, 0f, 0f, 1f),
                "Try Again",
                () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME),
                null // use default secondary action (back to Main Menu)
        );
    }
}
