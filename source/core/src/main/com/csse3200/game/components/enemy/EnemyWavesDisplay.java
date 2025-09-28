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

public class EnemyWavesDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(EnemyWavesDisplay.class);
    private final EnemyWaves wavesManager;
    private Table table;
    private Label waveNumberLabel;
    private Label maxWavesLabel;
    private Label timeRemaining;
    protected Stage stage;
    private static final float screenHeight = (float) Gdx.graphics.getHeight();

    public EnemyWavesDisplay(EnemyWaves wavesManager) {
        this.wavesManager = wavesManager;
        stage = ServiceLocator.getRenderService().getStage();
        addActors();
    }

    @Override
    public void create() {
        super.create();
        stage = ServiceLocator.getRenderService().getStage();
        addActors();
    }

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
        maxWavesLabel = new Label(maxWavesText, skin, "large");

        // Layout:
        // Row 1: Waves spawned/current wave
        table.add(waveNumberLabel).left().padLeft(10f);
        table.row();
        // Row 2: Max waves
        table.add(maxWavesLabel).left().padLeft(10f);
        table.row();

        Drawable bg = skin.newDrawable("white", new Color(255f, 255f, 255f, 0.6f));
        table.setBackground(bg);

        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }
}
