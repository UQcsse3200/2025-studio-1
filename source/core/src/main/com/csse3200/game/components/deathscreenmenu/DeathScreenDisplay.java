package com.csse3200.game.components.deathscreenmenu;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathScreenDisplay extends UIComponent  {
    private static final Logger logger = LoggerFactory.getLogger(DeathScreenDisplay.class);
    private final GdxGame game;
    private static final float Z_INDEX = 2f;
    private Table table;
    private NeonStyles neon;


    public DeathScreenDisplay(GdxGame game) {
        super();
        this.game = game;
    }

    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.70f);
        addActors();
    }

    private void addActors() {
        table = new Table();
        table.setFillParent(true);


        table.center();
        float btnW = stage.getWidth() * 0.34f;
        float btnH = Math.max(64f, stage.getHeight() * 0.08f);
        table.defaults().width(btnW).height(btnH);

        Label defeatedLabel = new Label("DEFEATED", skin, "title");
        defeatedLabel.setFontScale(3.0f);
        defeatedLabel.setColor(1f, 0f, 0f, 1f);
        table.add(defeatedLabel).colspan(2).center().padBottom(50f);
        table.row();

        Label.LabelStyle smallStyle = skin.get("small", Label.LabelStyle.class);
        smallStyle.fontColor = skin.getColor("white");

        Label roundLabel = new Label("Round: 1", skin, "small");
        roundLabel.setFontScale(3.0f);
        table.add(roundLabel).colspan(2).center().padBottom(50f);
        table.row();

        Label timeLabel = new Label("Time: 00:00", skin, "small");
        timeLabel.setFontScale(3.0f);
        table.add(timeLabel).colspan(2).center().padBottom(50f);
        table.row();

        TextButton.TextButtonStyle style = neon.buttonRounded();
        TextButton tryAgainBtn = new TextButton("Try Again", style);
        TextButton mainMenuBtn = new TextButton("Main Menu", style);


        tryAgainBtn.getLabel().setFontScale(2.0f);
        mainMenuBtn.getLabel().setFontScale(2.0f);


        // Triggers an event when the button is pressed
        tryAgainBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Try Again Button clicked");
                        restartGame();

                    }
                });

        mainMenuBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Main Menu button clicked");
                        backMainMenu();
                    }
                });


        table.row();
        table.add(tryAgainBtn).left().padRight(30f);
        table.add(mainMenuBtn).left();

        stage.addActor(table);
    }

    private void backMainMenu() {
        game.setScreen(ScreenType.MAIN_MENU);
    }
    private void restartGame() {
        game.setScreen(ScreenType.MAIN_GAME);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }

    @Override
    public void dispose() {
        table.clear();
        super.dispose();
    }
}
