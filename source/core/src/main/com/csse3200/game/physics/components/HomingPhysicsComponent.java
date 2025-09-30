package com.csse3200.game.physics.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class HomingPhysicsComponent extends PhysicsProjectileComponent {
    private Entity target;
    private Camera camera;
    private float turnRate;

    @Override
    public void create() {

        super.create();

        physicsComponent = entity.getComponent(PhysicsComponent.class);

        Body body = physicsComponent.getBody();

        body.setBullet(true);
        body.setGravityScale(0f);
        body.setFixedRotation(true);
        body.setLinearDamping(0f);

        if (initialVelocity != null) {
            body.setLinearVelocity(initialVelocity);
        }

        this.turnRate = 2f;

        //retrieves the camera used for getting the real cursor position
        Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
        for (Entity e : entities) {
            if (e.getComponent(CameraComponent.class) != null) {
                camera = e.getComponent(CameraComponent.class).getCamera();

            }
        }

        //real cursor position
        Vector3 origin = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        //arbitrary large number
        float minProximity = 10000000f;
        for (Entity e : entities) {

            //checks for combat entities, not including the current entity
            if (e.hasComponent(CombatStatsComponent.class) && e != ServiceLocator.getPlayer() &&
                    !e.getComponent(CombatStatsComponent.class).isDead())
             {

                Vector2 position = e.getCenterPosition();
                Vector2 displacement = new Vector2(position.x - origin.x,
                        position.y - origin.y);

                float proximity = displacement.len();
                if (proximity < minProximity) {

                    target = e;
                    minProximity = proximity;
                }
            }
        }
    }

    @Override
    public void update() {

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        Body body = physicsComponent.getBody();


        //checks target entity is alive
        if (target != null && !target.getComponent(CombatStatsComponent.class).isDead()) {

            System.out.println(target.getCenterPosition());
            //finds direction vector to the target
            Vector2 origin = entity.getCenterPosition();
            Vector2 destination = target.getCenterPosition();
            Vector2 desiredDirection = new Vector2(destination.x - origin.x,
                    destination.y - origin.y).nor();


            Vector2 currentVelocity = body.getLinearVelocity().nor();
            Vector2 changedDirection = currentVelocity.lerp(desiredDirection, 2f * dt)
                    .nor();

            float speed = body.getLinearVelocity().len();
            body.setLinearVelocity(changedDirection.scl(speed));
        }

        lived += dt;

        if (lived > lifetime) {
            entity.setToRemove();
            body.setLinearVelocity(new Vector2(0f, 0f));
        }
    }

    /**
     * Sets the turn rate of the projectile
     * @param turnRate the rate at which the projectile turns to the target
     */
    public void setTurnRate(float turnRate) {

        this.turnRate = turnRate;
    }

}

