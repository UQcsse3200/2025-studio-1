package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for converting between world-space and
 * normalised table-space coordinates.
 * <p>
 * Normalised coordinates are represented in the range {@code [0..1]} along
 * both axes, where (0,0) corresponds to the bottom-left corner of the table
 * and (1,1) corresponds to the top-right corner.
 * <p>
 * World coordinates are centered at (0,0), meaning the center of the table
 * corresponds to (0,0) in world-space, with ±X and ±Y extending to table edges.
 * <p>
 * These conversions are used primarily by {@link com.csse3200.game.components.minigames.pool.PoolGameDisplay}
 * for rendering, and by {@link com.csse3200.game.components.minigames.pool.PoolGame}
 * for mapping between UI and physics data.
 */
public final class TableSpace {

    private TableSpace() {
        // Utility class to prevent instantiation
    }

    /**
     * Converts a world-space position into normalised table-space coordinates.
     * <p>
     * Values are clamped to the range [0..1].
     *
     * @param wp  the world position vector (meters)
     * @param cfg the {@link TableConfig} describing table dimensions
     * @return a new {@link Vector2} in normalised table space
     */
    public static Vector2 toNorm(Vector2 wp, TableConfig cfg) {
        float nx = (wp.x + cfg.tableW() / 2f) / cfg.tableW();
        float ny = (wp.y + cfg.tableH() / 2f) / cfg.tableH();
        return new Vector2(MathUtils.clamp(nx, 0f, 1f), MathUtils.clamp(ny, 0f, 1f));
    }

    /**
     * Converts a list of world-space positions into normalised table-space coordinates.
     * <p>
     * Each entry is clamped to the range [0..1].
     *
     * @param wps a list of world-space positions (meters)
     * @param cfg the {@link TableConfig} describing table dimensions
     * @return a list of normalised coordinate vectors
     */
    public static List<Vector2> toNorm(List<Vector2> wps, TableConfig cfg) {
        List<Vector2> out = new ArrayList<>(wps.size());
        for (Vector2 v : wps) out.add(toNorm(v, cfg));
        return out;
    }

    /**
     * Converts normalised table-space coordinates into world-space.
     * <p>
     * Values are clamped to [0..1] before conversion.
     *
     * @param nx  normalised X coordinate (0..1)
     * @param ny  normalised Y coordinate (0..1)
     * @param cfg the {@link TableConfig} describing table dimensions
     * @return a new {@link Vector2} in world coordinates
     */
    public static Vector2 fromNorm(float nx, float ny, TableConfig cfg) {
        float x = MathUtils.clamp(nx, 0f, 1f) * cfg.tableW() - cfg.tableW() / 2f;
        float y = MathUtils.clamp(ny, 0f, 1f) * cfg.tableH() - cfg.tableH() / 2f;
        return new Vector2(x, y);
    }

    /**
     * Converts a normalised coordinate vector into world-space.
     * <p>
     * Equivalent to {@link #fromNorm(float, float, TableConfig)} using the vector’s components.
     *
     * @param n   normalised position vector (0..1)
     * @param cfg the {@link TableConfig} describing table dimensions
     * @return a new {@link Vector2} in world coordinates
     */
    public static Vector2 fromNorm(Vector2 n, TableConfig cfg) {
        return fromNorm(n.x, n.y, cfg);
    }
}