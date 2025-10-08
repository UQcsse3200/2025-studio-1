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
    private static final float screenHeight = (float) Gdx.graphics.getHeight();
    private final EnemyWaves wavesManager;
    protected Stage stage;
    private Table table;
    private Label maxWavesLabel;
    private Label waveNumberLabel;
    private Label waveDelayLabel;

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
        wavesManager.getEvents().addListener("updateMaxWave", this::updateMaxWave);
        wavesManager.getEvents().addListener("spawnWave", this::setTableVisible);
        wavesManager.getEvents().addListener("allWavesFinished", this::setTableInvisible);
    }

    private float safeScreenHeight() {
        try {
            return (Gdx.graphics != null) ? (float) Gdx.graphics.getHeight() : 720f;
        } catch (Exception e) {
            return 720f; // fallback for headless/unit tests
        }
    }

    private Label makeLabel(CharSequence text) {
        // Some tests may not have the "large" style loaded; fall back gracefully
        try {
            return new Label(text, skin, "large");
        } catch (Exception e) {
            logger.debug("Falling back to default label style for text '{}': {}", text, e.getMessage());
            return new Label(text, skin);
        }
    }

    /**
     * Initialises and adds the labels and table to the stage.
     * Sets up layout, background, and initial visibility.
     */
    private void addActors() {
        table = new Table();
        table.setSize(300f, 150f);
        table.setPosition(30f, safeScreenHeight() * 3 / 5f);

        // Current wave number
        int waveNumber = wavesManager.getWaveNumber();
        CharSequence waveNumberText = String.format("Waves spawned: %d", waveNumber);
        waveNumberLabel = makeLabel(waveNumberText);

        // Max waves
        int maxWaves = wavesManager.getMaxWaves();
        CharSequence maxWavesText = String.format("Max waves: %d", maxWaves);
        maxWavesLabel = makeLabel(maxWavesText);

        // Wave delay
        int waveDelay = wavesManager.getWaveDelayInSeconds();
        CharSequence waveDelayText = String.format("Wave delay: %ds", waveDelay);
        waveDelayLabel = makeLabel(waveDelayText);

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

        Drawable bg;
        try {
            bg = skin.newDrawable("white", new Color(1f, 1f, 1f, 0.6f)); // use 0-1f color range
        } catch (Exception e) {
            bg = null; // In tests skin may not have drawable; not critical
        }
        if (bg != null) {
            table.setBackground(bg);
        }
        table.setVisible(false);  // hide it first, only visible when a wave spawns

        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw handled by stage
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
     * Updates max wave label to reflect the current number of waves spawned.
     * Triggered by the "updateMaxWave" event from the waves manager.
     */
    public void updateMaxWave() {
        int maxWaves = wavesManager.getMaxWaves();
        CharSequence maxWavesText = String.format("Max waves: %d", maxWaves);
        maxWavesLabel.setText(maxWavesText);
    }

    /**
     * Makes the wave info table visible.
     */
    public void setTableVisible() {
        table.setVisible(true);
    }

    /**
     * Hides the wave info table.
     */
    public void setTableInvisible() {
        table.setVisible(false);
    }

    public Table getTable() {
        return table;
    }

    public Label getWaveNumberLabel() {
        return waveNumberLabel;
    }

    public Label getMaxWavesLabel() {
        return maxWavesLabel;
    }

    public Label getWaveDelayLabel() {
        return waveDelayLabel;
    }
}
