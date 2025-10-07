package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.math.Vector2;

public class HoverBobComponent extends Component {
    private float amplitude = 0.1f;
    private float speed = 2.2f;
    private float t = 0f;
    private float anchorY;

    public HoverBobComponent() {}
    public HoverBobComponent(float amplitude, float speed) {
        this.amplitude = amplitude; this.speed = speed;
    }

    @Override
    public void create() { anchorY = entity.getPosition().y; }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        t += dt;
        // recentre bob if something else moved the entity
        float curY = entity.getPosition().y;
        anchorY = anchorY + (curY - anchorY) * Math.min(1f, dt * 5f);
        float yOff = (float)Math.sin(t * speed) * amplitude;
        entity.setPosition(entity.getPosition().x, anchorY + yOff); // don’t touch X
    }
}
