package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.enemy.EnemyWaves;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.RandomUtils;

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

  protected EnemyWaves wavesManager; // manage waves via terminal command

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
  public void spawnEntity(Entity entity) {
    areaEntities.add(entity);
    ServiceLocator.getEntityService().register(entity);
  }

  protected void spawnFloor() {
    for (int i = 0; i < 25; i += 4) {
      GridPoint2 floorspawn = new GridPoint2(i, 4);

      Entity floor = ObstacleFactory.createLongFloor();
      spawnEntityAt(floor, floorspawn, false, false);
    }
  }

  /**
   * Start enemy waves from terminal command by typing "waves".
   */
  public void startWaves(Entity player) {
    if (wavesManager == null || wavesManager.isFinished()) {
      int room = getRoomNumber();
      int maxWaves = room > 4 ? 2 : 1; // mimic original behaviour: higher rooms get 2 waves
      wavesManager = new EnemyWaves(maxWaves, this, player);
      Gdx.app.log("GameArea", "Initializing waves: room=" + room + " maxWaves=" + maxWaves);
    }
    wavesManager.startWave();
  }

  public int getRoomNumber() { // changed from protected to public for EnemyWaves access
    String name = getClass().getSimpleName();
    // Look for digits after Floor or Room (e.g., Floor3GameArea, Room2Area)
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:Floor|Room)(\\d+)").matcher(name);
    if (m.find()) {
      try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
    }
    // Fallback: default first floor
    return 1;
  }

  public float getBaseDifficultyScale() {
    int room = getRoomNumber();
    // +40% per room after first (tweak as needed)
    return 1f + 0.4f * Math.max(0, room - 1);
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

  /**
   * Adds GhostGPT enemies onto the map.
   * @param total The total number of GhostGPT to be spawned.
   * @param scaleFactor The scale of increase in difficulty of the GhostGPT
   */
  public void spawnGhostGPT(int total, float scaleFactor, Entity player) {
    for (int i = 0; i < total; i++) {;
      Entity ghostGPT = NPCFactory.createGhostGPT(player, this, scaleFactor);
      spawnEntityAt(ghostGPT, new GridPoint2(8, 11), true, false);
    }
  }

  /**
   * Adds DeepSpin enemies onto the map.
   * @param total The total number of DeepSpins to be spawned.
   * @param scaleFactor The scale of increase in difficulty of the DeepSpin
   */
  public void spawnDeepspin(int total, float scaleFactor, Entity player) {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(3, 3);

    for (int i = 0; i < total; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity deepspin = NPCFactory.createDeepspin(player, this, scaleFactor);
      spawnEntityAt(deepspin, randomPos, true, true);
    }
  }

  /**
   * Adds GrokDroid enemies onto the map.
   * @param total The total number of GrokDroid to be spawned.
   * @param scaleFactor The scale of increase in difficulty of the GrokDroid
   */
  public void spawnGrokDroid(int total, float scaleFactor, Entity player) {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(3, 3);

    for (int i = 0; i < total; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity grokDroid = NPCFactory.createGrokDroid(player, this, scaleFactor);
      spawnEntityAt(grokDroid, randomPos, true, true);
    }
  }

  /**
   * Adds Vroomba enemies onto the map.
   * @param total The total number of Vroomba to be spawned.
   * @param scaleFactor The scale of increase in difficulty of the Vroomba
   */
  public void spawnVroomba(int total, float scaleFactor, Entity player) {
    Entity vroomba1 = NPCFactory.createVroomba(player, scaleFactor);
    Entity vroomba2 = NPCFactory.createVroomba(player, scaleFactor);
    spawnEntityAt(vroomba1, new GridPoint2(16, 5), true, false);
    spawnEntityAt(vroomba2, new GridPoint2(8, 5),  true, false);
  }

  // Enemy Projectiles
  public Entity spawnEnemyProjectile(Vector2 directionToFire, WeaponsStatsComponent source) {
    Entity laser = ProjectileFactory.createEnemyProjectile(directionToFire, source);
    spawnEntityAt(laser, new GridPoint2(0, 0), true, true);
    PhysicsProjectileComponent laserPhysics = laser.getComponent(PhysicsProjectileComponent.class);
    int projectileSpeed = 5; // Should be abstracted from WeaponsStatsComponent in future implementation
    laserPhysics.fire(directionToFire, projectileSpeed);
    return laser;
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
}
