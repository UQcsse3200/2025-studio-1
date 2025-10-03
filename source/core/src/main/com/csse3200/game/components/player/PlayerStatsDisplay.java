package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.enemy.EnemyDeathRewardComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.screens.BaseScreenDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.services.ServiceLocator;

import static java.lang.Math.clamp;

public class PlayerStatsDisplay extends BaseScreenDisplay {
    // UI constants
    private static final float BAR_WIDTH = 200f;
    private static final float BAR_HEIGHT = 30f;
    private static final float PAD = 5f;
    private static final int PCT_MAX = 100;

    // Colours
    private static final Color COLOR_BG = Color.DARK_GRAY;
    private static final Color COLOR_HEALTH = Color.RED;
    private static final Color COLOR_STAMINA = Color.GREEN;
    // Ammo formats (dedupe string literals)
    private static final String AMMO_SINGLE_FMT = "Ammo: %d";
    private static final String AMMO_DUAL_FMT = "Ammo: %d/%d";
    // Cached components (may be null if not present)
    private CombatStatsComponent combat;
    private InventoryComponent inventory;
    private AmmoStatsComponent ammoStats;
    // UI
    private ProgressBar healthBar;
    private ProgressBar staminaBar;
    private Label processorLabel;
    private Label ammoLabel;

    public PlayerStatsDisplay() {
        super(null);
    }

    @Override
    public void create() {
        // Cache BEFORE super.create(), since super.create() -> buildUI() needs these
        combat = entity.getComponent(CombatStatsComponent.class);
        inventory = entity.getComponent(InventoryComponent.class);
        ammoStats = entity.getComponent(AmmoStatsComponent.class);

        super.create(); // sets up root/neon and calls buildUI(root)
        registerListeners();
    }

