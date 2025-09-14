package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.TouchAttackComponent;

/**
 * Tech projectile attack for Boss3.
 * Spawns an animated CPU-shuriken projectile from Boss3_Attacks.atlas.
 */
public class EnemyMudBallAttackComponent extends Component {
    private static final String ATTACKS_ATLAS_PATH = "images/Boss3_Attacks.atlas";
    private static final String DEFAULT_PROJECTILE_ANIM = "boss3_attack_cpu";

    private final Entity target;
    private final float cooldown, range, speed, life;
    private final String projectileAnimName;

    private float timer = 0f;
    private float attackAnimTimer = 0f;

    private TextureAtlas attacksAtlas; // cached atlas

    public EnemyMudBallAttackComponent(Entity target,
                                       float cooldown,
                                       float range,
                                       float speed,
                                       float lifeSeconds) {
        this(target, DEFAULT_PROJECTILE_ANIM, cooldown, range, speed, lifeSeconds);
    }

    public EnemyMudBallAttackComponent(Entity target,
                                       String projectileAnimName,
                                       float cooldown,
                                       float range,
                                       float speed,
                                       float lifeSeconds) {
        this.target = target;
        this.projectileAnimName = projectileAnimName;
        this.cooldown = cooldown;
        this.range = range;
        this.speed = speed;
        this.life = lifeSeconds;
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer -= dt;

        // return boss to idle if attack anim timer runs out
        if (attackAnimTimer > 0f) {
            attackAnimTimer -= dt;
            if (attackAnimTimer <= 0f &&
                    entity != null &&
                    entity.getComponent(AnimationRenderComponent.class) != null) {
                entity.getEvents().trigger("boss3:idle");
            }
        }

        if (timer > 0f || entity == null || target == null) return;

        Vector2 from = entity.getCenterPosition();
        Vector2 to = target.getCenterPosition();
        if (range > 0f && from.dst2(to) > range * range) return;

        Vector2 vel = to.cpy().sub(from).nor().scl(speed);
        spawnProjectile(from, vel);
        timer = cooldown;

        if (entity.getComponent(AnimationRenderComponent.class) != null) {
            entity.getEvents().trigger("boss3:attack_cpu");
            attackAnimTimer = 0.35f;
        }
    }

    private TextureAtlas getOrLoadAttacksAtlas() {
        if (attacksAtlas != null) return attacksAtlas;

        var rs = ServiceLocator.getResourceService();
        try {
            attacksAtlas = rs.getAsset(ATTACKS_ATLAS_PATH, TextureAtlas.class);
            if (attacksAtlas != null) return attacksAtlas;
        } catch (GdxRuntimeException ignored) {}

        // Lazy load
        Gdx.app.log("Boss3", "Lazy-loading " + ATTACKS_ATLAS_PATH);
        rs.loadTextureAtlases(new String[]{ATTACKS_ATLAS_PATH});
        while (rs.loadForMillis(1)) { /* spin until loaded */ }

        try {
            attacksAtlas = rs.getAsset(ATTACKS_ATLAS_PATH, TextureAtlas.class);
        } catch (GdxRuntimeException ex) {
            Gdx.app.error("Boss3", "Failed to load attacks atlas: " + ATTACKS_ATLAS_PATH, ex);
            attacksAtlas = null;
        }
        return attacksAtlas;
    }

    private void spawnProjectile(Vector2 start, Vector2 velocity) {
        int dmg = 10;
        WeaponsStatsComponent bossStats = entity.getComponent(WeaponsStatsComponent.class);
        if (bossStats != null) dmg = bossStats.getBaseAttack();

        TextureAtlas atlas = getOrLoadAttacksAtlas();
        if (atlas == null) {
            timer = Math.max(timer, 0.25f);
            return;
        }
        if (atlas.findRegions(projectileAnimName).size == 0) {
            Gdx.app.error("Boss3", "No regions '" + projectileAnimName + "' in " + ATTACKS_ATLAS_PATH);
            timer = Math.max(timer, 0.25f);
            return;
        }

        Entity proj = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.ENEMY_PROJECTILE))
                .addComponent(new EnemyProjectileMovementComponent(velocity, life))
                .addComponent(new CombatStatsComponent(1))
                .addComponent(new WeaponsStatsComponent(dmg))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 0f));

        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        arc.addAnimation(projectileAnimName, 0.06f, Animation.PlayMode.LOOP);
        proj.addComponent(arc);

        arc.startAnimation(projectileAnimName);

        proj.setScale(0.8f, 0.8f);
        proj.setPosition(start);

        Gdx.app.postRunnable(() -> ServiceLocator.getEntityService().register(proj));
    }
}