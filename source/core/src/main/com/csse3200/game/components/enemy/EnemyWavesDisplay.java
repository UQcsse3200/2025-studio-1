package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays enemy wave information on the HUD, including current wave number,
 * maximum waves, and inter-wave delay.
 */
public class EnemyWavesDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(EnemyWavesDisplay.class);
    private final EnemyWaves wavesManager;
    private Table table;
    private Label waveNumberLabel;
    private Label waveDelayLabel;
    protected Stage stage;
    private static final float screenHeight = (float) Gdx.graphics.getHeight();

    /**
     * Constructs the EnemyWavesDisplay for the given EnemyWaves manager.
     * Registers event listeners to update the display and handle visibility.
     *
     * @param wavesManager the EnemyWaves instance managing wave logic
     */
    public EnemyWavesDisplay(EnemyWaves wavesManager) {
        this.wavesManager = wavesManager;
        stage = ServiceLocator.getRenderService().getStage();
        addActors();

        wavesManager.getEvents().addListener("updateWaveNumber", this::updateWaveNumber);
        wavesManager.getEvents().addListener("spawnWave", this::setTableVisible);
        wavesManager.getEvents().addListener("allWavesFinished", this::setTableInvisible);
    }

    /**
     * Initialises and adds the labels and table to the stage.
     * Sets up layout, background, and initial visibility.
     */
    private void addActors() {
        table = new Table();
        table.setSize(300f, 150f);
        table.setPosition(30f, screenHeight * 3/5);

        // Current wave number
        int waveNumber = wavesManager.getWaveNumber();
        CharSequence waveNumberText = String.format("Waves spawned: %d", waveNumber);
        waveNumberLabel = new Label(waveNumberText, skin, "large");

        // Max waves
        int maxWaves = wavesManager.getMaxWaves();
        CharSequence maxWavesText = String.format("Max waves: %d", maxWaves);
        Label maxWavesLabel = new Label(maxWavesText, skin, "large");

        // Wave delay
        int waveDelay = wavesManager.getWaveDelayInSeconds();
        CharSequence waveDelayText = String.format("Wave delay: " + waveDelay + "s");
        waveDelayLabel = new Label(waveDelayText, skin, "large");

        // Layout:
        // Row 1: Waves spawned/current wave
        table.add(waveNumberLabel).left().padLeft(10f);
        table.row();
        // Row 2: Max waves
        table.add(maxWavesLabel).left().padLeft(10f);
        table.row();
        // Row 3: Wave delay in seconds
        table.add(waveDelayLabel).left().padLeft(10f);
        table.row();

        Drawable bg = skin.newDrawable("white", new Color(255f, 255f, 255f, 0.6f));
        table.setBackground(bg);
        table.setVisible(false);  // hide it first, only visible when the wave is spawned

        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    /**
     * Updates the wave number label to reflect the current number of waves spawned.
     * Triggered by the "updateWaveNumber" event from the waves manager.
     */
    public void updateWaveNumber() {
        int waveNumber = wavesManager.getWaveNumber();
        CharSequence waveNumberText = String.format("Waves spawned: %d", waveNumber);
        waveNumberLabel.setText(waveNumberText);
    }

    /**
     * Makes the wave info table visible. Triggered when a new set of waves is spawned.
     */
    public void setTableVisible() {
        table.setVisible(true);
    }

    /**
     * Hides the wave info table. Triggered when all waves have finished.
     */
    public void setTableInvisible() {
        table.setVisible(false);
    }
}
