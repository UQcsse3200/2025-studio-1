package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an area in the game, such as a level, indoor area, etc. An area has a terrain and
 * other entities to spawn on that terrain.
 *
 * <p>Support for enabling/disabling game areas could be added by making this a Component instead.
 */
public abstract class GameArea implements Disposable {
  protected TerrainComponent terrain;
  protected List<Entity> areaEntities;

  protected GameArea() {
    areaEntities = new ArrayList<>();
  }

  /** Create the game area in the world. */
  public abstract void create();

  /** Dispose of all internal entities in the area */
  public void dispose() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
  }

  /**
   * Spawn entity at its current position
   *
   * @param entity Entity (not yet registered)
   */
  protected void spawnEntity(Entity entity) {
    areaEntities.add(entity);
    ServiceLocator.getEntityService().register(entity);
  }

  protected void spawnFloor() {
    for (int i = 0; i < 25; i += 4) {
      GridPoint2 floorspawn = new GridPoint2(i, 1);

      Entity floor = ObstacleFactory.createLongFloor();
      spawnEntityAt(floor, floorspawn, false, false);
    }
  }

  /**
   * Spawn entity on a given tile. Requires the terrain to be set first.
   *
   * @param entity Entity (not yet registered)
   * @param tilePos tile position to spawn at
   * @param centerX true to center entity X on the tile, false to align the bottom left corner
   * @param centerY true to center entity Y on the tile, false to align the bottom left corner
   */
  public void spawnEntityInRoom(String roomName, Entity entity) {
    Vector2 pos = getRoomSpawnPosition(roomName);
    entity.setPosition(pos);
    spawnEntity(entity);
  }

  protected Vector2 getRoomSpawnPosition(String roomName) {
    switch (roomName) {
      case "Floor1": return randomInBounds(2f, 8f, 2f, 8f);
      case "Floor2": return randomInBounds(4f, 18f, 4f, 18f);
      case "Floor3": return randomInBounds(5f, 20f, 5f, 20f);
      case "Floor4": return randomInBounds(6f, 22f, 6f, 22f);
      case "Floor5": return randomInBounds(7f, 24f, 7f, 24f);
      case "Floor6": return randomInBounds(8f, 26f, 8f, 26f);
      case "Floor7": return randomInBounds(9f, 28f, 9f, 28f);
      default: return new Vector2(0f, 0f);
    }
  }

  private Vector2 randomInBounds(float minX, float maxX, float minY, float maxY) {
    float x = MathUtils.random(minX, maxX);
    float y = MathUtils.random(minY, maxY);
    return new Vector2(x, y);
  }
  protected void spawnEntityAt(
      Entity entity, GridPoint2 tilePos, boolean centerX, boolean centerY) {
    Vector2 worldPos = terrain.tileToWorldPosition(tilePos);
    float tileSize = terrain.getTileSize();

    if (centerX) {
      worldPos.x += (tileSize / 2) - entity.getCenterPosition().x;
    }
    if (centerY) {
      worldPos.y += (tileSize / 2) - entity.getCenterPosition().y;
    }

    entity.setPosition(worldPos);
    spawnEntity(entity);
  }
}
