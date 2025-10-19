package com.csse3200.game.components.lighting;

import box2dLight.DirectionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class DirectionalLightComponent extends Component {
    private final int rays;
    private final Color color;
    private final float directionDeg;
    private final boolean xray;
    private DirectionalLight light;

    public DirectionalLightComponent(int rays, Color color, float directionDeg, boolean xray) {
        this.rays = rays;
        this.color = color;
        this.directionDeg = directionDeg;
        this.xray = xray;
    }

    @Override
    public void create() {
        RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();
        light = new DirectionalLight(rh, rays, color, directionDeg);
        light.setXray(xray);
    }

    @Override
    public void dispose() {
        if (light != null) {
            light.remove();
            light = null;
        }
    }
}