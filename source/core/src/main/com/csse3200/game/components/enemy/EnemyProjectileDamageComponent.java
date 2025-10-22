package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;

/**
 * Enemy projectile damage (with detailed logging):
 * - Handles collisions from (Fixture, Fixture) / (Entity, Entity) / mixed inputs.
 * - Only applies damage to the PLAYER layer.
 * - Logs each decision for debugging when damage does not occur.
 */
public class EnemyProjectileDamageComponent extends Component {
    private final int damage;

    public EnemyProjectileDamageComponent(int damage) {
        this.damage = damage;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    private void onCollisionStart(Object a, Object b) {
        Entity ea = toEntity(a);
        Entity eb = toEntity(b);

        // Log initial collision parameters
        if (ea == null || eb == null) {
            return;
        }

        // Identify "who am I?"
        Entity me;
        if (ea == entity) {
            me = ea;
        } else if (eb  == entity) {
            me = eb;
        } else {
            return;
        }

        Entity other = (me == ea) ? eb : ea;

        // Log both layers
        int otherLayer = layerOf(other);

        // Only affect PLAYER layer
        if (otherLayer != PhysicsLayer.PLAYER) {
            return;
        }

        // Apply damage
        CombatStatsComponent stats = other.getComponent(CombatStatsComponent.class);
        if (stats == null) {
            return;
        }

        stats.addHealth(-damage);
        // Trigger common damage events
        other.getEvents().trigger("hit", damage);
        other.getEvents().trigger("damaged", damage);
        other.getEvents().trigger("healthChange", -damage);

        // Destroy projectile
        Gdx.app.postRunnable(() -> {
            if (entity != null) {
                entity.dispose();
            }
        });
    }

    /**
     * Converts a Fixture or Entity to an Entity; returns Entity directly if already one
     */
    private Entity toEntity(Object obj) {
        if (obj instanceof Entity entity) return entity;
        if (obj instanceof Fixture f) {
            // Check fixture's userData
            Object u1 = f.getUserData();
            if (u1 instanceof Entity entity) {
                return entity;
            }
            // check body userData
            Object u2 = f.getBody().getUserData();
            if (u2 instanceof Entity entity) {
                return entity;
            }
        }
        return null;
    }

    private int layerOf(Entity e) {
        HitboxComponent hb = e.getComponent(HitboxComponent.class);
        return (hb == null) ? -1 : hb.getLayer();
    }
}