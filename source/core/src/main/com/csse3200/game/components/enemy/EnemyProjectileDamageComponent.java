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
    private static final String TAG = "EnemyProjectileDamage";
    private final int damage;

    public EnemyProjectileDamageComponent(int damage) {
        this.damage = damage;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        Gdx.app.log(TAG, "attached to projectile entity id=" + entity.getId());
    }

    private void onCollisionStart(Object a, Object b) {
        Entity ea = toEntity(a);
        Entity eb = toEntity(b);

        // Log initial collision parameters
        Gdx.app.log(TAG, "collisionStart raw: a=" + klass(a) + " b=" + klass(b));
        Gdx.app.log(TAG, "toEntity: ea=" + idOf(ea) + " eb=" + idOf(eb));

        if (ea == null || eb == null) {
            Gdx.app.log(TAG, "skip: one side cannot map to Entity");
            return;
        }

        // Identify "who am I?"
        Entity me = (ea == entity) ? ea : (eb == entity ? eb : null);
        if (me == null) {
            Gdx.app.log(TAG, "skip: neither side is this projectile (event from others)");
            return;
        }
        Entity other = (me == ea) ? eb : ea;

        // Log both layers
        int meLayer = layerOf(me);
        int otherLayer = layerOf(other);
        Gdx.app.log(TAG, "layers: me=" + layerName(meLayer) + " other=" + layerName(otherLayer));

        // Only affect PLAYER layer
        if (otherLayer != PhysicsLayer.PLAYER) {
            Gdx.app.log(TAG, "skip: other is not PLAYER layer");
            return;
        }

        // Apply damage
        CombatStatsComponent stats = other.getComponent(CombatStatsComponent.class);
        if (stats == null) {
            Gdx.app.log(TAG, "skip: PLAYER has no CombatStatsComponent");
            return;
        }

        int before = stats.getHealth();
        stats.addHealth(-damage);
        // Trigger common damage events
        other.getEvents().trigger("hit", damage);
        other.getEvents().trigger("damaged", damage);
        other.getEvents().trigger("healthChange", -damage);

        int after = stats.getHealth();
        Gdx.app.log(TAG, "HIT player for " + damage + "hp  (" + before + " -> " + after + ")");

        // Destroy projectile
        Gdx.app.postRunnable(() -> {
            if (entity != null) {
                Gdx.app.log(TAG, "dispose projectile id=" + entity.getId());
                entity.dispose();
            }
        });
    }

    /**
     * Converts a Fixture or Entity to an Entity; returns Entity directly if already one
     */
    private Entity toEntity(Object obj) {
        if (obj instanceof Entity) return (Entity) obj;
        if (obj instanceof Fixture) {
            Fixture f = (Fixture) obj;
            // Check fixture's userData
            Object u1 = f.getUserData();
            if (u1 instanceof Entity) {
                return (Entity) u1;
            }
            // check body userData
            Object u2 = f.getBody().getUserData();
            if (u2 instanceof Entity) {
                return (Entity) u2;
            }
        }
        return null;
    }

    private String klass(Object o) {
        return (o == null) ? "null" : o.getClass().getSimpleName();
    }

    private String idOf(Entity e) {
        return (e == null) ? "null" : ("Entity#" + e.getId());
    }

    private int layerOf(Entity e) {
        HitboxComponent hb = e.getComponent(HitboxComponent.class);
        return (hb == null) ? -1 : hb.getLayer();
    }

    private String layerName(int layer) {
        switch (layer) {
            case PhysicsLayer.PLAYER: return "PLAYER";
            case PhysicsLayer.NPC: return "NPC";
            case PhysicsLayer.OBSTACLE: return "OBSTACLE";
            case PhysicsLayer.NONE: return "NONE";
            default: return "unknown(" + layer + ")";
        }
    }
}