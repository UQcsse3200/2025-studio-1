package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Plays a one-shot explosion animation when the owning Boss entity dies,
 * then disposes the temporary effect entity and the Boss entity itself safely.
 *
 * Assumptions:
 * - The texture atlas "images/boss_explosion.atlas" is preloaded in ResourceService.
 * - The atlas contains regions named "boss_explosion" with sequential indices (0..N).
 * - The game's rendering uses nearest filtering for crisp pixel-art (as set in the atlas).
 */
public class BossDeathComponent extends Component {
    /** Path of the prebuilt atlas that contains frames for the boss death explosion. */
    private static final String ATLAS_PATH = "images/boss_explosion.atlas";
    /** Base region name in the atlas; frames must be named with this base plus ascending indices. */
    private static final String ANIM_NAME  = "boss_explosion";

    /** Frame duration in seconds (lower = faster). */
    private final float frameDuration;
    /** Visual scale multiplier applied after sizing the effect to the frame size. Use integers (2/3/4) for pixel art. */
    private final float scaleMultiplier;

    /**
     * Default configuration: 0.06s per frame, scaled up 4×.
     * Suitable for a large, impactful explosion without being too slow.
     */
    public BossDeathComponent() {
        this(0.06f, 4f);
    }

    /**
     * Custom configuration.
     * @param frameDuration   Seconds per frame, clamped to a small positive value.
     * @param scaleMultiplier Scale multiplier for the explosion entity; integer values keep pixel-art crisp.
     */
    public BossDeathComponent(float frameDuration, float scaleMultiplier) {
        this.frameDuration = Math.max(0.001f, frameDuration);
        this.scaleMultiplier = Math.max(0.01f, scaleMultiplier);
    }

    /**
     * Subscribes to the owning entity's "death" event when the component is created.
     * When "death" is triggered elsewhere (e.g., in the health/combat system),
     * this component will spawn and play the explosion.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("death", this::spawnExplosion);
    }

    /**
     * Spawns a temporary effect entity at the boss position, plays the explosion once,
     * and schedules safe disposal of both the effect and the boss entity.
     *
     * This method is defensive:
     * - If the atlas isn't loaded in ResourceService, it logs and returns without crashing.
     * - Uses postRunnable() to avoid modifying entity lists during iteration.
     */
    private void spawnExplosion() {
        // Ensure the ResourceService is present and the atlas is loaded.
        if (ServiceLocator.getResourceService() == null ||
                !ServiceLocator.getResourceService().containsAsset(ATLAS_PATH, TextureAtlas.class)) {
            Gdx.app.debug("BossDeath", "Atlas not loaded: " + ATLAS_PATH + " (skip explosion)");
            return;
        }

        // Obtain the shared atlas. Its lifecycle is managed by ResourceService, not by the effect entity.
        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(ATLAS_PATH, TextureAtlas.class);

        // Create the transient effect entity that will render the explosion.
        Entity effect = new Entity();

        // Attach an AnimationRenderComponent to handle the sprite sequence.
        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        arc.setDisposeAtlas(false); // Prevent disposing a shared atlas when the effect entity is disposed.
        arc.addAnimation(ANIM_NAME, frameDuration, Animation.PlayMode.NORMAL);

        // Add the renderer and a helper component that disposes the effect when the animation finishes.
        effect.addComponent(arc).addComponent(new OneShotDisposeComponent());

        // Place the effect at the same position as the boss.
        effect.setPosition(entity.getPosition());

        // Resize the effect entity to match the frame size, then apply an additional visual scale.
        arc.scaleEntity();
        if (scaleMultiplier != 1f) {
            Vector2 s = effect.getScale();
            effect.setScale(s.x * scaleMultiplier, s.y * scaleMultiplier);
        }

        // Register the effect so it becomes active in the world and starts updating/rendering.
        EntityService es = ServiceLocator.getEntityService();
        if (es != null) {
            es.register(effect);
        }

        // Start the explosion animation.
        arc.startAnimation(ANIM_NAME);

        // Hide the boss immediately, and ensure the boss renderer won't dispose the shared atlas.
        entity.setEnabled(false);
        AnimationRenderComponent bossArc = entity.getComponent(AnimationRenderComponent.class);
        if (bossArc != null) {
            bossArc.setDisposeAtlas(false);
        }

        // Dispose the boss safely after the current frame to avoid concurrent modification issues.
        Gdx.app.postRunnable(entity::dispose);
    }

    /**
     * Minimal helper component that disposes its owning entity once its current animation finishes.
     * This assumes the owning entity has an AnimationRenderComponent playing a finite animation.
     */
    private static class OneShotDisposeComponent extends Component {
        private boolean scheduled;

        @Override
        public void update() {
            if (scheduled) return;

            AnimationRenderComponent arc = entity.getComponent(AnimationRenderComponent.class);
            // Guard against nulls and ensure an animation is active before checking completion.
            if (arc != null && arc.getCurrentAnimation() != null && arc.isFinished()) {
                scheduled = true;
                // Dispose on the application thread after the current frame to avoid iterator invalidation.
                Gdx.app.postRunnable(entity::dispose);
            }
        }
    }
}

