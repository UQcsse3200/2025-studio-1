package com.csse3200.game.components.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EnemyHealthDisplay extends Component {
    private static final Logger logger = LoggerFactory.getLogger(EnemyHealthDisplay.class);
    private int maxHealth;
    private ProgressBar healthBar;
    // UI constants
    private static final float BAR_WIDTH = 100f;
    private static final float BAR_HEIGHT = 15f;
    // Colours
    private static final Color COLOR_BG  = Color.DARK_GRAY;
    private static final Color COLOR_HEALTH = Color.RED;
    protected Stage stage;

    @Override
    public void create() {
        super.create();
        maxHealth = entity.getComponent(CombatStatsComponent.class).getHealth();
        // Health bar
        ProgressBar.ProgressBarStyle healthBarStyle = makeBarStyle(COLOR_HEALTH, BAR_HEIGHT);
        healthBar = new ProgressBar(0, maxHealth, 1, false, healthBarStyle);
        healthBar.setWidth(BAR_WIDTH);
        healthBar.setValue(maxHealth);
        healthBar.setAnimateDuration(0f);
        Vector2 pos = entity.getPosition();
        Vector3 worldPos3 = new Vector3(pos.x, pos.y, 0);


        stage = ServiceLocator.getRenderService().getStage();
        stage.addActor(healthBar);

        Vector3 screenPos = stage.getCamera().project(worldPos3);
        float offsetY = entity.getScale().y * 6f + 5f; // above head
        healthBar.setPosition(screenPos.x * 130f, screenPos.y * 80f);
        entity.getEvents().addListener("updateHealth", this::updateEnemyHealthUI);

    }

    /**
     * Updates the player's health on the UI.
     * @param health player health
     */
    public void updateEnemyHealthUI(int health) {
        healthBar.setValue(health);
    }

    /**
     * Helper to create a colored drawable from a 1x1 pixel texture
     */
    private Drawable makeColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    /** Creates a simple horizontal bar style with a colored fill and dark background. */
    private ProgressBar.ProgressBarStyle makeBarStyle(Color fill, float barHeight) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = makeColorDrawable(COLOR_BG);
        style.background.setMinHeight(barHeight);
        style.knobBefore = makeColorDrawable(fill);
        style.knobBefore.setMinHeight(barHeight);
        style.knob = null;
        return style;
    }

    @Override
    public void update() {
        if (healthBar == null || entity == null) return;


        Vector2 pos = entity.getPosition();
        OrthographicCamera gameCamera = ServiceLocator.getRenderService().getCamera();
        Vector3 worldPos3 = new Vector3(pos.x, pos.y, 0);
        Vector3 screenPos = gameCamera.project(worldPos3);

        healthBar.setPosition(screenPos.x, screenPos.y);
        // Debug print
        logger.info("Entity pos (world): {},{} | HealthBar pos (screen): {},{}",
                pos.x, pos.y,
                healthBar.getX(), healthBar.getY());
    }

    public void dispose() {
        super.dispose();
        if (healthBar != null) healthBar.remove();
    }
}