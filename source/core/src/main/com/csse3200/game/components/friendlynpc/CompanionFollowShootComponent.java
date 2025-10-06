package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;

public class CompanionFollowShootComponent extends Component {
    private float cooldown = 0.25f;
    private float cd = 0f;

    private Entity boundPlayer;

    @Override
    public void create() {
        tryBindToCurrentPlayer();
    }

    @Override
    public void update() {
        cd -= ServiceLocator.getTimeSource().getDeltaTime();
        if (ServiceLocator.getPlayer() != boundPlayer) {
            tryBindToCurrentPlayer();
        }
    }

    private void tryBindToCurrentPlayer() {
        Entity p = ServiceLocator.getPlayer();
        if (p == null || p == boundPlayer) return;

        p.getEvents().addListener(
                "player_shoot_order",
                (Vector2 world, Vector2 dir) -> onShootOrder(world, dir)
        );
        boundPlayer = p;
    }

    private void onShootOrder(Vector2 world, Vector2 playerDir) {
        if (cd > 0f) return;

        PlayerActions pa = (boundPlayer != null) ? boundPlayer.getComponent(PlayerActions.class) : null;
        WeaponsStatsComponent stats = (pa != null) ? pa.getCurrentWeaponStats() : null;
        if (stats == null) return;

        Entity bullet = ProjectileFactory.createPistolBullet(stats);

        Vector2 from = entity.getCenterPosition();
        bullet.setPosition(from.x - bullet.getScale().x / 2f, from.y - bullet.getScale().y / 2f);

        var area = ServiceLocator.getGameArea();
        if (area != null) {
            area.spawnEntity(bullet);
        } else {
            ServiceLocator.getEntityService().register(bullet);
        }

        Vector2 dirToSameWorld = world.cpy().sub(from);
        if (dirToSameWorld.len2() == 0f) {
            dirToSameWorld.set(playerDir);
        }

        PhysicsProjectileComponent phy = bullet.getComponent(PhysicsProjectileComponent.class);
        phy.fire(dirToSameWorld, 5f);

        cd = cooldown;
        entity.getEvents().trigger("fired");
    }

    public CompanionFollowShootComponent cooldown(float seconds) {
        this.cooldown = seconds;
        return this;
    }
}