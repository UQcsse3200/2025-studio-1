package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TableSpaceTest {
    private TableConfig cfg = TableConfig.builder()
            .tableSize(2.24f, 1.12f)
            .railThickness(0.105f, 0.085f)
            .ballRadius(0.0285f)
            .pocketRadiusScale(2.5f)
            .pocketInsetScaleX(1f)
            .pocketInsetScaleY(1f)
            .pocketFunnelScale(1f)
            .build();

    @Test
    void toNorm_centerIsHalfHalf() {
        Vector2 centerWorld = new Vector2(0f, 0f);
        Vector2 n = TableSpace.toNorm(centerWorld, cfg);
        assertEquals(0.5f, n.x, 1e-6);
        assertEquals(0.5f, n.y, 1e-6);
    }

    @Test
    void fromNorm_inverseOfToNorm() {
        Vector2 world = new Vector2(0.3f, -0.2f);
        Vector2 norm = TableSpace.toNorm(world, cfg);
        Vector2 world2 = TableSpace.fromNorm(norm, cfg);
        assertEquals(world.x, world2.x, 1e-5);
        assertEquals(world.y, world2.y, 1e-5);
    }

    @Test
    void clampsToBounds() {
        Vector2 tooBig = TableSpace.fromNorm(2f, -1f, cfg);
        Vector2 back = TableSpace.toNorm(tooBig, cfg);
        assertTrue(back.x <= 1f && back.x >= 0f);
        assertTrue(back.y <= 1f && back.y >= 0f);
    }
}