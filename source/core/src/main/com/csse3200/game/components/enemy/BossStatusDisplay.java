package com.csse3200.game.components.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Boss health bar
 */
public class BossStatusDisplay extends Component {
    private static final Logger log = LoggerFactory.getLogger(BossStatusDisplay.class);

    private static final float BAR_WIDTH = 800f;
    private static final float BAR_HEIGHT = 40f;
    private static final Color COLOR_BG = Color.DARK_GRAY;
    private static final Color COLOR_HEALTH = Color.ORANGE;

    private final List<Texture> disposableTextures = new ArrayList<>();
    private final String bossName;
    private Table table;
    private ProgressBar healthBar;
    private int maxHealth = 100;

    public BossStatusDisplay(String bossName) {
        this.bossName = bossName;
    }

    @Override
    public void create() {
        super.create();

        try {
            // Get max health from entity
            CombatStatsComponent combatStats = entity.getComponent(CombatStatsComponent.class);
            if (combatStats != null) {
                maxHealth = combatStats.getMaxHealth();
            }
            // Listen to CombatStatsComponent's existing events for dynamic tracking
            entity.getEvents().addListener("updateHealth", this::updateBossHealthUI);
            entity.getEvents().addListener("bossFury", this::setPhase);
            entity.getEvents().addListener("death", this::onBossDeath);

            createHealthBar();
            log.debug("Simple Boss health bar created for: {}", bossName);

        } catch (Exception e) {
            log.error("Failed to create boss health bar: {}", e.getMessage());
        }
    }

    /**
     * Update boss health UI in response to health changes
     */
    public void updateBossHealthUI(int health) {
        if (healthBar != null) {
            healthBar.setValue(health);

            float healthPercentage = (float) health / maxHealth;
            log.debug("[BOSS UI] Health updated: {}/{} ({}%)", health, maxHealth, (int) (healthPercentage * 100));
            // Change color to red when health drops to 50% or below
            if (healthPercentage <= 0.5f) {
                setHealthBarColor(Color.RED);
            } else {
                setHealthBarColor(COLOR_HEALTH);
            }
        }
    }

    /**
     * Handle boss death event
     */
    public void onBossDeath() {
        log.debug("[BOSS UI] Boss {} has been defeated!", bossName);

        if (healthBar != null) {
            healthBar.setValue(0);
        }
    }

    /**
     * Create a simple health bar
     */
    private void createHealthBar() {
        // Get stage safely
        Stage stage = ServiceLocator.getRenderService().getStage();
        if (stage == null) {
            log.error("Stage is null, cannot create health bar");
            return;
        }

        // Create table
        table = new Table();
        table.setSize(BAR_WIDTH, BAR_HEIGHT);
        float screenWidth = stage.getWidth();
        float screenHeight = stage.getHeight();

        // Position at center-top
        float x = (screenWidth - BAR_WIDTH) / 2;        // Center horizontally
        float y = screenHeight - BAR_HEIGHT - 270f;      // Near top with 50px margin
        table.setPosition(x, y);

        // Create simple health bar
        ProgressBar.ProgressBarStyle healthBarStyle = createBarStyle();
        healthBar = new ProgressBar(0, maxHealth, 1, false, healthBarStyle);
        healthBar.setValue(maxHealth); // Full health
        healthBar.setAnimateDuration(0f);

        // Add to table
        table.add(healthBar).width(BAR_WIDTH).height(BAR_HEIGHT).pad(5);

        // Add to stage
        stage.addActor(table);

        log.debug("Health bar added to stage");
    }

    /**
     * Create simple bar style
     */
    private ProgressBar.ProgressBarStyle createBarStyle() {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();

        // Background (gray)
        style.background = makeColorDrawable(COLOR_BG);
        style.background.setMinHeight(BAR_HEIGHT);

        // Health bar (red)
        style.knobBefore = makeColorDrawable(COLOR_HEALTH);
        style.knobBefore.setMinHeight(BAR_HEIGHT);

        style.knob = null;
        return style;
    }

    public void setPhase(String phase) {
        if ("FURY".equalsIgnoreCase(phase)) {
            setHealthBarColor(Color.RED);
        } else {
            setHealthBarColor(COLOR_HEALTH);
        }
    }

    private void setHealthBarColor(Color color) {
        if (healthBar == null) return;
        ProgressBar.ProgressBarStyle st = new ProgressBar.ProgressBarStyle(healthBar.getStyle());
        st.knobBefore = makeColorDrawable(color);
        st.knobBefore.setMinHeight(BAR_HEIGHT);
        healthBar.setStyle(st);
    }

    /**
     * Create colored drawable
     */
    private Drawable makeColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        disposableTextures.add(texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (healthBar != null) healthBar.remove();
        if (table != null) table.remove();

        for (Texture tex : disposableTextures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        disposableTextures.clear();
    }
}