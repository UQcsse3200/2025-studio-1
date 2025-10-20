package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.lighting.*;
import com.csse3200.game.entities.Entity;

public final class LightFactory {
    private LightFactory() {}

    public static Entity createPointLightEntity(int rays, Color color, float distance, boolean xray, Vector2 offset) {
        return new Entity().addComponent(new PointLightComponent(rays, color, distance, xray, offset));
    }

    public static Entity createDirectionalLightEntity(int rays, Color color, float directionDeg, boolean xray) {
        return new Entity().addComponent(new DirectionalLightComponent(rays, color, directionDeg, xray));
    }

    public static Entity createChainLightEntity(int rays, Color color, float distance, float[] chainXY, boolean xray) {
        return new Entity().addComponent(new ChainLightComponent(rays, color, distance, xray, chainXY));
    }

    public static Entity createConeLightEntity(int rays, Color color, float distance, float coneDegree, boolean xray, Vector2 offset) {
        return new Entity().addComponent(new ConeLightComponent(rays, color, distance, coneDegree, xray, offset));
    }
}