package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.ui.UIComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** A UI component for displaying player stats (health, stamina, processor, ammo). */
public class PlayerStatsDisplay extends UIComponent {
    // UI constants
    private static final float BAR_WIDTH = 200f;
    private static final float BAR_HEIGHT = 30f;

    // Colours
    private static final Color COLOR_BG = Color.DARK_GRAY;
    private static final Color COLOR_HEALTH = Color.RED;
    private static final Color COLOR_STAMINA = Color.GREEN;

    // Track textures we create so we can dispose them explicitly
    private final List<Texture> disposableTextures = new ArrayList<>();

    // Cached UI
    private Table table;
    private ProgressBar healthBar;
    private ProgressBar staminaBar;
    private Label processorLabel;
    private Label ammoLabel;

    // Cached components (avoid repeated lookups)
    private CombatStatsComponent combatStats;
    private AmmoStatsComponent ammoStats;
    private InventoryComponent inventory;

    @Override
    public void create() {
        super.create();

        // cache frequently-used components once
        combatStats = entity.getComponent(CombatStatsComponent.class);
        ammoStats = entity.getComponent(AmmoStatsComponent.class);
        inventory = entity.getComponent(InventoryComponent.class);

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
     * Helper to create a colored drawable from a 1x1 pixel texture.
     */
    private Drawable makeColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        try {
            pixmap.setColor(color);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            disposableTextures.add(texture);
            return new TextureRegionDrawable(new TextureRegion(texture));
        } finally {
            pixmap.dispose();
        }
    }

    /**
     * Creates a simple horizontal bar style with a colored fill and dark background.
     */
    private ProgressBar.ProgressBarStyle makeBarStyle(Color fill) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = makeColorDrawable(COLOR_BG);
        style.background.setMinHeight(BAR_HEIGHT);
        style.knobBefore = makeColorDrawable(fill);
        style.knobBefore.setMinHeight(BAR_HEIGHT);
        style.knob = null; // continuous fill
        return style;
    }

    /**
     * Creates actors and positions them on the stage using a table.
     */
    private void addActors() {
        table = new Table();
        table.top().left();
        table.setFillParent(true);
        table.padTop(45f).padLeft(5f);

        // Health bar
        ProgressBar.ProgressBarStyle healthBarStyle = makeBarStyle(COLOR_HEALTH);
        int health = combatStats != null ? combatStats.getHealth() : 0;
        healthBar = new ProgressBar(0, 100, 1, false, healthBarStyle);
        healthBar.setValue(health);
        healthBar.setAnimateDuration(0f);

        // Stamina bar (0..100 shows percent so UI is decoupled from gameplay max)
        ProgressBar.ProgressBarStyle staminaStyle = makeBarStyle(COLOR_STAMINA);
        staminaBar = new ProgressBar(0, 100, 1, false, staminaStyle);
        staminaBar.setValue(100);
        staminaBar.setAnimateDuration(0f);

        // Processor label
        int processor = inventory != null ? inventory.getProcessor() : 0;
        processorLabel = new Label(String.format(Locale.ROOT, "Processor: %d", processor), skin, "large");

        // Ammo label
        int ammoReserves = ammoStats != null ? ammoStats.getAmmo() : 0;
        ammoLabel = new Label(String.format(Locale.ROOT, "Ammo: %d", ammoReserves), skin, "large");

        // Layout
        table.add(healthBar).width(BAR_WIDTH).height(BAR_HEIGHT).pad(5);
        table.row();
        table.add(staminaBar).width(BAR_WIDTH).height(BAR_HEIGHT).pad(5);
        table.row();
        table.add(processorLabel).left().padLeft(10f);
        table.row();
        table.add(ammoLabel).left().padLeft(10f);
        table.row();

        // IMPORTANT: add to stage so it actually renders
        stage.addActor(table);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    /**
     * Updates the player's health on the UI (expects 0..100).
     */
    public void updatePlayerHealthUI(int health) {
        if (healthBar != null) healthBar.setValue(Math.clamp(health, 0, 100));
    }

    /**
     * Updates the player's stamina on the UI.
     */
    public void updatePlayerStaminaUI(int current, int max) {
        if (staminaBar == null || max <= 0) return;
        float pct = (current * 100f) / max;
        staminaBar.setValue(Math.clamp(pct, 0f, 100f));
    }

    /**
     * Updates the player's processor on the UI.
     */
    public void updatePlayerProcessorUI(int processor) {
        if (processorLabel != null) {
            processorLabel.setText(String.format(Locale.ROOT, "Processor: %d", processor));
        }
    }

    /**
     * Updates the ammo display (shoot/reload/pickup/etc.).
     */
    public void updateAmmoUI() {
        if (ammoLabel == null) return;

        int reserves = ammoStats != null ? ammoStats.getAmmo() : 0;
        Entity equipped = inventory != null ? inventory.getCurrItem() : null;
        setAmmoText(reserves, equipped);
    }

    /**
     * Updates the ammo display when the inventory slot is switched.
     */
    public void updateAmmoUIAfterSwitch(int focusItem) {
        if (ammoLabel == null) return;

        int reserves = ammoStats != null ? ammoStats.getAmmo() : 0;
        Entity equipped = (inventory != null) ? inventory.get(focusItem) : null;
        setAmmoText(reserves, equipped);
    }

    private void setAmmoText(int reserves, Entity equipped) {
        // default: show reserves only
        CharSequence text = String.format(Locale.ROOT, "Ammo: %d", reserves);

        if (equipped != null) {
            ItemComponent itemInfo = equipped.getComponent(ItemComponent.class);
            if (itemInfo != null && itemInfo.getType() == ItemTypes.RANGED) {
                MagazineComponent mag = equipped.getComponent(MagazineComponent.class);
                if (mag != null) {
                    text = String.format(Locale.ROOT, "Ammo: %d/%d", reserves, mag.getCurrentAmmo());
                }
            }
        }
        ammoLabel.setText(text);
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
        if (table != null) table.remove();
        if (healthBar != null) healthBar.remove();
        if (staminaBar != null) staminaBar.remove();
        if (processorLabel != null) processorLabel.remove();
        if (ammoLabel != null) ammoLabel.remove();

        for (Texture tex : disposableTextures) {
            if (tex != null) tex.dispose();
        }
        disposableTextures.clear();
    }
}
