package com.csse3200.game.components.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.tasks.WaitTask;
import com.csse3200.game.effects.Effect;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.projectiles.ProjectileTarget;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;

public class RangedUseComponent extends ItemActionsComponent {

    public void use(Entity player) {
        boolean isConsumable = entity.hasComponent(ConsumableComponent.class);

        Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
        attackSound.play();

        // Current method of obtaining camera (subject to change/should be improved)
        Camera cam = null;
        for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
            if (entity.hasComponent(CameraComponent.class)) {
                cam = entity.getComponent(CameraComponent.class).getCamera();
            }
        }
        assert cam != null;

        WeaponsStatsComponent weaponsStats = entity.getComponent(WeaponsStatsComponent.class);
        String texturePath = weaponsStats.getProjectileTexturePath();

        Entity bullet;
        if (isConsumable) {
            bullet = ProjectileFactory.createBomb(ProjectileTarget.ENEMY, weaponsStats, texturePath);
        } else {
            bullet = ProjectileFactory.createProjectile(ProjectileTarget.ENEMY, weaponsStats, texturePath);
        }
        PhysicsProjectileComponent projectilePhysics = bullet.getComponent(PhysicsProjectileComponent.class);
        Vector2 origin = new Vector2(player.getPosition());

        bullet.setPosition(origin);

        Vector2 center = bullet.getCenterPosition();
        ServiceLocator.getEntityService().register(bullet);

        Vector3 destination = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));


        if (isConsumable) {
            projectilePhysics.fire(new Vector2(destination.x - center.x, destination.y - center.y), 2);
            bomb_timer(entity.getComponent(ConsumableComponent.class).getDuration(), bullet);
        } else {
            projectilePhysics.fire(new Vector2(destination.x - center.x, destination.y - center.y), 5);
        }
    }

    private void bomb_timer(int duration, Entity bullet) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                bullet.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(new Vector2(0, 0));
            }
        }, duration);

        ConsumableComponent consumable = entity.getComponent(ConsumableComponent.class);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                Entity explosion = ItemFactory.createItem("images/electriczap.png");
                explosion.setPosition(bullet.getPosition());
                explosion.create();

                bullet.dispose();
                for (Effect effect : consumable.getEffects()) {
                    effect.apply(bullet);
                }

                explosion.dispose();
            }
        }, duration * 2);
    }
}
