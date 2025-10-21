package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.lighting.*;
import com.csse3200.game.entities.Entity;

public final class LightFactory {
    private LightFactory() {}

    public static Entity createConeLightEntity(Color color, boolean xray, Vector2 offset) {
        return new Entity().addComponent(new ConeLightComponent(color, xray, offset));
    }
}