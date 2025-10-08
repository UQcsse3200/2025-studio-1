package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes an NPC lead the player along a series of waypoints at a set speed.
 * Call begin() to start the leading behaviour.
 */
public class NpcLeadComponent extends Component {
    private final List<Vector2> waypoints;
    private final float speed;
    private final float arriveRadius;
    private int idx = 0;
    private boolean active = false;

    public NpcLeadComponent(List<Vector2> waypoints, float speed, float arriveRadius) {
        this.waypoints = new ArrayList<>(waypoints);
        this.speed = speed;
        this.arriveRadius = arriveRadius;
    }

    public void begin() {
        if (waypoints.isEmpty()) return;
        active = true;
        entity.getEvents().trigger("dialogue:line", "Stay close, I’ll guide you.");
    }

    @Override
    public void update() {
        if (!active || idx >= waypoints.size()) return;

        Vector2 pos = entity.getCenterPosition();
        Vector2 target = waypoints.get(idx);
        float dist = pos.dst(target);
        if (dist <= arriveRadius) {
            idx++;
            if (idx >= waypoints.size()) {
                active = false;
                entity.getEvents().trigger("dialogue:line", "We’re here.");
                return;
            }
            return;
        }

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        Vector2 dir = target.cpy().sub(pos).nor();
        entity.setPosition(entity.getPosition().add(dir.scl(speed * dt)));
    }
}
