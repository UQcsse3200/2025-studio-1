package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for switching to the Win Screen.
 */
public class EndScreenCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(EndScreenCommand.class);

    private final GdxGame game;
    private final GdxGame.ScreenType screenType;

    /**
     * Create a new EndScreenCommand
     *
     * @param game       the current {@link GdxGame} instance
     * @param screenType the target screen type (e.g. WIN_SCREEN, LOSE_SCREEN)
     */
    public EndScreenCommand(GdxGame game, GdxGame.ScreenType screenType) {
        this.game = game;
        this.screenType = screenType;
    }

    @Override
    public boolean action(ArrayList<String> args) {
        logger.info("Switching to {}", screenType);
        game.setScreen(screenType);
        return true;
    }
}
