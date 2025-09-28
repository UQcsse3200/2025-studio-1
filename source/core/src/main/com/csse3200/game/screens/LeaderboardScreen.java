package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.components.screens.LeaderboardScreenDisplay;

/**
 * Screen shown to display the leaderboard.
 * This class extends BaseScreen and provides the
 * specific UI entity for the leaderboard .
 */
public class LeaderboardScreen extends BaseScreen {

    public LeaderboardScreen(GdxGame game) {
        super(game, "images/menu_background.png");
    }

    @Override
    protected Entity createUIScreen(Stage stage) {
        return new Entity()
                .addComponent(new LeaderboardScreenDisplay(game))
                .addComponent(new InputDecorator(stage, 10));
    }
}
