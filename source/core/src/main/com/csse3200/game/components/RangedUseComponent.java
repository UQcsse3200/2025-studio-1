package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.projectiles.ProjectileTarget;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;

public class RangedUseComponent extends ItemActionsComponent {

    public void use(Entity player) {
        WeaponsStatsComponent weaponsStats = entity.getComponent(WeaponsStatsComponent.class);
        String texturePath = weaponsStats.getProjectileTexturePath();

        Entity bullet = ProjectileFactory.createProjectile(ProjectileTarget.ENEMY, weaponsStats, texturePath);
        PhysicsProjectileComponent projectilePhysics = bullet.getComponent(PhysicsProjectileComponent.class);
        Vector2 origin = new Vector2(entity.getPosition());

        bullet.setPosition(origin);
        ServiceLocator.getEntityService().register(bullet);

        Vector3 destination = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        projectilePhysics.fire(new Vector2(destination.x - origin.x, destination.y - origin.y), 5);
    }
}
