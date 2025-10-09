package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TableBuilderCollisionTest {
    private final World world = new World(new Vector2(0, 0), true);
    private final PoolWorld poolWorld = new PoolWorld(world);
    private final TableConfig cfg = TableConfig.builder()
            .tableSize(2.24f, 1.12f)
            .railThickness(0.105f, 0.085f)
            .ballRadius(0.0285f)
            .pocketRadiusScale(2.5f)
            .pocketInsetScaleX(1f)
            .pocketInsetScaleY(1f)
            .pocketFunnelScale(0.9f)
            .build();

    @AfterEach
    void cleanup() { world.dispose(); }

    @Test
    void ballBouncesOffRails() {
        TableBuilder tb = new TableBuilder(poolWorld, cfg);
        tb.buildRails();

        // Create a ball near the right rail moving right; it should bounce back (negative vx)
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(cfg.tableW() / 2f - cfg.railX() - cfg.ballR() * 1.1f, 0f);
        Body ball = world.createBody(bd);

        CircleShape sh = new CircleShape();
        sh.setRadius(cfg.ballR());
        FixtureDef fd = new FixtureDef();
        fd.shape = sh;
        fd.restitution = 0.95f;
        fd.filter.categoryBits = TableBuilder.LAYER_BALL;
        fd.filter.maskBits = (short)(TableBuilder.LAYER_RAIL | TableBuilder.LAYER_BALL | TableBuilder.LAYER_POCKET);
        ball.createFixture(fd);
        sh.dispose();

        ball.setLinearVelocity(new Vector2(2f, 0f));

        // step enough to collide & bounce
        for (int i = 0; i < 120; i++) world.step(1/120f, 6, 2);

        assertTrue(ball.getLinearVelocity().x <= 0f, "Expected bounce back (vx <= 0)");
    }
}