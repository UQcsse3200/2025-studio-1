package com.csse3200.game.lighting;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class PointLightFollowComponent extends Component {
    private final int rays; private final Color color; private final float distance; private final Vector2 offset;
    private PointLight light;

    public PointLightFollowComponent(int rays, Color color, float distance, Vector2 offset) {
        this.rays = rays; this.color = color; this.distance = distance; this.offset = offset;
    }

    @Override public void create() {
        RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();
        light = new PointLight(rh, rays, color, distance, 0f, 0f);
        light.setXray(false);               // IMPORTANT: allow shadows
        light.setSoftnessLength(1.2f);
        update();                           // initial position
    }

    @Override public void update() {
        if (light == null) return;
        Vector2 p = entity.getPosition();
        light.setPosition(p.x + offset.x, p.y + offset.y);
    }

    @Override public void dispose() {
        if (light != null) { light.remove(); light = null; }
    }
}