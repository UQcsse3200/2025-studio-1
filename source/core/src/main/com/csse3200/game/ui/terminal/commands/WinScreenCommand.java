package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;

import java.util.ArrayList;

/**
 * A command for switching to the Win Screen.
 */
public class WinScreenCommand implements Command {
    private final GdxGame game;

    /**
     * Create a new WinGameCommand
     *
     * @param game the current {@link GdxGame} instance
     */
    public WinScreenCommand(GdxGame game) {
        this.game = game;
    }

    @Override
    public boolean action(ArrayList<String> args) {
        game.setScreen(GdxGame.ScreenType.WIN_SCREEN);
        return true;
    }
}
