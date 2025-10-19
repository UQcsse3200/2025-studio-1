package com.csse3200.game.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * When this entity touches a valid enemy's hitbox, deal damage to them and apply a knockback.
 *
 * <p>Requires CombatStatsComponent, HitboxComponent on this entity.
 *
 * <p>Damage is only applied if target entity has a CombatStatsComponent. Knockback is only applied
 * if target entity has a PhysicsComponent.
 */
public class TouchAttackComponent extends Component {
    private final short targetLayer;
    private float knockbackForce = 0f;
    private HitboxComponent hitboxComponent;

    /**
     * Create a component which attacks entities on collision, without knockback.
     *
     * @param targetLayer The physics layer of the target's collider.
     */
    public TouchAttackComponent(short targetLayer) {
        this.targetLayer = targetLayer;
    }

    /**
     * Create a component which attacks entities on collision, with knockback.
     *
     * @param targetLayer The physics layer of the target's collider.
     * @param knockback   The magnitude of the knockback applied to the entity.
     */
    public TouchAttackComponent(short targetLayer, float knockback) {
        this.targetLayer = targetLayer;
        this.knockbackForce = knockback;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        hitboxComponent = entity.getComponent(HitboxComponent.class);
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        if (hitboxComponent.getFixture() != me) {
            // Not triggered by hitbox, ignore
            return;
        }

        if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) {
            // Doesn't match our target layer, ignore
            return;
        }

        // Try to attack target.
        if (!PhysicsLayer.contains(PhysicsLayer.OBSTACLE, other.getFilterData().categoryBits)) {
            Entity target = ((BodyUserData) other.getBody().getUserData()).entity;
            Entity attacker = ((BodyUserData) me.getBody().getUserData()).entity;

            CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
            WeaponsStatsComponent attackerWeapon = attacker.getComponent(WeaponsStatsComponent.class);

            if (targetStats != null && attackerWeapon != null) {
                targetStats.takeDamage(attackerWeapon.getBaseAttack());
            }

            // Apply knockback (if knockback resistance is not 100%)
            PhysicsComponent physicsComponent = target.getComponent(PhysicsComponent.class);
            if (targetStats != null && targetStats.getKnockbackResistance() != 1f
                    && physicsComponent != null && knockbackForce > 0f) {
                Body targetBody = physicsComponent.getBody();
                Vector2 direction = target.getCenterPosition().sub(entity.getCenterPosition());
                Vector2 impulse = direction.setLength(knockbackForce * (1 - targetStats.getKnockbackResistance()));
                targetBody.applyLinearImpulse(impulse, targetBody.getWorldCenter(), true);
            }
        }

        //disposes entity if it is a projectile
        if (entity.hasComponent(PhysicsProjectileComponent.class)) {
            //Do explosion if it is a rocket only
            if (entity.getComponent(WeaponsStatsComponent.class).getRocket()) {
                spawnExplosion(entity.getPosition());
            }
            entity.setToRemove();
        }
    }


    private void spawnExplosion(Vector2 position) {
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/rocketExplosion.atlas", TextureAtlas.class);

        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
        animator.addAnimation("rocketExplosion", 0.05f, Animation.PlayMode.NORMAL);

        // Create the explosion entity first
        Entity explosion = new Entity();
        explosion.addComponent(animator);

        // Add a self-removing component
        explosion.addComponent(new Component() {
            private final int frameCount = atlas.findRegions("rocketExplosion").size;
            private final float frameDuration = 0.05f;
            private final float animationDuration = frameCount * frameDuration;
            private float elapsedTime = 0f;

            @Override
            public void update() {
                elapsedTime += ServiceLocator.getTimeSource().getDeltaTime();
                if (elapsedTime >= animationDuration) {
                    explosion.setToRemove();
                }
            }
        });

        explosion.setScale(2f, 2f);
        explosion.setPosition(position);

        ServiceLocator.getEntityService().register(explosion);

        animator.startAnimation("rocketExplosion");
    }


}