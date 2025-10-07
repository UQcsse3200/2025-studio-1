package com.csse3200.game.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Component for a breakable platform that shakes, fades, and disappears after a player steps on it.
 * The platform listens for collision events to detect when a player lands on it.
 * Once triggered, it will wait for a configurable delay before starting a shaking animation,
 * then disables the platform's physics body and smoothly fades out its texture before removal.
 * All timing and effect parameters are configurable via constructor or can be customized by subclassing.
 */

public class BreakablePlatformComponent extends Component {
    // Configurable timing fields (with default values)
    private final float shakeDelay;
    private final float shakeDuration;
    private final float shakeAmount;
    private final float fadeDelay;
    private final float fadeDuration;

    private boolean triggered = false;
     /* Reference to the texture render component used for fading effect. */
    private TextureRenderComponent render;

    public BreakablePlatformComponent() {
        shakeDelay = 1f;
        shakeDuration = 0.5f;
        shakeAmount = 0.13f;
        fadeDelay = 0.5f;
        fadeDuration = 1.5f;
    }

    @Override
    public void create() {
        render = entity.getComponent(TextureRenderComponent.class);
        entity.getEvents().addListener("collisionStart", this::onCollision);
    }
    /**
     * Handles collision events from physics engine.
     * Checks if the colliding entity is the player standing on top,
     * and if so, triggers shaking and break sequence after configured delay.
     *
     * @param platformFixture Fixture of this platform.
     * @param otherFixture    Fixture of the other colliding entity.
     */
    private void onCollision(Fixture platformFixture, Fixture otherFixture) {
        Object data = otherFixture.getBody().getUserData();
        Entity other = (data instanceof BodyUserData userData) ? userData.entity : null;

        if (!triggered && other != null && other.getComponent(InventoryComponent.class) != null) {
            float playerY = other.getPosition().y;
            float platformY = entity.getPosition().y;
            /* Only trigger break if player is standing above with some tolerance */
            if (playerY > platformY + 0.2f) {
                triggered = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        shake();
                    }
                }, shakeDelay);
            }
        }
    }
    /**
     * Starts the shaking animation by moving the platform back and forth randomly for the configured duration.
     * Once shaking completes, initiates the fade out.
     */
    private void shake() {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) return;

        Body body = physics.getBody();
        Vector2 startPos = entity.getPosition();

        Timer.schedule(new Timer.Task() {
            float time = 0f;

            @Override
            public void run() {
                if (body != null && render != null) {
                    float factor = 1f - (time / shakeDuration);
                    float x = startPos.x + (float)Math.sin(time * 40) * shakeAmount * factor;
                    float y = startPos.y + (float)Math.cos(time * 40) * shakeAmount * factor;
                    body.setTransform(x, y, 0);
                    entity.setPosition(body.getPosition(), false);
                }

                time += 0.05f;
                if (time >= shakeDuration) {
                    if (body != null) body.setTransform(startPos, 0);
                    entity.setPosition(startPos, false);
                    this.cancel();
                    fade();
                }
            }
        }, 0f, 0.05f);
    }
    /**
     * Starts the fading effect by gradually reducing the alpha of the platform's texture.
     * Disables the physics body to allow the player to fall through.
     * Once fading completes, marks the entity for removal.
     */
    private void fade() {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        Body body = physics != null ? physics.getBody() : null;

        // Disable hitbox so player falls through
        if (body != null) body.setActive(false);

        Timer.schedule(new Timer.Task() {
            float time = 0f;

            @Override
            public void run() {
                if (render != null) {
                    render.setAlpha(Interpolation.smooth.apply(1f, 0f, time / fadeDuration));
                }

                time += 0.05f;
                if (time >= fadeDuration) {
                    this.cancel();
                    entity.setToRemove();
                }
            }
        }, fadeDelay, 0.05f);
    }
}
