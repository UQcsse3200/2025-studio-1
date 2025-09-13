package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents an area in the game, such as a level, indoor area, etc. An area has a terrain and
 * other entities to spawn on that terrain.
 *
 * <p>Support for enabling/disabling game areas could be added by making this a Component instead.
 */
public abstract class GameArea implements Disposable {
  protected TerrainComponent terrain;
  protected List<Entity> areaEntities;
  /** Prevents re-entrant room transitions across areas */
  protected static boolean isTransitioning = false;

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

  /** Attempt to start a room transition. Returns false if one is already in progress. */
  protected boolean beginTransition() {
    if (isTransitioning || com.csse3200.game.services.ServiceLocator.isTransitioning()) return false;
    isTransitioning = true;
    com.csse3200.game.services.ServiceLocator.setTransitioning(true);
    return true;
  }

  /** Mark the end of a room transition. */
  protected void endTransition() {
    isTransitioning = false;
    com.csse3200.game.services.ServiceLocator.setTransitioning(false);
  }

  /**
   * Spawn entity at its current position
   *
   * @param entity Entity (not yet registered)
   */
  public void spawnEntity(Entity entity) {
    areaEntities.add(entity);
    if (com.csse3200.game.services.ServiceLocator.isTransitioning()) {
      Gdx.app.postRunnable(() -> ServiceLocator.getEntityService().register(entity));
    } else {
      ServiceLocator.getEntityService().register(entity);
    }
  }

  protected void spawnFloor() {
    for (int i = 0; i < 25; i += 4) {
      GridPoint2 floorspawn = new GridPoint2(i, 6);

      Entity floor = ObstacleFactory.createLongFloor();
      spawnEntityAt(floor, floorspawn, false, false);
    }
  }

  /**
   * Remove an entity
   * @param entity to be removed
   */
  public void removeEntity(Entity entity) {
    entity.setEnabled(false);
    areaEntities.remove(entity);
    Gdx.app.postRunnable(entity::dispose);
  }

  /**
   * Gets all the current entities
   * @return the entities on the map
   */
  public List<Entity> getEntities() {
    return this.areaEntities;
  }

  /**
   * Spawn an entity inside the specified room. Requires the terrain to be set first.
   *
   * @param entity   entity to spawn (not yet registered)
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

  /** Convenience to load textures if not already loaded. */
  protected void ensureTextures(String[] texturePaths) {
    com.csse3200.game.services.ResourceService rs = ServiceLocator.getResourceService();
    java.util.List<String> toLoad = new java.util.ArrayList<>();
    for (String path : texturePaths) {
      if (!rs.containsAsset(path, com.badlogic.gdx.graphics.Texture.class)) {
        toLoad.add(path);
      }
    }
    if (!toLoad.isEmpty()) {
      rs.loadTextures(toLoad.toArray(new String[0]));
      rs.loadAll();
    }
  }

  /** Ensure the common player atlas is available. */
  protected void ensurePlayerAtlas() {
    com.csse3200.game.services.ResourceService rs = ServiceLocator.getResourceService();
    if (!rs.containsAsset("images/player.atlas", com.badlogic.gdx.graphics.g2d.TextureAtlas.class)) {
      rs.loadTextureAtlases(new String[] {"images/player.atlas"});
      rs.loadAll();
    }
  }

  /** Unload a set of assets if loaded. */
  protected void unloadAssets(String[] assetPaths) {
    com.csse3200.game.services.ResourceService rs = ServiceLocator.getResourceService();
    rs.unloadAssets(assetPaths);
  }

  /** Create terrain of a given type and add an optional color overlay. */
  protected void setupTerrainWithOverlay(TerrainFactory factory,
                                         TerrainFactory.TerrainType type,
                                         Color overlayColor) {
    terrain = factory.createTerrain(type);
    spawnEntity(new Entity().addComponent(terrain));
    if (overlayColor != null) {
      Entity overlay = new Entity();
      overlay.setScale(1000f, 1000f);
      overlay.setPosition(-500f, -500f);
      overlay.addComponent(new com.csse3200.game.rendering.SolidColorRenderComponent(overlayColor));
      spawnEntity(overlay);
    }
  }

