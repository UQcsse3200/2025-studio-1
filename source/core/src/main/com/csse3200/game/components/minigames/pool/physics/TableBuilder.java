package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for constructing the physical boundaries of the pool table,
 * including rails and pocket sensors.
 * <p>
 * The {@code TableBuilder} defines static Box2D bodies that represent the
 * table walls (rails) and circular sensor regions (pockets). These define
 * the playable area and handle pocket detection via the {@link PocketContactSystem}.
 */
public class TableBuilder {
    public static final short LAYER_RAIL = 0x0002;
    public static final short LAYER_POCKET = 0x0004;
    public static final short LAYER_BALL = com.csse3200.game.physics.PhysicsLayer.NPC;
    public static final short MASK_RAIL = LAYER_BALL;
    public static final short MASK_POCKET = LAYER_BALL;

    private static final Logger log = LoggerFactory.getLogger(TableBuilder.class);

    private final PoolWorld world;
    private final TableConfig config;
    private final Body[] pocketSensors = new Body[6];
    private Body railsBody;

    /**
     * Constructs a {@code TableBuilder}.
     *
     * @param world  the {@link PoolWorld} used to create physics bodies
     * @param config the {@link TableConfig} providing table dimensions and pocket data
     */
    public TableBuilder(PoolWorld world, TableConfig config) {
        this.world = world;
        this.config = config;
    }

    /**
     * Checks if the table geometry has already been built.
     *
     * @return {@code true} if the rails exist; {@code false} otherwise
     */
    public boolean isBuilt() {
        return railsBody != null;
    }

    /**
     * Builds the physical rails (walls) of the pool table.
     * <p>
     * Creates a static {@link Body} with a closed {@link ChainShape}
     * that defines the playable boundary of the table. Balls bounce
     * off this loop with minimal friction and high restitution.
     * <p>
     * This method has no effect if rails have already been built.
     */
    public void buildRails() {
        if (railsBody != null) return;

        // Define the inner play area bounds (inset from outer table size)
        float halfW = config.tableW() / 2f, halfH = config.tableH() / 2f;
        float insetX = config.railX(), insetY = config.railY();

        Vector2[] verts = new Vector2[]{
                new Vector2(-halfW + insetX, -halfH + insetY),
                new Vector2(halfW - insetX, -halfH + insetY),
                new Vector2(halfW - insetX, halfH - insetY),
                new Vector2(-halfW + insetX, halfH - insetY)
        };

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        railsBody = world.raw().createBody(bd);

        ChainShape loop = new ChainShape();
        loop.createLoop(verts);

        try {
            FixtureDef fd = new FixtureDef();
            fd.shape = loop;
            fd.friction = 0.05f;
            fd.restitution = 0.98f;

            // Configure collision filters so only balls collide with rails
            Filter filter = new Filter();
            filter.categoryBits = LAYER_RAIL;
            filter.maskBits = MASK_RAIL;

            Fixture fx = railsBody.createFixture(fd);
            fx.setFilterData(filter);
        } finally {
            loop.dispose();
        }
    }

    /**
     * Builds circular pocket sensors around the edges of the table.
     * <p>
     * Each pocket is represented by a static {@link Body} with a circular
     * sensor fixture. These fixtures detect ball collisions without
     * applying any physical response, and are used by
     * {@link PocketContactSystem} to trigger pot events.
     */
    public void buildPocketSensors() {
        float hx = config.tableW() / 2f, hy = config.tableH() / 2f;
        float ix = hx - config.pocketInsetX(), iy = hy - config.pocketInsetY();
        float f = config.pocketFunnel();

        // Pocket centers: top-left, top-center, top-right, bottom-right, bottom-center, bottom-left
        Vector2[] centers = new Vector2[]{
                new Vector2(-ix + f, iy - f),
                new Vector2(0f, iy - f),
                new Vector2(ix - f, iy - f),
                new Vector2(ix - f, -iy + f),
                new Vector2(0f, -iy + f),
                new Vector2(-ix + f, -iy + f)
        };

        // Create six static circular sensor bodies
        for (int i = 0; i < 6; i++) {
            BodyDef bd = new BodyDef();
            bd.type = BodyDef.BodyType.StaticBody;
            bd.position.set(centers[i]);

            Body body = world.raw().createBody(bd);

            CircleShape circle = new CircleShape();
            circle.setRadius(config.pocketR());

            FixtureDef fd = new FixtureDef();
            fd.shape = circle;
            fd.isSensor = true;
            fd.filter.categoryBits = LAYER_POCKET;
            fd.filter.maskBits = MASK_POCKET;

            body.createFixture(fd).setUserData(i);
            circle.dispose();

            pocketSensors[i] = body;
        }
    }
}