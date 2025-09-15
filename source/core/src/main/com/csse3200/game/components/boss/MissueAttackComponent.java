package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.services.ServiceLocator;


public class MissueAttackComponent extends Component {
    private static final float COOLDOWN = 3f;
    private static final int COUNT = 3;
    private static final float WARN_TIME = 1f;
    private static final float SKY_HEIGHT = 5f;
    private static final float FALL_SPEED = 4f;
    private static final float MIN_X = -8f, MAX_X = 8f;
    private static final float MIN_Y = 0f,  MAX_Y = 6f;
    private boolean attack = false;

    private static class WarningEntry {
        Vector2 pos;
        float t;
        Entity visual;
    }

    private final java.util.ArrayList<WarningEntry> actives = new java.util.ArrayList<>();
    private float timer = 0f;

    @Override
    public void update() {
        if (!attack ) {
            return;
        }
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer += dt;

        if (timer >= COOLDOWN) {
            timer = 0f;
            spawnWarnings();
        }

        for (int i = actives.size() - 1; i >= 0; i--) {
            WarningEntry w = actives.get(i);
            w.t += dt;
            if (w.t >= WARN_TIME) {
                launchMissile(w.pos);
                if (w.visual != null) w.visual.dispose();
                actives.remove(i);
            }
        }
    }

    private void spawnWarnings() {
        for (int i = 0; i < COUNT; i++) {
            float x = MathUtils.random(MIN_X, MAX_X);
            float y = MathUtils.random(MIN_Y, MAX_Y);
            Vector2 ground = new Vector2(x, y);

            Entity warning = BossFactory.createWarning(ground);
            ServiceLocator.getEntityService().register(warning);

            WarningEntry entry = new WarningEntry();
            entry.pos = ground;
            entry.t = 0f;
            entry.visual = warning;
            actives.add(entry);
        }
    }
    private void launchMissile(Vector2 ground) {
        Vector2 spawn = new Vector2(ground.x, ground.y + SKY_HEIGHT);
        Vector2 vel = new Vector2(0f, -FALL_SPEED);
        Entity missile = BossFactory.createMissle(spawn, vel);
        missile.addComponent(new ConstantVelocityComponent(0f, -FALL_SPEED));
        ServiceLocator.getEntityService().register(missile);
    }
    private static class ConstantVelocityComponent extends Component {
        private final float vx, vy;
        public ConstantVelocityComponent(float vx, float vy) { this.vx = vx; this.vy = vy; }
        @Override public void update() {
            var phys = entity.getComponent(com.csse3200.game.physics.components.PhysicsComponent.class);
            if (phys != null && phys.getBody() != null) {
                phys.getBody().setLinearVelocity(vx, vy);
                phys.getBody().setGravityScale(0f);
                phys.getBody().setBullet(true);
            }
        }
    }

    @Override
    public void dispose() {
        for (WarningEntry w : actives) {
            if (w.visual != null) w.visual.dispose();
        }
        actives.clear();
    }
    public void setAttack(boolean attack)
    {
        this.attack = attack;
    }
}