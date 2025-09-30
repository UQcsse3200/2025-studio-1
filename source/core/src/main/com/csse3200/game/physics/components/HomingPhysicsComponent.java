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

public class HomingPhysicsComponent extends PhysicsProjectileComponent{
    private PhysicsComponent physicsComponent;
    private Vector2 initialVelocity;
    private final float lifetime = 5f;
    private float lived = 0f;
    private Entity target;
    private Camera camera;

    @Override
    public void create() {

        physicsComponent = entity.getComponent(PhysicsComponent.class);

        Body body = physicsComponent.getBody();

        body.setBullet(true);
        body.setGravityScale(0f);
        body.setFixedRotation(true);
        body.setLinearDamping(0f);

        if (initialVelocity != null) {
            body.setLinearVelocity(initialVelocity);
        }

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
            if (e.hasComponent(CombatStatsComponent.class) && e != entity) {

                Vector2 position = e.getCenterPosition();
                Vector2 displacement = new Vector2(position.x - origin.x,
                        position.y - origin.y);

                //plz double check len works as intended :)
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


    }
}