  /** Camera bounds helper. */
  protected static class Bounds {
    public final float leftX, rightX, bottomY, topY, viewWidth, viewHeight;
    public final Vector2 camPos;
    public Bounds(float leftX, float rightX, float bottomY, float topY,
                  float viewWidth, float viewHeight, Vector2 camPos) {
      this.leftX = leftX; this.rightX = rightX; this.bottomY = bottomY; this.topY = topY;
      this.viewWidth = viewWidth; this.viewHeight = viewHeight; this.camPos = camPos;
    }
  }

  protected Bounds getCameraBounds(com.csse3200.game.components.CameraComponent cameraComponent) {
    OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
    Vector2 camPos = cameraComponent.getEntity().getPosition();
    float viewWidth = cam.viewportWidth;
    float viewHeight = cam.viewportHeight;
    float leftX = camPos.x - viewWidth / 2f;
    float rightX = camPos.x + viewWidth / 2f;
    float bottomY = camPos.y - viewHeight / 2f;
    float topY = camPos.y + viewHeight / 2f;
    return new Bounds(leftX, rightX, bottomY, topY, viewWidth, viewHeight, camPos);
  }

  /** Solid walls on edges. */
  protected void addSolidWallLeft(Bounds b, float wallWidth) {
    Entity w = ObstacleFactory.createWall(wallWidth, b.viewHeight);
    w.setPosition(b.leftX, b.bottomY);
    spawnEntity(w);
  }
  protected void addSolidWallRight(Bounds b, float wallWidth) {
    Entity w = ObstacleFactory.createWall(wallWidth, b.viewHeight);
    w.setPosition(b.rightX - wallWidth, b.bottomY);
    spawnEntity(w);
  }
  protected void addSolidWallTop(Bounds b, float wallWidth) {
    Entity w = ObstacleFactory.createWall(b.viewWidth, wallWidth);
    w.setPosition(b.leftX, b.topY - wallWidth);
    spawnEntity(w);
  }
  protected void addSolidWallBottom(Bounds b, float wallWidth) {
    Entity w = ObstacleFactory.createWall(b.viewWidth, wallWidth);
    w.setPosition(b.leftX, b.bottomY);
    spawnEntity(w);
  }

  /** Add a vertical door on the left edge, splitting the wall into two segments. */
  protected void addVerticalDoorLeft(Bounds b, float wallWidth, Runnable onEnter) {
    float doorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float doorY = b.camPos.y - doorHeight / 2f;
    float topSegHeight = Math.max(0f, b.topY - (doorY + doorHeight));
    if (topSegHeight > 0f) {
      Entity top = ObstacleFactory.createWall(wallWidth, topSegHeight);
      top.setPosition(b.leftX, doorY + doorHeight);
      spawnEntity(top);
    }
    float bottomSegHeight = Math.max(0f, doorY - b.bottomY);
    if (bottomSegHeight > 0f) {
      Entity bottom = ObstacleFactory.createWall(wallWidth, bottomSegHeight);
      bottom.setPosition(b.leftX, b.bottomY);
      spawnEntity(bottom);
    }
    Entity door = ObstacleFactory.createDoorTrigger(wallWidth, doorHeight);
    door.setPosition(b.leftX + 0.001f, doorY);
    door.addComponent(new com.csse3200.game.components.DoorComponent(onEnter));
    spawnEntity(door);
  }

