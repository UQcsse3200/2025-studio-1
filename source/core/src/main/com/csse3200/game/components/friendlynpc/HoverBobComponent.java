package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.math.Vector2;

public class HoverBobComponent extends Component {
    private float amplitude = 0.1f;
    private float speed = 2.2f;
    private float t = 0f;
    private Vector2 basePos;

    public HoverBobComponent() { }
    public HoverBobComponent(float amplitude, float speed) {
        this.amplitude = amplitude;
        this.speed = speed;
    }

    @Override
    public void create() {
        basePos = entity.getPosition().cpy();
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        t += dt;
        float y = (float) Math.sin(t * speed) * amplitude;
        entity.setPosition(basePos.x, basePos.y + y);
    }
}