    private void registerListeners() {
        entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);
        entity.getEvents().addListener("staminaChanged", this::updatePlayerStaminaUI);
        entity.getEvents().addListener("updateProcessor", this::updatePlayerProcessorUI);
        entity.getEvents().addListener("shoot", this::updateAmmoUI);
        entity.getEvents().addListener("reload", this::updateAmmoUI);
        entity.getEvents().addListener("pick up", this::updateAmmoUI);
        entity.getEvents().addListener("ammo replenished", this::updateAmmoUI);
        entity.getEvents().addListener("focus item", this::updateAmmoUIAfterSwitch);
    }

    /* Build & draw */
    @Override
    protected void buildUI(Table root) {
        // Health bar: use real max if available, otherwise [0..100]
        int healthVal = (combat != null) ? combat.getHealth() : 0;
        int maxHealth = (combat != null) ? combat.getMaxHealth() : PCT_MAX;

        healthBar = new ProgressBar(0, maxHealth, 1, false, makeBarStyle(COLOR_HEALTH));
        healthBar.setAnimateDuration(0f);
        healthBar.setValue(clamp(healthVal, 0, maxHealth));

        // Stamina bar as percentage [0..100]
        staminaBar = new ProgressBar(0, PCT_MAX, 1, false, makeBarStyle(COLOR_STAMINA));
        staminaBar.setAnimateDuration(0f);
        staminaBar.setValue(PCT_MAX);

        // Processor label
        int processor = (inventory != null) ? inventory.getProcessor() : 0;
        processorLabel = new Label(formatProcessor(processor), skin, "white");

        // Ammo label
        ammoLabel = new Label(formatAmmoLabel(), skin, "white");

        // Layout top-left
        root.top().left().padTop(45f).padLeft(5f);
        root.add(healthBar).width(BAR_WIDTH).height(BAR_HEIGHT).pad(PAD);
        root.row();
        root.add(staminaBar).width(BAR_WIDTH).height(BAR_HEIGHT).pad(PAD);
        root.row();
        root.add(processorLabel).left().padLeft(10f);
        root.row();
        root.add(ammoLabel).left().padLeft(10f);
        root.row();
    }

    @Override
    public void draw(SpriteBatch batch) { /* Stage handles rendering */ }

    /* Update handlers */

    public void updatePlayerHealthUI(int health) {
        if (healthBar == null) return;
        float max = healthBar.getMaxValue();
        healthBar.setValue(clamp(health, 0, (int) max));
    }

    public void updatePlayerStaminaUI(int current, int max) {
        if (staminaBar == null || max <= 0) return;
        float pct = 100f * current / max;
        staminaBar.setValue(clamp(pct, 0f, 100f));
    }

    public void updatePlayerProcessorUI(int processor) {
        if (processorLabel == null) return;
        processorLabel.setText(formatProcessor(processor));
    }

    public void updateAmmoUI() {
        if (ammoLabel == null) return;
        ammoLabel.setText(formatAmmoLabel());
    }

    public void updateAmmoUIAfterSwitch(int focusItem) {
        if (ammoLabel == null) return;
        ammoLabel.setText(formatAmmoLabelAfterSwitch(focusItem));
    }

    /* Internals */
    private ProgressBar.ProgressBarStyle makeBarStyle(Color fill) {
        TextureRegionDrawable bg = new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(makeSolidTexture(COLOR_BG)));
        TextureRegionDrawable before = new TextureRegionDrawable(new TextureRegion(makeSolidTexture(fill)));


        bg.setMinHeight(PlayerStatsDisplay.BAR_HEIGHT);
        before.setMinHeight(PlayerStatsDisplay.BAR_HEIGHT);

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = bg;
        style.knobBefore = before;
        style.knob = null; // continuous fill
        return style;
    }

    private String formatProcessor(int p) {
        return String.format("Processor: %d", p);
    }

    // Centralised ammo formatting (DRY)
    private String formatAmmo(Entity equipped, int reserves) {
        if (equipped == null) return String.format(AMMO_SINGLE_FMT, reserves);

        ItemComponent info = equipped.getComponent(ItemComponent.class);
        if (info == null || info.getType() != ItemTypes.RANGED) {
            return String.format(AMMO_SINGLE_FMT, reserves);
        }

        MagazineComponent mag = equipped.getComponent(MagazineComponent.class);
        int inMag = (mag != null) ? mag.getCurrentAmmo() : 0;
        return String.format(AMMO_DUAL_FMT, reserves, inMag);
    }

    private String formatAmmoLabel() {
        int reserves = (ammoStats != null) ? ammoStats.getAmmo() : 0;
        Entity equipped = (inventory != null) ? inventory.getCurrItem() : null;
        return formatAmmo(equipped, reserves);
    }

    private String formatAmmoLabelAfterSwitch(int focusItem) {
        int reserves = (ammoStats != null) ? ammoStats.getAmmo() : 0;
        Entity equipped = (inventory != null) ? inventory.get(focusItem) : null;
        return formatAmmo(equipped, reserves);
    }

    /**
     * Debug helper: kill a single enemy with rewards.
     * (No continue/break warnings; early-return exits loop on first kill.)
     */
    @SuppressWarnings("unused")
    private void killOneEnemy() {
        EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.debug("No EntityService registered; cannot kill enemy");
            return;
        }
        for (Entity e : es.getEntities()) {
            boolean isPlayer = (e == this.entity);
            CombatStatsComponent stats = e.getComponent(CombatStatsComponent.class);
            boolean invalid = (stats == null || stats.getHealth() <= 0);
            boolean noReward = (e.getComponent(EnemyDeathRewardComponent.class) == null);

            if (!isPlayer && !invalid && !noReward) {
                logger.debug("Killing enemy {} via debug button", e);
                stats.setHealth(0);
                return; // stop after killing one
            }
        }
    }

    /* ---- Test hooks ---- */
    protected void setHealthBar(ProgressBar bar) {
        this.healthBar = bar;
    }

    protected void setProcessorLabel(Label label) {
        this.processorLabel = label;
    }
}
