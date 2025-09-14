package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Tracking mud ball attack: Fires a mud ball towards the target.
 */
public class EnemyMudBallAttackComponent extends Component {
    private final Entity target;
    private final float cooldown;
    private final float range;
    private final float speed;
    private final float life;

    private float timer = 0f;

    public EnemyMudBallAttackComponent(Entity target, float cooldown, float range, float speed, float lifeSeconds) {
        this.target = target;
        this.cooldown = cooldown;
        this.range = range;
        this.speed = speed;
        this.life = lifeSeconds;
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer -= dt;

        if (timer > 0f || entity == null || target == null) return;

        Vector2 from = entity.getCenterPosition();
        Vector2 to = target.getCenterPosition();

        if (from.dst2(to) > range * range) return;

        Vector2 dir = to.cpy().sub(from).nor();
        Vector2 vel = dir.scl(speed);
        spawnProjectile(from, vel);
        timer = cooldown;
    }

    private void spawnProjectile(Vector2 start, Vector2 velocity) {
        // Use the boss's base attack as projectile damage (default to 10 if not available)
        int dmg = 10;
        WeaponsStatsComponent bossStats = entity.getComponent(WeaponsStatsComponent.class);
        if (bossStats != null) dmg = bossStats.getBaseAttack();

        Entity proj = new Entity()
                .addComponent(new PhysicsComponent())
                // Sensor allows it to trigger collisions without being blocked by walls
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TextureRenderComponent("images/mud_ball_1.png"))
                .addComponent(new EnemyProjectileMovementComponent(velocity, life))
                .addComponent(new EnemyProjectileDamageComponent(dmg));

        proj.setPosition(start);
        proj.getComponent(TextureRenderComponent.class).scaleEntity();

        // Delayed registration to avoid concurrent modification (e.g., iterator issues)
        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            com.csse3200.game.areas.GameArea area = ServiceLocator.getGameArea();
            if (area != null) {
                area.spawnEntity(proj);
            } else {
                ServiceLocator.getEntityService().register(proj);
            }
        });
    }
}