package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class PoolWorld {
    private final World world;

    public PoolWorld(World world) {
        this.world = world;
        this.world.setGravity(new Vector2(0, 0));
    }

    public World raw() {
        return world;
    }

    public boolean isLocked() {
        return world.isLocked();
    }
}