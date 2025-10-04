package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Auto companion shooter (minimal checks).
 * Assumptions:
 *  - Player always has PlayerActions and a current weapon.
 *  - Fireball bullet from ProjectileFactory always has PhysicsProjectileComponent.
 *  - Bullet size is finalized in the factory.
 */
public class AutoCompanionShootComponent extends Component {
    private static final float cooldown      = 1f;    // shoot cooldown (s)
    private static final float range         = 7.0f;  // seek radius (m)
    private static final float scan_interval = 0.12f; // scan period (s)
    private static final float speed         = 5f;    // bullet speed

    private float cd = 0f;
    private float scanTimer = 0f;
    private Entity boundPlayer;

    @Override
    public void create() {
        boundPlayer = ServiceLocator.getPlayer(); // no cross-map rebind
    }

    @Override
    public void update() {
        if (boundPlayer == null) return;

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        cd -= dt;
        scanTimer -= dt;

        if (scanTimer <= 0f) {
            scanTimer = scan_interval;
            tryAutoShoot();
        }
    }

    private void tryAutoShoot() {
        if (cd > 0f) return;
        Vector2 from = entity.getCenterPosition();
        Entity target = findTarget(from, range);
        if (target == null) return;
        WeaponsStatsComponent stats = boundPlayer
                .getComponent(PlayerActions.class)
                .getCurrentWeaponStats();
        Vector2 dir = target.getCenterPosition().cpy().sub(from);
        Entity bullet = ProjectileFactory.createFireballBullet(stats);
        bullet.setPosition(from.x - bullet.getScale().x / 2f, from.y - bullet.getScale().y / 2f);
        var area = ServiceLocator.getGameArea();
        if (area != null) area.spawnEntity(bullet);
        else ServiceLocator.getEntityService().register(bullet);
        bullet.getComponent(PhysicsProjectileComponent.class).fire(dir, speed);
        cd = cooldown;
        entity.getEvents().trigger("fired");
    }

    // Enemy = NPC layer and (if has HP component) still alive
    private boolean isEnemy(Entity e) {
        if (e == entity || e == boundPlayer) return false;
        HitboxComponent hb = e.getComponent(HitboxComponent.class);
        if (hb == null || hb.getLayer() != PhysicsLayer.NPC) return false;
        CombatStatsComponent cs = e.getComponent(CombatStatsComponent.class);
        return cs == null || cs.getHealth() > 0;
    }

    /** Find nearest enemy within radius r (short name). */
    private Entity findTarget(Vector2 from, float r) {
        Array<Entity> all = ServiceLocator.getEntityService().getEntities();
        float r2 = r * r, best = Float.MAX_VALUE;
        Entity ans = null;
        for (Entity e : all) {
            if (!isEnemy(e)) continue;
            float d2 = from.dst2(e.getCenterPosition());
            if (d2 <= r2 && d2 < best) { best = d2; ans = e; }
        }
        return ans;
    }
}
