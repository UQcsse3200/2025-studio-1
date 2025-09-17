package com.csse3200.game.components.boss;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * let the missle down by the y direction
 */
public class MissleMovementComponent extends Component {
    private final float speed;

    public MissleMovementComponent(float speed) {
        this.speed = speed;
    }
    /**
     * let the missue do the movement by changing the position
     */
    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        float x = entity.getPosition().x;
        float y = entity.getPosition().y - speed * dt;
        entity.setPosition(x, y);
    }
}