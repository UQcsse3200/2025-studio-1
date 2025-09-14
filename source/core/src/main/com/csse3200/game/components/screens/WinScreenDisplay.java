package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.GdxGame;

/**
 * End-of-run UI shown when the player wins.
 * <p>
 * This subclass configures {@link BaseEndScreenDisplay} with:
 * <ul>
 *   <li>Title: <b>"Victory"</b> in green</li>
 *   <li>Primary button: <b>"Continue"</b> → switches to {@link GdxGame.ScreenType#MAIN_GAME}</li>
 *   <li>Secondary button: <b>"Main Menu"</b> → handled by the base class (back to main menu)</li>
 * </ul>
 */
public class WinScreenDisplay extends BaseEndScreenDisplay {
    /**
     * Creates a victory end-screen display.
     *
     * @param game the {@link GdxGame} instance used for navigation
     */
    public WinScreenDisplay(GdxGame game) {
        super(
                game,
                "Victory",
                new Color(0f, 1f, 0f, 1f),
                "Continue",
                () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME),
                null // use default secondary action (back to Main Menu)
        );
    }
}