  /** Add a vertical door on the right edge. */
  protected void addVerticalDoorRight(Bounds b, float wallWidth, Runnable onEnter) {
    float doorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float doorY = b.camPos.y - doorHeight / 2f;
    float topSegHeight = Math.max(0f, b.topY - (doorY + doorHeight));
    if (topSegHeight > 0f) {
      Entity top = ObstacleFactory.createWall(wallWidth, topSegHeight);
      top.setPosition(b.rightX - wallWidth, doorY + doorHeight);
      spawnEntity(top);
    }
    float bottomSegHeight = Math.max(0f, doorY - b.bottomY);
    if (bottomSegHeight > 0f) {
      Entity bottom = ObstacleFactory.createWall(wallWidth, bottomSegHeight);
      bottom.setPosition(b.rightX - wallWidth, b.bottomY);
      spawnEntity(bottom);
    }
    Entity door = ObstacleFactory.createDoorTrigger(wallWidth, doorHeight);
    door.setPosition(b.rightX - wallWidth - 0.001f, doorY);
    door.addComponent(new com.csse3200.game.components.DoorComponent(onEnter));
    spawnEntity(door);
  }

  /** Add a horizontal door on the top edge. */
  protected void addHorizontalDoorTop(Bounds b, float wallWidth, Runnable onEnter) {
    float doorWidth = Math.max(1f, b.viewWidth * 0.2f);
    float doorX = b.camPos.x - doorWidth / 2f;
    float leftSegWidth = Math.max(0f, doorX - b.leftX);
    if (leftSegWidth > 0f) {
      Entity left = ObstacleFactory.createWall(leftSegWidth, wallWidth);
      left.setPosition(b.leftX, b.topY - wallWidth);
      spawnEntity(left);
    }
    float rightStart = doorX + doorWidth;
    float rightSegWidth = Math.max(0f, (b.leftX + b.viewWidth) - rightStart);
    if (rightSegWidth > 0f) {
      Entity right = ObstacleFactory.createWall(rightSegWidth, wallWidth);
      right.setPosition(rightStart, b.topY - wallWidth);
      spawnEntity(right);
    }
    Entity door = ObstacleFactory.createDoorTrigger(doorWidth, wallWidth);
    door.setPosition(doorX, b.topY - wallWidth + 0.001f);
    door.addComponent(new com.csse3200.game.components.DoorComponent(onEnter));
    spawnEntity(door);
  }

  /** Add a horizontal door on the bottom edge. */
  protected void addHorizontalDoorBottom(Bounds b, float wallWidth, Runnable onEnter) {
    float doorWidth = Math.max(1f, b.viewWidth * 0.2f);
    float doorX = b.camPos.x - doorWidth / 2f;
    float leftSegWidth = Math.max(0f, doorX - b.leftX);
    if (leftSegWidth > 0f) {
      Entity left = ObstacleFactory.createWall(leftSegWidth, wallWidth);
      left.setPosition(b.leftX, b.bottomY);
      spawnEntity(left);
    }
    float rightStart = doorX + doorWidth;
    float rightSegWidth = Math.max(0f, (b.leftX + b.viewWidth) - rightStart);
    if (rightSegWidth > 0f) {
      Entity right = ObstacleFactory.createWall(rightSegWidth, wallWidth);
      right.setPosition(rightStart, b.bottomY);
      spawnEntity(right);
    }
    Entity door = ObstacleFactory.createDoorTrigger(doorWidth, wallWidth);
    door.setPosition(doorX, b.bottomY + 0.001f);
    door.addComponent(new com.csse3200.game.components.DoorComponent(onEnter));
    spawnEntity(door);
  }

  /** Helper to clear current entities and transition to a new area. */
  protected void clearAndLoad(Supplier<GameArea> nextAreaSupplier) {
    if (!beginTransition()) return;
    // Ensure transition happens on the render thread to avoid race conditions
    Gdx.app.postRunnable(() -> {
      // Phase 1: disable and dispose current area's entities
      for (Entity entity : areaEntities) {
        entity.setEnabled(false);
        entity.dispose();
      }
      areaEntities.clear();

      // Phase 2: on the next frame, build the next area to avoid Box2D world-locked/native races
      Gdx.app.postRunnable(() -> {
        try {
          GameArea next = nextAreaSupplier.get();
          com.csse3200.game.services.ServiceLocator.registerGameArea(next);
          next.create();
        } finally {
          endTransition();
        }
      });
    });
  }
}
