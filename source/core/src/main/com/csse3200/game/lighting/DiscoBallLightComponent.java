package com.csse3200.game.lighting;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class DiscoBallLightComponent extends Component {
    private final int rays;
    private final Color c1;
    private final Color c2;
    private final float baseDist;
    private final float pulseDist;
    private final float speed;
    private final boolean xray;
    private final Vector2 offset;

    private PointLight light;
    private float t;

    public DiscoBallLightComponent(
            int rays,
            Vector2 localOffset,
            Color c1, Color c2,
            float baseDist,
            float pulseDist,
            float speed,
            boolean xray
    ) {
        this.rays = rays;
        this.offset = new Vector2(localOffset);
        this.c1 = new Color(c1);
        this.c2 = new Color(c2);
        this.baseDist = baseDist;
        this.pulseDist = pulseDist;
        this.speed = speed;
        this.xray = xray;
    }

    /**
     * Creates the underlying light and positions it at the entity.
     */
    @Override
    public void create() {
        RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();
        light = new PointLight(rh, rays, c1, baseDist, 0f, 0f);
        light.setXray(xray);          // no shadows
        light.setSoftnessLength(1f);

        // initial position
        Vector2 p = entity.getPosition();
        light.setPosition(p.x + offset.x, p.y + offset.y);
    }

    /**
     * Follows the entity and animates colour and distance over time.
     */
    @Override
    public void update() {
        if (light == null) return;

        // follow entity
        Vector2 p = entity.getPosition();
        light.setPosition(p.x + offset.x, p.y + offset.y);

        // simple color + radius animation
        t += ServiceLocator.getTimeSource().getDeltaTime();
        float s = (float)(0.5 + 0.5 * Math.sin(t * speed));   // 0..1
        Color mix = new Color(c1).lerp(c2, s);
        light.setColor(mix);
        light.setDistance(baseDist + pulseDist * (float)(0.5 + 0.5 * Math.sin(t * speed * 1.7)));
    }

    /**
     * Removes the light from the RayHandler and clears references.
     */
    @Override
    public void dispose() {
        if (light != null) {
            light.remove();
            light = null;
        }
    }
}