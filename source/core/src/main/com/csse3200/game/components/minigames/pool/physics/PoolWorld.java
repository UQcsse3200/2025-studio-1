package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.physics.box2d.World;

/**
 * Wrapper around a Box2D {@link World} used by the pool minigame.
 * <p>
 * Provides a simplified interface for interacting with the physics world,
 * enforcing configuration suitable for a top-down pool simulation.
 */
public class PoolWorld {
    private final World world;

    /**
     * Creates a new {@code PoolWorld} with a provided Box2D world.
     * <p>
     * Sets the global velocity threshold to zero for precise ball–ball
     * and ball–rail collisions.
     *
     * @param world the Box2D {@link World} instance to wrap
     */
    public PoolWorld(World world) {
        this.world = world;
        World.setVelocityThreshold(0f);
        world.setContinuousPhysics(true);
    }

    /**
     * Returns the underlying Box2D {@link World}.
     * <p>
     * Use this when direct physics operations are required, such as creating bodies.
     *
     * @return the raw {@link World} object
     */
    public World raw() {
        return world;
    }

    /**
     * Checks if the physics world is currently locked.
     * <p>
     * A locked world indicates that a physics step or collision callback
     * is in progress, and bodies cannot be safely modified.
     *
     * @return {@code true} if the world is locked, otherwise {@code false}
     */
    public boolean isLocked() {
        return world.isLocked();
    }
}