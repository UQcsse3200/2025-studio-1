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
 * 追踪型泥球攻击：朝 target 发射一枚泥球
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
        // 以 Boss 的 baseAttack 作为弹丸伤害（没有就给个默认值）
        int dmg = 10;
        CombatStatsComponent bossStats = entity.getComponent(CombatStatsComponent.class);
        if (bossStats != null) dmg = bossStats.getBaseAttack();

        Entity proj = new Entity()
                .addComponent(new PhysicsComponent())
                // 关键：Sensor，触发碰撞但不会被墙体“顶住”
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TextureRenderComponent("images/mud_ball_1.png"))
                .addComponent(new EnemyProjectileMovementComponent(velocity, life))
                .addComponent(new EnemyProjectileDamageComponent(dmg));

        proj.setPosition(start);
        proj.getComponent(TextureRenderComponent.class).scaleEntity();

        // 延迟注册，避免 #iterator() cannot be used nested
        com.badlogic.gdx.Gdx.app.postRunnable(() ->
                ServiceLocator.getEntityService().register(proj)
        );
    }
}