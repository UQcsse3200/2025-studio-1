package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BallFactoryTest {
    private final World world = new World(new Vector2(0, 0), true);
    private final PoolWorld poolWorld = new PoolWorld(world);
    private final TableConfig cfg = TableConfig.builder()
            .tableSize(2.24f, 1.12f)
            .railThickness(0.105f, 0.085f)
            .ballRadius(0.0285f)
            .pocketRadiusScale(2.5f)
            .pocketInsetScaleX(1f)
            .pocketInsetScaleY(1f)
            .pocketFunnelScale(1f)
            .build();

    @AfterEach
    void dispose() { world.dispose(); }

    @Test
    void spawnCueAndRack_hasExpectedCountsAndIds() {
        BallFactory f = new BallFactory(poolWorld, cfg);
        f.spawnCue(new Vector2(-cfg.tableW() * 0.30f, 0f));
        f.spawnRackTriangle(new Vector2(cfg.tableW() * 0.25f, 0f));

        assertNotNull(f.getCueBody());
        assertEquals(15, f.getObjectBodies().size());
        assertEquals(16, f.getIdMap().size()); // cue=0 plus 15 objects
        assertTrue(f.getIdMap().containsKey(0));
    }

    @Test
    void resetRack_repositionsObjectsDeterministically() {
        BallFactory f = new BallFactory(poolWorld, cfg);
        f.spawnCue(new Vector2(0, 0));
        f.spawnRackTriangle(new Vector2(0.5f, 0.1f));

        var before = f.getObjectBallPositions();
        f.resetRack(new Vector2(0.5f, 0.1f));
        var after = f.getObjectBallPositions();

        assertEquals(before.size(), after.size());
        // Should be same triangle positions (ignoring tiny spawn jitter that only applies at create time)
        for (int i = 0; i < before.size(); i++) {
            // after reset, they should exactly match reset targets; ensure not wildly off
            assertTrue(before.get(i).dst2(after.get(i)) < 1e-2);
        }
    }
}