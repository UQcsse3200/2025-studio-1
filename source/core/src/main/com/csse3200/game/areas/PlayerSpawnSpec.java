package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;

import java.util.Objects;
import java.util.function.Supplier;

/** Specification for spawning a player in a room/area (grid-based). */
public record PlayerSpawnSpec(
        String roomId,
        GridPoint2 cell,          // <- grid cell, not world position
        boolean centerOnTile,
        boolean collidable,
        Supplier<Entity> playerFactory
) {
    public PlayerSpawnSpec {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(cell,   "cell must not be null");
        Objects.requireNonNull(playerFactory, "playerFactory must not be null");
    }

    /** Default: centered, collidable, using PlayerFactory::createPlayer. */
    public static PlayerSpawnSpec of(String roomId, GridPoint2 cell) {
        return new PlayerSpawnSpec(roomId, cell, true, true, PlayerFactory::createPlayer);
    }

    /** Convenience: pass raw ints. */
    public static PlayerSpawnSpec of(String roomId, int cellX, int cellY) {
        return of(roomId, new GridPoint2(cellX, cellY));
    }

    public PlayerSpawnSpec withFactory(Supplier<Entity> factory) {
        return new PlayerSpawnSpec(roomId, cell, centerOnTile, collidable, factory);
    }

    public PlayerSpawnSpec withCenter(boolean center) {
        return new PlayerSpawnSpec(roomId, cell, center, collidable, playerFactory);
    }

    public PlayerSpawnSpec withCollidable(boolean c) {
        return new PlayerSpawnSpec(roomId, cell, centerOnTile, c, playerFactory);
    }

    public PlayerSpawnSpec withCell(GridPoint2 newCell) {
        return new PlayerSpawnSpec(roomId, newCell, centerOnTile, collidable, playerFactory);
    }
}
