package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.records.RoundData;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.session.LeaderBoardManager;
import com.csse3200.game.utils.TimefromSeconds;

import java.util.List;

/**
 * the leaderboard screen display showing the leaderboard information and the related data .
 * This class extends BaseScreenDisplay and provides the specific UI for the leaderboard.
 */
public class LeaderboardScreenDisplay extends BaseScreenDisplay {
    /**
     * Creates a new Leaderboard screen display.
     *
     * @param game which is used for screeen navigation actions
     */
    public LeaderboardScreenDisplay(GdxGame game) {
        super(game);
    }

    /**
     * Builds the leaderboard UI which includes the title, current highscore, leaderboard data rows, and navigation buttons.
     *
     * @param root it is the root table to which the Ui elements are added.
     */
    @Override
    protected void buildUI(Table root) {
        addTitle(root, "Leaderboard", 2.5f, Color.GOLD, 30f);

        //it sets the fonts and styles used by different labels in the leaderboard UI.
        BitmapFont font = new BitmapFont();
        font.getData().setScale(2f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle rowStyle = new Label.LabelStyle(font, Color.LIGHT_GRAY);
        Label.LabelStyle leaderboardStyle = new Label.LabelStyle(font, Color.CYAN);

        //it is the main panel that contains all the leaderboard UI elements.
        Table panel = new Table();
        panel.setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.5f)));
        panel.defaults().pad(20f);

        //it is the panel which displays the current highest score at the top of the leaderboard.
        int highestScore = getHighestScore();
        Table highScoreBox = new Table();
        highScoreBox.setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.8f)));
        highScoreBox.defaults().pad(15f);

        Label highScoreLabel = new Label("Current Highest Score: " + highestScore, style);
        highScoreLabel.setAlignment(Align.center);

        highScoreBox.add(highScoreLabel).expandX().fillX();
        panel.add(highScoreBox).growX().padTop(15f).padBottom(15f).row();

        Table headerTable = new Table();
        headerTable.defaults().pad(10f).expandX();

        headerTable.add(new Label("Round", leaderboardStyle)).expandX().center();
        headerTable.add(new Label("Currency", leaderboardStyle)).expandX().center();
        headerTable.add(new Label("Time", leaderboardStyle)).expandX().center();
        headerTable.add(new Label("Score", leaderboardStyle)).expandX().center();
        panel.add(headerTable).growX().row();

        LeaderBoardManager lbm = ServiceLocator.getLeaderBoardManager();
        List<RoundData> rounds = (lbm != null) ? lbm.getLeaderBoard() : List.of();

        if (rounds.isEmpty()) {
            panel.add(new Label("No rounds recorded yet.", new Label.LabelStyle(font, Color.LIGHT_GRAY)))
                    .center().row();
        } else {
            for (int i = 0; i < rounds.size(); i++) {
                RoundData rd = rounds.get(i);
                String mmss = TimefromSeconds.toMMSS((int) rd.getTime());

                Table row = new Table();
                row.defaults().pad(5f).expandX();

                row.add(new Label(String.valueOf(i + 1), rowStyle)).center();
                row.add(new Label(String.valueOf(rd.getCurrency()), rowStyle)).center();
                row.add(new Label(mmss, rowStyle)).center();
                row.add(new Label(String.valueOf(rd.getScore()), rowStyle)).center();

                panel.add(row).growX().row();
            }
        }

        panel.add(new Label("-----------------------------------------", style)).center().row();

        Table buttons = new Table();
        buttons.defaults().pad(10f);

        buttons.add(button("Main Menu", 1.5f, () -> game.setScreen(GdxGame.ScreenType.MAIN_MENU)));
        buttons.add(button("Try Again", 1.5f, this::onStart));
        buttons.add(button("Exit Game", 1.5f, this::onExit));

        panel.add(buttons).center().row();

        root.add(panel).center().expand().pad(100f);
    }


    /**
     * Gets the highest score from the leaderboard.
     *
     * @return the highest score, or 0 if the leaderboard is empty or not available.
     */
    private int getHighestScore() {
        LeaderBoardManager lbm = ServiceLocator.getLeaderBoardManager();
        if (lbm == null || lbm.getLeaderBoard().isEmpty()) {
            return 0;
        }
        return lbm.getLeaderBoard().stream().mapToInt(RoundData::getScore).max().orElse(0);
    }

    /**
     * Restarts the game by navigating to the main game screen.
     */
    private void onStart() {
        logger.info("Restarting game directly from leaderboard");
        ServiceLocator.getButtonSoundService().playClick();
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);
    }

    /**
     * Exits the game application.
     */
    private void onExit() {
        ServiceLocator.getButtonSoundService().playClick();
        logger.info("Exit game");
        game.exit();
    }
}