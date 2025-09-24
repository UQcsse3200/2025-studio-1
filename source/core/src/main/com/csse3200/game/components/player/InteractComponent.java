package com.csse3200.game.components.player;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.HitboxComponent;

import java.util.ArrayList;

/**
 * Handles "interact" event triggered by pressing E as seen in KeyboardPlayerInputComponent
 */
public class InteractComponent extends HitboxComponent{
    private ArrayList<Entity> collidedEntities = new ArrayList<>();

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::entityCollide);
        entity.getEvents().addListener("collisionEnd", this::entitySeparate);
        entity.getEvents().addListener("interact", this::attemptInteract);

        super.create();
    }

    private void entityCollide(Fixture me, Fixture other) {
        System.out.println("entity collided");
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;

        Entity otherEntity = userData.entity;
        if (otherEntity.isInteractable()) {
            collidedEntities.add(otherEntity);
            System.out.println(collidedEntities);
        }
    }

    private void entitySeparate(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;

        Entity otherEntity = userData.entity;
        if (otherEntity.isInteractable()) {
            collidedEntities.remove(otherEntity);
        }
    }

    private void attemptInteract() {
        Entity closest = null;
        float closestDistance = Float.MAX_VALUE;
        for (Entity e : collidedEntities) {
            float distanceFromPlayer = entity.getCenterPosition().dst(e.getCenterPosition());

            if (closest == null) {
                closest = e;
                closestDistance = distanceFromPlayer;
            } else {
                if (distanceFromPlayer < closestDistance) {
                    closest = e;
                    closestDistance = distanceFromPlayer;
                }
            }
        }

        if (closest != null) {
            closest.getEvents().trigger("interact");
        }
    }
}
