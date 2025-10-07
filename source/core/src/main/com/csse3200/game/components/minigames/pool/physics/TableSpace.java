package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public final class TableSpace {
    private TableSpace() {
    }

    public static Vector2 toNorm(Vector2 wp, TableConfig cfg) {
        float nx = (wp.x + cfg.tableW() / 2f) / cfg.tableW();
        float ny = (wp.y + cfg.tableH() / 2f) / cfg.tableH();
        return new Vector2(MathUtils.clamp(nx, 0f, 1f), MathUtils.clamp(ny, 0f, 1f));
    }

    public static List<Vector2> toNorm(List<Vector2> wps, TableConfig cfg) {
        List<Vector2> out = new ArrayList<>(wps.size());
        for (Vector2 v : wps) out.add(toNorm(v, cfg));
        return out;
    }

    public static Vector2 fromNorm(float nx, float ny, TableConfig cfg) {
        float x = MathUtils.clamp(nx, 0f, 1f) * cfg.tableW() - cfg.tableW() / 2f;
        float y = MathUtils.clamp(ny, 0f, 1f) * cfg.tableH() - cfg.tableH() / 2f;
        return new Vector2(x, y);
    }

    public static Vector2 fromNorm(Vector2 n, TableConfig cfg) {
        return fromNorm(n.x, n.y, cfg);
    }
}
