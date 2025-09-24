package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.services.ServiceLocator;

/**
 * Spawns random ground warnings at a fixed cooldown; after a short warning time,
 * a missile is spawned above that position and falls straight down.
 * <p>
 * Usage:
 * - Attach this component to a boss.
 * - Call setAttack(true) to start the periodic warning/missile cycle.
 * - Call setAttack(false) to stop it.
 * <p>
 * Notes:
 * - World/random range is controlled by minX/maxX and minY/maxY.
 * - Only visuals are created for warnings; the entity is disposed when the missile launches.
 */
public class MissueAttackComponent extends Component {
    private static final float COOLDOWN = 0.3f;   // seconds between warning spawns
    private static final int COUNT = 1;           // warnings per spawn wave
    private static final float WARN_TIME = 2f;     // seconds a warning stays before missile drops
    private static final float SKY_HEIGHT = 7f;    // missile spawn height above ground warning
    private static final float MIN_X = 0f, MAX_X = 30f; // horizontal range for warnings

    /**
     * Whether the system is active (true = spawning warnings/missiles).
     */
    private boolean attack = false;

    /**
     * One active warning's state.
     */
    private static class WarningEntry {
        Vector2 pos;     // ground position of the warning
        float t;         // elapsed time since warning was spawned
        Entity visual;   // the spawned warning entity (for cleanup)
    }

    /**
     * Currently active warnings.
     */
    private final java.util.ArrayList<WarningEntry> actives = new java.util.ArrayList<>();
    /**
     * Timer tracking time since last spawn wave.
     */
    private float timer = 0f;

    @Override
    public void update() {
        if (!attack) {
            return; // do nothing while disabled
        }
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer += dt;
        // Periodically spawn new warnings
        if (timer >= COOLDOWN) {
            timer = 0f;
            spawnWarnings();
        }

        // Advance warnings and drop missiles when their warn time expires
        for (int i = actives.size() - 1; i >= 0; i--) {
            WarningEntry w = actives.get(i);
            w.t += dt;
            if (w.t >= WARN_TIME) {
                launchMissile(w.pos);
                if (w.visual != null) {
                    w.visual.dispose();
                }
                actives.remove(i);
            }
        }
    }

    /**
     * Spawn a wave of ground warnings at random positions within the configured bounds.
     */
    private void spawnWarnings() {
        for (int i = 0; i < COUNT; i++) {
            float x = MathUtils.random(MIN_X, MAX_X);
            float y = 3f;
            Vector2 ground = new Vector2(x, y);

            // Visual warning on the ground
            Entity warning = BossFactory.createWarning(ground);
            ServiceLocator.getEntityService().register(warning);

            // Track its lifetime
            WarningEntry entry = new WarningEntry();
            entry.pos = ground;
            entry.t = 0f;
            entry.visual = warning;
            actives.add(entry);
        }
    }

    /**
     * Spawn a missile above the given ground point. Movement handled by the missile itself.
     */
    private void launchMissile(Vector2 ground) {
        Vector2 spawn = new Vector2(ground.x, ground.y + SKY_HEIGHT); //passed through to factory if used there
        Entity missile = BossFactory.createMissle(spawn);
        ServiceLocator.getEntityService().register(missile);
    }

    /**
     * Clean up any lingering warning visuals when this component is disposed.
     */
    @Override
    public void dispose() {
        for (WarningEntry w : actives) {
            if (w.visual != null) {
                w.visual.dispose();
            }
        }
        actives.clear();
    }

    /**
     * Enable/disable the attack cycle.
     */
    public void setAttack(boolean attack) {
        this.attack = attack;
    }
}