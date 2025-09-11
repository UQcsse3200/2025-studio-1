package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal Boss health bar - just displays a red bar in top-right corner
 */
public class BossStatusDisplay extends Component {

    private static final float BAR_WIDTH = 600f;
    private static final float BAR_HEIGHT = 20f;
    private static final Color COLOR_BG = Color.DARK_GRAY;
    private static final Color COLOR_HEALTH = Color.ORANGE;

    private final List<Texture> disposableTextures = new ArrayList<>();
    private Table table;
    private ProgressBar healthBar;
    private String bossName;
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

            createHealthBar();
            System.out.println("Simple Boss health bar created for: " + bossName);

        } catch (Exception e) {
            System.err.println("Failed to create boss health bar: " + e.getMessage());
        }
    }

    /**
     * Create a simple health bar
     */
    private void createHealthBar() {
        // Get stage safely
        Stage stage = ServiceLocator.getRenderService().getStage();
        if (stage == null) {
            System.err.println("Stage is null, cannot create health bar");
            return;
        }

        // Create table
        table = new Table();
        table.setSize(BAR_WIDTH, BAR_HEIGHT);
        float screenWidth = stage.getWidth();
        float screenHeight = stage.getHeight();
        table.setPosition((screenWidth - BAR_WIDTH) / 2, 140);

        // Create simple health bar
        ProgressBar.ProgressBarStyle healthBarStyle = createBarStyle();
        healthBar = new ProgressBar(0, maxHealth, 1, false, healthBarStyle);
        healthBar.setValue(maxHealth); // Full health
        healthBar.setAnimateDuration(0f);

        // Add to table
        table.add(healthBar).width(BAR_WIDTH).height(BAR_HEIGHT).pad(5);

        // Add to stage
        stage.addActor(table);

        System.out.println("Health bar added to stage");
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