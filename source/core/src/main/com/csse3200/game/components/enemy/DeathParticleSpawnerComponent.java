package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Spawns a one-shot explosion particle animation when the attached enemy dies.
 */
public class DeathParticleSpawnerComponent extends Component {
  private static final List<String> VALID_ANIMATIONS = Arrays.asList(
      "explosion_1",
      "explosion_2"
  );
  private static final Logger logger = LoggerFactory.getLogger(DeathParticleSpawnerComponent.class);
  private String atlasPath = "images/explosion_1.atlas";
  private String animName = "explosion_1";
  private float frameDuration = 0.08f;

  /**
   * Default constructor.
   * Uses the default death animation ("explosion_1").
   */
  public DeathParticleSpawnerComponent() {}

  /**
   * Constructor with custom animation.
   * Sets the animation to the given name if valid; adjusts frame duration if necessary.
   * Throws IllegalArgumentException if the animation name is invalid.
   *
   * @param animationName the name of the death animation to use
   */
  public DeathParticleSpawnerComponent(String animationName) {
    this.animName = animationName;
    this.atlasPath = "images/explosion_2";
    if ("explosion_2".equals(animationName)) {
      this.frameDuration = 0.06f;
    }
    if (!VALID_ANIMATIONS.contains(animationName)) {
      throw new IllegalArgumentException(
              "Invalid animation name: " + animationName + ". Default animation is now explosion_1."
      );
    }
  }

  @Override
  public void create() {
    entity.getEvents().addListener("death", this::spawnParticles);
  }

  private void spawnParticles() {
    try {
      if (ServiceLocator.getResourceService() == null || !ServiceLocator.getResourceService().containsAsset(atlasPath, TextureAtlas.class)) {
        logger.debug("Explosion atlas not loaded; skipping death particles");
        return;
      }
      TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(atlasPath, TextureAtlas.class);
      Entity effect = new Entity();
      AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
      arc.setDisposeAtlas(false); // shared atlas managed by ResourceService
      arc.addAnimation(animName, frameDuration, Animation.PlayMode.NORMAL);
      effect.addComponent(arc).addComponent(new OneShotDisposeComponent());
      effect.setPosition(entity.getPosition());
      EntityService es = ServiceLocator.getEntityService();
      if (es != null) {
        es.register(effect);
      }
      arc.startAnimation(animName);
      // Hide and schedule disposal of original enemy
      entity.setEnabled(false);
      // Ensure enemy animator won't dispose shared atlas
      AnimationRenderComponent enemyArc = entity.getComponent(AnimationRenderComponent.class);
      if (enemyArc != null) {
        enemyArc.setDisposeAtlas(false);
      }
      Gdx.app.postRunnable(() -> entity.dispose());
    } catch (Exception e) {
      logger.error("Failed to spawn death particles", e);
    }
  }

  /** Component that disposes its entity once the named animation finishes. */
  private static class OneShotDisposeComponent extends Component {
    private boolean scheduled;
    @Override
    public void update() {
      if (scheduled) return; // already scheduled for removal
      AnimationRenderComponent arc = entity.getComponent(AnimationRenderComponent.class);
      if (arc != null && arc.getCurrentAnimation() != null && arc.isFinished()) {
        scheduled = true;
        // Defer disposal to after current frame to avoid nested iteration over components array
        Gdx.app.postRunnable(() -> {
          // Extra null/flag safety
            entity.dispose();
        });
      }
    }
  }
}
