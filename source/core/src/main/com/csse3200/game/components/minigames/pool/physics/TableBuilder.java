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
    private Entity railsEntity;

    public TableBuilder(PoolWorld world, TableConfig cfg) {
        this.world = world;
        this.cfg = cfg;
    }

    public boolean isBuilt() {
        return railsEntity != null;
    }

    public void buildRails() {
        PhysicsComponent phys = new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody);

        float halfW = cfg.tableW() / 2f, halfH = cfg.tableH() / 2f;
        float insetX = cfg.railX(), insetY = cfg.railY();
        Vector2[] verts = new Vector2[]{
                new Vector2(-halfW + insetX, -halfH + insetY),
                new Vector2(halfW - insetX, -halfH + insetY),
                new Vector2(halfW - insetX, halfH - insetY),
                new Vector2(-halfW + insetX, halfH - insetY)
        };
        ChainShape loop = new ChainShape();
        loop.createLoop(verts);

        ColliderComponent col = new ColliderComponent()
                .setShape(loop)
                .setFriction(0.05f)
                .setRestitution(0.98f)
                .setFilter(LAYER_RAIL, MASK_RAIL);

        railsEntity = new Entity().addComponent(phys).addComponent(col);
        railsEntity.create();
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

