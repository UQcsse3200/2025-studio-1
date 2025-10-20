package com.csse3200.game.lighting;

import box2dLight.ChainLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class ChainLightComponent extends Component {
    private final int rays;
    private final Color color;
    private final float distance;
    private final boolean xray;
    private final float[] chain; // interleaved x,y
    private ChainLight light;

    public ChainLightComponent(int rays, Color color, float distance, boolean xray, float[] chain) {
        this.rays = rays;
        this.color = color;
        this.distance = distance;
        this.xray = xray;
        this.chain = chain;
    }

    @Override
    public void create() {
        RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();
        light = new ChainLight(rh, rays, color, distance, chain.length / 2, chain);
        light.setXray(xray);
        light.setSoftnessLength(1f);
    }

    @Override
    public void dispose() {
        if (light != null) {
            light.remove();
            light = null;
        }
    }
}