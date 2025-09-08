package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Circular mud spray (fires `count` projectiles evenly around a circle).
 * Each projectile: Sensor collision + damage + limited lifetime.
 */
public class EnemyMudRingSprayComponent extends Component {
    private final float cooldown;
    private final int count;
    private final float speed;
    private final float life;

    private float timer = 0f;
    private float angleOffset = 0f; // Adds rotation to each ring for a better visual effect

    public EnemyMudRingSprayComponent(float cooldown, int count, float speed, float lifeSeconds) {
        this.cooldown = cooldown;
        this.count = Math.max(1, count);
        this.speed = speed;
        this.life = lifeSeconds;
    }

    @Override
    public void update() {
        if (!ServiceLocator.getTimeSource().isPaused()) {
            float dt = ServiceLocator.getTimeSource().getDeltaTime();
            timer -= dt;
            if (timer > 0f || entity == null) return;

            spawnRing();
            timer = cooldown;
            angleOffset += Math.toRadians(7f); // Rotate the next ring by 7 degrees
        }
    }

    private void spawnRing() {
        Vector2 center = entity.getCenterPosition();

        int dmg = 10;
        CombatStatsComponent bossStats = entity.getComponent(CombatStatsComponent.class);
        if (bossStats != null) dmg = bossStats.getBaseAttack();

        float step = (float)(Math.PI * 2 / count);
        for (int i = 0; i < count; i++) {
            float ang = angleOffset + step * i;
            Vector2 vel = new Vector2((float)Math.cos(ang), (float)Math.sin(ang)).scl(speed);
            spawnOne(center, vel, dmg);
        }
    }

    private void spawnOne(Vector2 start, Vector2 velocity, int dmg) {
        Entity proj = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TextureRenderComponent("images/mud.png"))
                .addComponent(new EnemyProjectileMovementComponent(velocity, life))
                .addComponent(new EnemyProjectileDamageComponent(dmg));

        proj.setPosition(start);
        proj.getComponent(TextureRenderComponent.class).scaleEntity();

        com.badlogic.gdx.Gdx.app.postRunnable(() ->
                ServiceLocator.getEntityService().register(proj)
        );
    }
}