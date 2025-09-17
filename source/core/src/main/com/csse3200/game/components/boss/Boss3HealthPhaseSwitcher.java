package com.csse3200.game.components.boss;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A component that switches Boss-3's rendering from a static PNG
 *
 * This allows the boss to visually "crack" or change form at 50%, 40%, and 25% health.
 * The atlas is pre-attached during entity creation, but disabled until triggered.
 *
 * The thresholds are ratios of current health / max health.
 */
public class Boss3HealthPhaseSwitcher extends Component {
    private static class Phase {
        final float threshold;
        final String animName;
        Phase(float t, String n) { threshold = t; animName = n; }
    }

    private final String atlasPath;
    private final float frameDuration;
    private final List<Phase> phases = new ArrayList<>();
    private float lastAppliedThreshold = 1.01f;

    public Boss3HealthPhaseSwitcher(String atlasPath, float frameDuration) {
        this.atlasPath = atlasPath;
        this.frameDuration = frameDuration;
    }

    public Boss3HealthPhaseSwitcher addPhase(float threshold, String animationName) {
        phases.add(new Phase(threshold, animationName));
        return this;
    }

    /**
     * Called when the component is created.
     * Sorts thresholds in descending order and applies the correct phase immediately
     * (in case the boss spawns below full health).
     */
    @Override
    public void create() {
        phases.sort(Comparator.comparingDouble((Phase p) -> p.threshold).reversed());
        applyIfNeeded(getHealthRatio());
    }

    /**
     * Called once per frame.
     * Checks current health ratio and applies phase changes when thresholds are crossed.
     */
    @Override
    public void update() {
        applyIfNeeded(getHealthRatio());
    }

    /**
     * @return Current health ratio (0.0–1.0) of the boss entity,
     *         or 1.0 if combat stats are missing.
     */
    private float getHealthRatio() {
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        if (stats == null || stats.getMaxHealth() <= 0) return 1f;
        return (float) stats.getHealth() / (float) stats.getMaxHealth();
    }

    /**
     * Apply the correct animation if the boss has crossed into a new phase.
     *
     * @param ratio Current health ratio.
     */
    private void applyIfNeeded(float ratio) {
        for (Phase p : phases) {
            if (ratio <= p.threshold && lastAppliedThreshold > p.threshold) {
                swapToAtlasAndPlay(p.animName);
                lastAppliedThreshold = p.threshold;
                break;
            }
        }
    }

    /**
     * Switch rendering from the static PNG to the atlas animations.
     * Disables the {@link TextureRenderComponent}, enables the
     * {@link AnimationRenderComponent}, and starts the given animation.
     *
     * @param animName The animation to start (must exist in the atlas).
     */
    private void swapToAtlasAndPlay(String animName) {
        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(atlasPath, TextureAtlas.class);

        AnimationRenderComponent arc = entity.getComponent(AnimationRenderComponent.class);
        if (arc == null) {
            Vector2 scale = entity.getScale();

            TextureRenderComponent trc = entity.getComponent(TextureRenderComponent.class);
            if (trc != null) trc.setEnabled(false);

            arc = new AnimationRenderComponent(atlas);
            arc.setDisposeAtlas(false);
            for (Phase p : phases) {
                arc.addAnimation(p.animName, frameDuration, Animation.PlayMode.LOOP);
            }
            entity.addComponent(arc);
            entity.setScale(scale.x, scale.y);
            arc.scaleEntity();
        }

        if (arc.hasAnimation(animName)) {
            arc.startAnimation(animName);
        }
    }
}
