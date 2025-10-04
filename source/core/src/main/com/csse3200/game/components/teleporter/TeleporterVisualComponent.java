package com.csse3200.game.components.teleporter;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Handles teleporter visual states:
 *  - idle (n1)
 *  - activation step 1 (n2)
 *  - activation step 2 (n3) then run supplied callback (e.g. area transition)
 */
public class TeleporterVisualComponent extends RenderComponent {
    private final Texture idle;
    private final Texture act1;
    private final Texture act2;

    private Texture current;

    private enum State { IDLE, ACT1, ACT2, DONE }
    private State state = State.IDLE;
    private float elapsed;
    private float frameDuration = 0.15f; // time per activation frame
    private Runnable onComplete;
    private final GameTime time;

    public TeleporterVisualComponent(Texture idle, Texture act1, Texture act2) {
        this.idle = idle;
        this.act1 = act1;
        this.act2 = act2;
        this.current = idle;
        this.time = ServiceLocator.getTimeSource();
    }

    /** Begin activation sequence if not already running. */
    public void activate(Runnable after) {
        if (state != State.IDLE) return; // ignore if already activating/finished
        state = State.ACT1;
        elapsed = 0f;
        current = act1;
        onComplete = after;
    }

    @Override
    public void update() {
        float dt = time != null ? time.getDeltaTime() : 1/60f;
        switch (state) {
            case ACT1 -> updateAct1(dt);
            case ACT2 -> updateAct2(dt);
            default -> {}
        }
    }

    private void updateAct1(float dt) {
        elapsed += dt;
        if (elapsed >= frameDuration) {
            state = State.ACT2;
            elapsed = 0f;
            current = act2;
        }
    }

    private void updateAct2(float dt) {
        elapsed += dt;
        if (elapsed >= frameDuration) {
            state = State.DONE;
            elapsed = 0f;
            if (onComplete != null) {
                Runnable cb = onComplete;
                onComplete = null; // prevent repeat
                cb.run();
            }
            // Do not revert to idle; new area will likely load. If needed, set back:
            // current = idle; state = State.IDLE;
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        if (current == null) return;
        Vector2 pos = entity.getPosition();
        Vector2 scale = entity.getScale();
        batch.draw(current, pos.x, pos.y, scale.x, scale.y);
    }

    /** Reset to idle (optional external use). */
    public void reset() {
        state = State.IDLE;
        current = idle;
        elapsed = 0f;
        onComplete = null;
    }

    public void setFrameDuration(float seconds) {
        if (seconds > 0f) this.frameDuration = seconds;
    }
}
