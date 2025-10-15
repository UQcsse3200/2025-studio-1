package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.LeaderboardScreenDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;

/**
 * Screen shown to display the leaderboard.
 * This class extends BaseScreen and provides the
 * specific UI entity for the leaderboard .
 */
public class LeaderboardScreen extends BaseScreen {

    public LeaderboardScreen(GdxGame game) {
        super(game, "images/menu_background.png");
    }

    /**
     * Creates the UI entity for the leaderboard screen.
     *
     * @param stage the stage to which the UI entity is added.
     * @return the created UI entity.
     */
    @Override
    protected Entity createUIScreen(Stage stage) {
        return new Entity()
                .addComponent(new LeaderboardScreenDisplay(game))
                .addComponent(new InputDecorator(stage, 10));
    }
}
