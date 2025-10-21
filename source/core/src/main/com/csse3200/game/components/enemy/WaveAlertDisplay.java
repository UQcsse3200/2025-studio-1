package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Displays a flashing system alert banner when an enemy wave is detected.
 */
public class WaveAlertDisplay extends UIComponent {
    private static final String ALERT_TEXT_1 = "> SYSTEM ALERT:";
    private static final String ALERT_TEXT_2 = "ENEMY WAVE DETECTED..";

    private Table table;
    private Label alertLabel1;
    private Label alertLabel2;
    private final Stage stage;

    public WaveAlertDisplay() {
        stage = ServiceLocator.getRenderService().getStage();
        addActors();
    }

    /**
     * Initialises and adds the labels and table to the stage.
     * Sets up layout, background, and initial visibility.
     */
    private void addActors() {
        table = new Table();
        table.setFillParent(true); // center on screen
        table.center();
        table.padBottom(Gdx.graphics.getHeight() * 0.3f);

        // Create the alert label
        try {
            alertLabel1 = new Label(ALERT_TEXT_1, skin, "large");
            alertLabel2 = new Label(ALERT_TEXT_2, skin, "large");
        } catch (Exception e) {
            alertLabel1 = new Label(ALERT_TEXT_1, skin);
            alertLabel2 = new Label(ALERT_TEXT_2, skin);
        }
        alertLabel1.setColor(Color.SKY);
        alertLabel2.setColor(Color.SKY);
        table.setVisible(false);

        float scale = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) / 250f;
        alertLabel1.setFontScale(scale);
        alertLabel2.setFontScale(scale);

        table.add(alertLabel1).center();
        table.row();
        table.add(alertLabel2).center();
        table.row();

        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw handled by stage
    }

    /**
     * Displays the wave alert on screen with a flashing color effect.
     * Makes the table visible and starts their color animation.
     */
    public void display() {
       table.setVisible(true);
        alertLabel1.clearActions();
        alertLabel2.clearActions();

        // Flash between cyan and white
        alertLabel1.addAction(Actions.forever(
                Actions.sequence(
                        Actions.color(Color.SKY, 0.25f, Interpolation.swing),
                        Actions.color(Color.WHITE, 0.25f, Interpolation.swing)
                )
        ));
        alertLabel2.addAction(Actions.forever(
                Actions.sequence(
                        Actions.color(Color.SKY, 0.25f, Interpolation.sine),
                        Actions.color(Color.WHITE, 0.25f, Interpolation.sine)
                )
        ));
    }

    /**
     * Hides the wave alert and stops all ongoing animations.
     * Also removes the alert table from the stage to clean up resources.
     */
    public void dispose() {
        alertLabel1.setVisible(false);
        alertLabel2.setVisible(false);
        alertLabel1.clearActions();
        alertLabel2.clearActions();
        table.remove();
    }

    /**
     * @return the first alert label used in the flashing wave alert display
     */
    public Label getAlertLabel1() {
        return alertLabel1;
    }

    /**
     * @return the second alert label used in the flashing wave alert display
     */
    public Label getAlertLabel2() {
        return alertLabel2;
    }

    /**
     * @return the root {@link Table} used for layout and display.
     */
    public Table getTable() {
        return table;
    }
}