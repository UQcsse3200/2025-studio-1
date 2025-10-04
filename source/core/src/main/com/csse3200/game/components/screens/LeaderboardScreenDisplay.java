package com.csse3200.game.components.screens;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.records.RoundData;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.session.LeaderBoardManager;

import java.util.List;


public class LeaderboardScreenDisplay extends BaseScreenDisplay {

    public LeaderboardScreenDisplay(GdxGame game) {
        super(game);
    }

    @Override
    protected void buildUI(Table root) {
        addTitle(root, "Leaderboard", 2.5f, Color.GOLD, 30f);
        // Style for rows
        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.8f);
        Label.LabelStyle rowStyle = new Label.LabelStyle(font, Color.CYAN);

        Table panel = new Table();
        panel.setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.5f)));
        panel.defaults().pad(20f);

        Label header = new Label("Round   Currency   Time   Score", new Label.LabelStyle(font, Color.WHITE));
        header.setAlignment(Align.center);
        panel.add(header).center().padBottom(12f).row();

        // Fetch data from the current session's leaderboard
        LeaderBoardManager lbm = ServiceLocator.getLeaderBoardManager();
        List<RoundData> rounds = (lbm != null) ? lbm.getLeaderBoard() : List.of();

        if (rounds.isEmpty()) {
            panel.add(new Label("No rounds recorded yet.", new Label.LabelStyle(font, Color.LIGHT_GRAY)))
                    .center().row();
        } else {
            for (int i = 0; i < rounds.size(); i++) {
                RoundData rd = rounds.get(i);
                String mmss = toMMSS((int) rd.getTime());
                String line = String.format("%3d     %7d     %5s   %5d",
                        i + 1, rd.getCurrency(), mmss, rd.getScore());
                panel.add(new Label(line, rowStyle)).left().row();
            }
        }

        panel.add(button("Back", 1.5f, () -> game.setScreen(GdxGame.ScreenType.DEATH_SCREEN))).row();
        panel.add(button("Exit", 1.5f, () -> game.setScreen(GdxGame.ScreenType.MAIN_MENU))).row();

        root.add(panel).center().expand().pad(100f);
    }

    private static String toMMSS(int totalSeconds) {
        int m = Math.max(0, totalSeconds) / 60;
        int s = Math.max(0, totalSeconds) % 60;
        return String.format("%02d:%02d", m, s);
    }
}