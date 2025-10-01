package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.services.CountdownTimerService;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a button to exit the Main Game screen to the Main Menu screen.
 */
public class MainGameDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainGameDisplay.class);
    private static final float Z_INDEX = 2f;
    private Table table;

    private Label timerLabel;
    private final CountdownTimerService timerService;

    public MainGameDisplay(CountdownTimerService timerService) {
        this.timerService = timerService;
    }

    @Override
    public void create() {
        super.create();
        addActors();
    }

    private void addActors() {
        table = new Table();
        table.top();
        table.setFillParent(true);

        TextButton mainMenuBtn = new TextButton("Exit", skin);

        timerLabel = new Label("", skin, "large");
        table.add().expandX();
        table.add(timerLabel).center().padTop(10f);
        table.add().expandX();


        // Triggers an event when the button is pressed.
        mainMenuBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Exit button clicked");
                        entity.getEvents().trigger("exit");
                    }
                });

        table.add(mainMenuBtn).right().padTop(10f).padRight(10f);

        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
        long remainingMs = timerService.getRemainingMs();
        int min = (int) (remainingMs / 60000);
        int sec = (int) ((remainingMs / 1000) % 60);
        String timeText = String.format("%02d:%02d", min, sec);
        timerLabel.setText(timeText);
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
