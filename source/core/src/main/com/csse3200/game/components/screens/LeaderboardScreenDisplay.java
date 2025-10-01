package com.csse3200.game.components.screens;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;



public class LeaderboardScreenDisplay extends BaseScreenDisplay {

    public LeaderboardScreenDisplay(GdxGame game) {
        super(game);
    }

    @Override
    protected void buildUI(Table root) {
        addTitle(root, "Leaderboard", 2.5f, Color.GOLD, 30f);

        BitmapFont font = new BitmapFont();
        font.getData().setScale(2f);
        Label.LabelStyle leaderboardStyle = new Label.LabelStyle(font, Color.CYAN);

        Table panel = new Table();
        panel.setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.5f)));
        panel.defaults().pad(20f);

        // sample leaderboard data
        panel.add(new Label("1. Player A - 1200", leaderboardStyle)).row();
        panel.add(new Label("2. Player B - 1000", leaderboardStyle)).row();
        panel.add(new Label("3. Player C - 800", leaderboardStyle)).row();

        panel.add(button("Back", 1.5f, () -> game.setScreen(GdxGame.ScreenType.DEATH_SCREEN))).row();
        panel.add(button("Exit", 1.5f, () -> game.setScreen(GdxGame.ScreenType.MAIN_MENU))).row();

        root.add(panel).center().expand().pad(100f);
    }
}