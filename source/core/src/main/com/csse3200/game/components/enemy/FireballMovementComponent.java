package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class FireballMovementComponent extends Component {
    private final Vector2 velocity;

    public FireballMovementComponent(Vector2 velocity) {
        this.velocity = new Vector2(velocity);
    }

    @Override
    public void update() {
        float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();
        Vector2 pos = entity.getPosition();
        pos.add(new Vector2(velocity).scl(deltaTime));
        entity.setPosition(pos);
    }
}