package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.projectiles.ProjectileTarget;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;

public class RangedUseComponent extends ItemActionsComponent {

    public void use(Entity player) {
        Camera cam = null;
        for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
            if (entity.hasComponent(CameraComponent.class)) {
                cam = entity.getComponent(CameraComponent.class).getCamera();
            }
        }
        WeaponsStatsComponent weaponsStats = entity.getComponent(WeaponsStatsComponent.class);
        String texturePath = weaponsStats.getProjectileTexturePath();

        Entity bullet = ProjectileFactory.createProjectile(ProjectileTarget.ENEMY, weaponsStats, texturePath);
        PhysicsProjectileComponent projectilePhysics = bullet.getComponent(PhysicsProjectileComponent.class);
        Vector2 origin = new Vector2(player.getPosition());

        bullet.setPosition(origin);

        Vector2 center = bullet.getCenterPosition();
        ServiceLocator.getEntityService().register(bullet);

        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector3 destination = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        projectilePhysics.fire(new Vector2(destination.x - center.x, destination.y - center.y), 5);
    }
}
