package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
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
    int maxHealth;
    int currentHealth;
    ProgressBar healthBar;
    // UI constants
    private static final float BAR_WIDTH = 70f;
    private static final float BAR_HEIGHT = 5f;
    // Colours
    private static final Color COLOR_BG = Color.DARK_GRAY;
    private static final Color COLOR_HEALTH = Color.RED;
    protected Stage stage;
    // Vertical offset for positioning the health bar slightly above the enemy sprite
    private float offsetY = 0.6f;
    // Screen resolution info
    // These values adapt drawing to different screen resolutions, while keeping
    // the game world consistent with a base resolution of 1920x1080 (16:9).
    private static final float screenWidth = (float) Gdx.graphics.getWidth();
    private static final float height = (float) Gdx.graphics.getHeight();
    // Scale factor based on width, using 1920px as the base reference
    float scale = screenWidth / 1920f;
    // The height of the background when scaled to fit the current screen width
    float scaledHeight = 1080f * scale;
    // Vertical offset needed when the screen is not 16:9 (letterboxing case).
    // This centers the background vertically by calculating unused space at the top/bottom.
    float verticalScreenOffset = (height - scaledHeight) / 2;
    // Conversion factors from world coordinates to stage coordinates.
    // These constants were empirically derived to map world positions correctly to the UI stage.
    private static final float WORLD_TO_STAGE_X = screenWidth / 14.82837630565971f;
    private static final float WORLD_TO_STAGE_Y = screenWidth / 1.76f / 8.5f;

    public EnemyHealthDisplay() {
    }

    public EnemyHealthDisplay(float offsetY) {
        this.offsetY = offsetY;
    }

    @Override
    public void create() {
        super.create();
        maxHealth = entity.getComponent(CombatStatsComponent.class).getMaxHealth();
        // Health bar
        ProgressBar.ProgressBarStyle healthBarStyle = makeBarStyle(COLOR_HEALTH, BAR_HEIGHT);
        healthBar = new ProgressBar(0, maxHealth, 1, false, healthBarStyle);
        healthBar.setWidth(BAR_WIDTH);
        healthBar.setValue(maxHealth);
        healthBar.setAnimateDuration(0f);
        // Update currentHealth for testing purpose
        currentHealth = maxHealth;
        // Set stage and add health bar
        stage = ServiceLocator.getRenderService().getStage();
        stage.addActor(healthBar);
        // Update ProgressBar when health value is changed
        entity.getEvents().addListener("updateHealth", this::updateEnemyHealthUI);
    }

    /**
     * Updates the enemy's health on the UI.
     * Also update private variable currentHealth for testing purpose.
     *
     * @param health enemy health
     */
    public void updateEnemyHealthUI(int health) {
        healthBar.setValue(health);
        currentHealth = health;
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

    /**
     * Creates a simple horizontal bar style with a colored fill and dark background.
     */
    private ProgressBar.ProgressBarStyle makeBarStyle(Color fill, float barHeight) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = makeColorDrawable(COLOR_BG);
        style.background.setMinHeight(barHeight);
        style.knobBefore = makeColorDrawable(fill);
        style.knobBefore.setMinHeight(barHeight);
        style.knob = null;
        return style;
    }

    /**
     * Updates the position of the enemy's health bar every frame.
     * Converts the entity's world coordinates into stage coordinates
     * and repositions the health bar above the enemy.
     * Skips update if the entity or health bar is missing.
     */
    @Override
    public void update() {
        if (healthBar == null || entity == null) {
            logger.debug("HealthBar or Enemy is null");
            return;
        }
        Vector2 pos = entity.getPosition();
        // Convert world position to screen-space position and update the health bar's placement.
        healthBar.setPosition(
                (pos.x + 0.1f) * WORLD_TO_STAGE_X,
                (pos.y - 2.8f + offsetY) * WORLD_TO_STAGE_Y + verticalScreenOffset
        );
    }

    /**
     * Cleans up resources when the entity is destroyed.
     * Removes the health bar from the stage to prevent it from lingering
     * after the enemy is removed.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (healthBar != null) healthBar.remove();
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }
}