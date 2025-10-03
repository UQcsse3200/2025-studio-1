package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.enemy.EnemyDeathRewardComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A ui component for displaying player stats, e.g. health.
 */
public class PlayerStatsDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PlayerStatsDisplay.class);
    // UI constants
    private static final float BAR_WIDTH = 200f;
    private static final float BAR_HEIGHT = 30f;

    // Colours
    private static final Color COLOR_BG = Color.DARK_GRAY;
    private static final Color COLOR_HEALTH = Color.RED;
    private static final Color COLOR_STAMINA = Color.GREEN;

    // Track textures we create so we can dispose them explicitly
    private final List<Texture> disposableTextures = new ArrayList<>();

    private Table table;
    private ProgressBar healthBar;
    private TextButton killEnemyButton;
    private ProgressBar staminaBar;
    private Label processorLabel;
    private Label ammoLabel;

    /**
     * Creates reusable ui styles and adds actors to the stage.
     */
    @Override
    public void create() {
        super.create();
        addActors();

        entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);
        entity.getEvents().addListener("updateProcessor", this::updatePlayerProcessorUI);
        entity.getEvents().addListener("staminaChanged", this::updatePlayerStaminaUI);
        entity.getEvents().addListener("shoot", this::updateAmmoUI);
        entity.getEvents().addListener("reload", this::updateAmmoUI);
        entity.getEvents().addListener("pick up", this::updateAmmoUI);
        entity.getEvents().addListener("ammo replenished", this::updateAmmoUI);
        entity.getEvents().addListener("focus item", this::updateAmmoUIAfterSwitch);

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
        disposableTextures.add(texture);
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
     * Creates actors and positions them on the stage using a table.
     *
     * @see Table for positioning options
     */
    private void addActors() {
        table = new Table();
        table.top().left();
        table.setFillParent(true);
        table.padTop(45f).padLeft(5f);

        // Health bar
        ProgressBar.ProgressBarStyle healthBarStyle = makeBarStyle(COLOR_HEALTH, BAR_HEIGHT);
        int health = entity.getComponent(CombatStatsComponent.class).getHealth();
        healthBar = new ProgressBar(0, 100, 1, false, healthBarStyle);
        healthBar.setValue(health);
        healthBar.setAnimateDuration(0f);

        // Stamina bar (0..100 shows percent so UI is decoupled from gameplay max)
        ProgressBar.ProgressBarStyle staminaStyle = makeBarStyle(COLOR_STAMINA, BAR_HEIGHT);
        staminaBar = new ProgressBar(0, 100, 1, false, staminaStyle);
        staminaBar.setValue(100);
        staminaBar.setAnimateDuration(0f);

        // Processor label
        int processor = entity.getComponent(InventoryComponent.class).getProcessor();
        CharSequence processorText = String.format("Processor: %d", processor);
        processorLabel = new Label(processorText, skin, "white");

        // Ammo label
        int ammo = entity.getComponent(AmmoStatsComponent.class).getAmmo();
        CharSequence ammoText = String.format("Ammo: %d", ammo);
        ammoLabel = new Label(ammoText, skin, "white");

        // Layout:
        // Row 1: Health bar
        table.add(healthBar).width(BAR_WIDTH).height(BAR_HEIGHT).pad(5).left();
        table.row();
        // Row 2: Stamina bar
        table.add(staminaBar).width(BAR_WIDTH).height(BAR_HEIGHT).pad(5).left();
        table.row();
        // Row 3: Processor label
        table.add(processorLabel).left().padLeft(5f);
        table.row();
        // Row 4: Ammo label
        table.add(ammoLabel).left().padLeft(5f);
        table.row();

        killEnemyButton = new TextButton("Kill Enemy", skin);
        killEnemyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                killOneEnemy();
            }
        });
        table.add(killEnemyButton).left().padTop(5f);
        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    /**
     * Updates the player's health on the UI.
     *
     * @param health player health
     */
    public void updatePlayerHealthUI(int health) {
        healthBar.setValue(health);
    }

    /**
     * Updates the player's stamina on the UI.
     *
     * @param current the current stamina value
     * @param max     the max stamina
     */
    public void updatePlayerStaminaUI(int current, int max) {
        float pct = (current * 100f) / max;
        if (pct < 0f) pct = 0f;
        if (pct > 100f) pct = 100f;
        staminaBar.setValue(pct);
    }

    /**
     * Updates the player's processor on the UI.
     *
     * @param processor player processor
     */
    public void updatePlayerProcessorUI(int processor) {
        CharSequence text = String.format("Processor: %d", processor);
        processorLabel.setText(text);
    }


    /**
     * Updates the Player's ammo display on the UI when they shoot or reload
     */
    public void updateAmmoUI() {

        int ammoReserves = entity.getComponent(AmmoStatsComponent.class).getAmmo();
        Entity equipped = entity.getComponent(InventoryComponent.class).getCurrItem();

        if (equipped == null) {

            CharSequence text = String.format("Ammo: %d", ammoReserves);
            ammoLabel.setText(text);
            return;
        }

        ItemComponent itemInfo = equipped.getComponent(ItemComponent.class);
        if (itemInfo.getType() == ItemTypes.RANGED) {

            MagazineComponent mag = equipped.getComponent(MagazineComponent.class);
            CharSequence text = String.format("Ammo: %d/%d", ammoReserves, mag.getCurrentAmmo());
            ammoLabel.setText(text);
        }
    }

    /**
     * Updates the ammo UI after the inventory slot is switched
     */
    public void updateAmmoUIAfterSwitch(int focusItem) {

        int ammoReserves = entity.getComponent(AmmoStatsComponent.class).getAmmo();
        Entity equipped = entity.getComponent(InventoryComponent.class).get(focusItem);

        if (equipped == null) {

            CharSequence text = String.format("Ammo: %d", ammoReserves);
            ammoLabel.setText(text);
            return;
        }

        ItemComponent itemInfo = equipped.getComponent(ItemComponent.class);
        if (itemInfo.getType() == ItemTypes.RANGED) {

            MagazineComponent mag = equipped.getComponent(MagazineComponent.class);
            CharSequence text = String.format("Ammo: %d/%d", ammoReserves, mag.getCurrentAmmo());
            ammoLabel.setText(text);
        }

    }

    /**
     * Kills one enemy entity in the game for testing purposes.
     * Only kills an enemy that has a reward component, and disposes it after death.
     */
    private void killOneEnemy() {
        EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.debug("No EntityService registered; cannot kill enemy");
            return;
        }
        for (Entity e : es.getEntities()) {
            if (e == this.entity) continue; // skip player self
            CombatStatsComponent stats = e.getComponent(CombatStatsComponent.class);
            if (stats == null || !hasRewardComponent(e)) continue;
            if (stats.getHealth() <= 0) continue; // already dead
            logger.debug("Killing enemy {} via debug button", e);
            stats.setHealth(0); // triggers death + reward + particles
            break; // only kill one per click
        }
    }

    /**
     * Checks if the entity has an EnemyDeathRewardComponent.
     */
    private boolean hasRewardComponent(Entity e) {
        return e.getComponent(EnemyDeathRewardComponent.class) != null;
    }

    /**
     * For use in test code
     */
    protected void setHealthBar(ProgressBar bar) {
        this.healthBar = bar;
    }

    /**
     * For use in test code
     */
    protected void setProcessorLabel(Label label) {
        this.processorLabel = label;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (healthBar != null) healthBar.remove();
        if (staminaBar != null) staminaBar.remove();
        if (processorLabel != null) processorLabel.remove();
        // Dispose textures we created for the drawables
        for (Texture tex : disposableTextures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        disposableTextures.clear();
    }
}