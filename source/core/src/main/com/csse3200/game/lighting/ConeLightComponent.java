package com.csse3200.game.lighting;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class ConeLightComponent extends Component {
    private final int rays;
    private final Color color;
    private final float distance;
    private final boolean xray;
    private final Vector2 offset;
    private ConeLight light;

    public ConeLightComponent(int rays, Color color, float distance, float coneDegree, boolean xray, Vector2 offset) {
        this.rays = rays;
        this.color = color;
        this.distance = distance;
        this.xray = xray;
        this.offset = offset != null ? offset : new Vector2();
    }

    @Override
    public void create() {
        RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();
        Vector2 p = entity.getPosition();

        light = new ConeLight(rh, rays, color, distance, 0f, 0f,-90f, 60f);
        light.setXray(xray);
        light.setSoftnessLength(1f);
        light.setPosition(p.x + offset.x,p.y + offset.y);
    }

    @Override
    public void update() {
        if (light == null) return;
        Vector2 p = entity.getPosition();
        light.setPosition(p.x + offset.x, p.y + offset.y);
        light.setDirection(-90f);
    }

    @Override
    public void dispose() {
        if (light != null) {
            light.remove();
            light = null;
        }
    }
}
