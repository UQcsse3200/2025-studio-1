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

        this.turnRate = 20f;

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

            //finds direction vector to the target
            Vector2 origin = entity.getCenterPosition();
            Vector2 destination = target.getCenterPosition();
            Vector2 desiredDirection = new Vector2(destination.x - origin.x,
                    destination.y - origin.y).nor();

            Vector2 currentDirection = body.getLinearVelocity().nor();
            Vector2 newDirection = currentDirection.lerp(desiredDirection,
                    turnRate * dt).nor();

            float speed = initialVelocity.len();
            body.setLinearVelocity(newDirection.scl(speed));

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

    /**
     * gets the rate at which the projectile turns
     * @return turn rate
     */
    public float getTurnRate() {
        return turnRate;
    }

    /**
     * sets the target entity, used for testing
     * @param target entity to be targeted
     */
    public void setTargetEntity(Entity target) {
        this.target = target;
    }

    /**
     * Gets the targeted entity
     * @return the target entity for the projectile
     */
    public Entity getTargetEntity() {

        return this.target;
    }

}

