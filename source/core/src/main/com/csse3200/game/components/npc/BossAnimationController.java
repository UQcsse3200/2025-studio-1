package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;

public class BossMovementComponent extends Component {
    private PhysicsComponent physics;
    private float speed = 2f;
    private boolean movingRight = true;

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
    }
    @Override
    public void update() {
        if (physics == null) return;

        float vx = movingRight ? speed : -speed;
        physics.getBody().setLinearVelocity(vx, 0f);

        float x = entity.getPosition().x;
        if (x > 10f) {
            movingRight = false;
        } else if (x < 2f) {
            movingRight = true;
        }
    }
}

