package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableBuilder {
    public static final short LAYER_RAIL = 0x0002;
    public static final short LAYER_POCKET = 0x0004;
    public static final short LAYER_BALL = com.csse3200.game.physics.PhysicsLayer.NPC;
    public static final short MASK_RAIL = LAYER_BALL;
    public static final short MASK_POCKET = LAYER_BALL;
    private static final Logger log = LoggerFactory.getLogger(TableBuilder.class);
    private final PoolWorld world;
    private final TableConfig cfg;
    private final Body[] pocketSensors = new Body[6];
    private Body railsBody;


    public TableBuilder(PoolWorld world, TableConfig cfg) {
        this.world = world;
        this.cfg = cfg;
    }

    public boolean isBuilt() {
        return railsBody != null;
    }

    public void buildRails() {
        if (railsBody != null) return;

        // Geometry: inner rectangle where balls can move, inset by rail thickness
        float halfW = cfg.tableW() / 2f, halfH = cfg.tableH() / 2f;
        float insetX = cfg.railX(),     insetY = cfg.railY();

        Vector2[] verts = new Vector2[] {
                new Vector2(-halfW + insetX, -halfH + insetY),
                new Vector2( halfW - insetX, -halfH + insetY),
                new Vector2( halfW - insetX,  halfH - insetY),
                new Vector2(-halfW + insetX,  halfH - insetY)
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

            // Collision filtering: rails collide with balls
            Filter filter = new Filter();
            filter.categoryBits = LAYER_RAIL;
            filter.maskBits = MASK_RAIL;

            Fixture fx = railsBody.createFixture(fd);
            fx.setFilterData(filter);
        } finally {
            loop.dispose();
        }

        log.debug("Rails built in pool world at inset X={}, Y={}", insetX, insetY);
    }


    public void buildPocketSensors() {
        float hx = cfg.tableW() / 2f, hy = cfg.tableH() / 2f;
        float ix = hx - cfg.pocketInsetX(), iy = hy - cfg.pocketInsetY();
        float f = cfg.pocketFunnel();
        Vector2[] centers = new Vector2[] {
                new Vector2(-ix + f,  iy - f), // top-left    (IN from left, IN from top)
                new Vector2( 0f,      iy - f), // top-center  (IN from top)
                new Vector2( ix - f,  iy - f), // top-right   (IN from right, IN from top)
                new Vector2( ix - f, -iy + f), // bottom-right(IN from right, IN from bottom)
                new Vector2( 0f,     -iy + f), // bottom-center(IN from bottom)
                new Vector2(-ix + f, -iy + f)  // bottom-left (IN from left, IN from bottom)
        };

        for (int i = 0; i < 6; i++) {
            BodyDef bd = new BodyDef();
            bd.type = BodyDef.BodyType.StaticBody;
            bd.position.set(centers[i]);
            Body body = world.raw().createBody(bd);
            CircleShape circle = new CircleShape();
            circle.setRadius(cfg.pocketR());
            FixtureDef fd = new FixtureDef();
            fd.shape = circle;
            fd.isSensor = true;
            fd.filter.categoryBits = LAYER_POCKET;
            fd.filter.maskBits = MASK_POCKET;
            body.createFixture(fd).setUserData(i);
            circle.dispose();
            pocketSensors[i] = body;
        }
        log.debug("Created {} pocket sensors.", pocketSensors.length);
    }
}

