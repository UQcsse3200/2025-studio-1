package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.enemy.EnemyWaves;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  protected TerrainFactory terrainFactory;
  protected CameraComponent cameraComponent;
  /** Prevents re-entrant room transitions across areas */
  protected static boolean isTransitioning = false;


  private final float VERTICAL_HEIGHT_OFFSET = 9.375f;

  private static final String deepSpin = "DeepSpin";
  private static final String ghostGpt = "GhostGPT";
  private static final String vroomba = "Vroomba";
  private static final String grokDroid = "GrokDroid";
  private static final String turret = "Turret";


  protected EnemyWaves wavesManager; // manage waves via terminal command
  protected static int roomNumber = 1;

  protected GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
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
    if (isTransitioning || ServiceLocator.isTransitioning()) return false;
    isTransitioning = true;
    ServiceLocator.setTransitioning(true);
    return true;
  }

  /** Mark the end of a room transition. */
  protected void endTransition() {
    isTransitioning = false;
    ServiceLocator.setTransitioning(false);
  }

  /**
   * Spawn entity at its current position
   *
   * @param entity Entity (not yet registered)
   */
  public void spawnEntity(Entity entity) {
    areaEntities.add(entity);
    if (ServiceLocator.isTransitioning()) {
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
   * Start enemy waves from terminal command by typing "waves".
   */
  public void startWaves(Entity player) {
    if (wavesManager == null || wavesManager.allWavesFinished()) {
      int room = getRoomNumber();
      int maxWaves = room > 4 ? 2 : 1; // mimic original behaviour: higher rooms get 2 waves
      wavesManager = new EnemyWaves(maxWaves, this, player);
      Gdx.app.log("GameArea", "Initializing waves: room=" + room + " maxWaves=" + maxWaves);
    }
    wavesManager.startWave();
  }

  /**
   * Returns the room number corresponding to the current floor.
   * @return Room number as an int if the floor name is in the format "Floor2"
   * with 2 being any number, otherwise returns 1.
   */
  public int getRoomNumber() { // changed from protected to public for EnemyWaves access
    return roomNumber;
  }

  /**
   * Returns the base difficulty scale of the current room.
   * @return Scaling factor as a float.
   */
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
   * Spawns the enemies based on the room number given.
   * @param roomNumber The number of the current floor/room.
   * @param total The total number of enemies to be spawned.
   * @param scaleFactor The scaling factor of the difficulty of the enemies to be spawned.
   * @param player The player {@link Entity} that is to be target by the enemies.
   */
  public void spawnEnemies(int roomNumber, int total, float scaleFactor, Entity player) {
      Map<String, ArrayList<Vector2>> positions = getEnemySpawnPosition(roomNumber);
      switch (roomNumber) {
          case 1:
              spawnDeepspin(total, scaleFactor, player, positions);
              spawnTurret(total,scaleFactor, player, positions);
              break;
          case 2:
              spawnVroomba(total, scaleFactor, player, positions);
              spawnGhostGPT(total, scaleFactor, player, positions);
              break;
          case 3:
              spawnVroomba(total, scaleFactor, player, positions);
              spawnDeepspin(total, scaleFactor, player, positions);
              break;
          case 4:
              spawnGhostGPT(total, scaleFactor, player, positions);
              spawnDeepspin(total, scaleFactor, player, positions);
              break;
          case 5:
              spawnGhostGPT(total, scaleFactor, player, positions);
              spawnDeepspin(total, scaleFactor, player, positions);
              spawnVroomba(total, scaleFactor, player, positions);
              break;
          case 6, 7:
              spawnGhostGPT(total, scaleFactor, player, positions);
              spawnGrokDroid(total, scaleFactor, player, positions);
              break;
          default:
              spawnGhostGPT(total, scaleFactor, player, positions);
              spawnGrokDroid(total, scaleFactor, player, positions);
              break;
      }
  }

  /**
   * Adds GhostGPT enemies onto the map.
   * @param total The total number of GhostGPT to be spawned.
   * @param scaleFactor The scale of increase in difficulty of the GhostGPT
   */
  public void spawnGhostGPT(
          int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
      ArrayList<Vector2> spawnPositions = positions.get(ghostGpt);
      for (Vector2 pos : spawnPositions) {
          Entity ghostGpt = NPCFactory.createGhostGPT(player, this, scaleFactor);
          ghostGpt.setPosition(pos);
          spawnEntity(ghostGpt);
      }
  }

  /**
   * Adds DeepSpin enemies onto the map.
   * @param total The total number of DeepSpins to be spawned.
   * @param scaleFactor The scale of increase in difficulty of the DeepSpin
   */
  public void spawnDeepspin(
          int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
      ArrayList<Vector2> spawnPositions = positions.get(deepSpin);

      for (Vector2 pos : spawnPositions) {
          Entity deepSpin = NPCFactory.createDeepspin(player, this, scaleFactor);
          deepSpin.setPosition(pos);
          spawnEntity(deepSpin);
      }
  }

  /**
   * Adds GrokDroid enemies onto the map.
   * @param total The total number of GrokDroid to be spawned.
   * @param scaleFactor The scale of increase in difficulty of the GrokDroid
   */
  public void spawnGrokDroid(
          int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
      ArrayList<Vector2> spawnPositions = positions.get(grokDroid);
      for (Vector2 pos : spawnPositions) {
          Entity grokDroid = NPCFactory.createGrokDroid(player, this, scaleFactor);
          grokDroid.setPosition(pos);
          spawnEntity(grokDroid);
      }
  }

  /**
   * Adds Vroomba enemies onto the map.
   * @param total The total number of Vroomba to be spawned.
   * @param scaleFactor The scale of increase in difficulty of the Vroomba
   */
  public void spawnVroomba(
          int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
      ArrayList<Vector2> spawnPositions = positions.get(vroomba);
      for (Vector2 pos : spawnPositions) {
          Entity vroomba = NPCFactory.createVroomba(player, scaleFactor);
          vroomba.setPosition(pos);
          spawnEntity(vroomba);
      }
  }
    /**
     * Adds Turret enemies onto the map.
     * @param total The total number of Turret to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the GhostGPT
     */
    public void spawnTurret(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(turret);
        for (Vector2 pos : spawnPositions) {
            Entity turret = NPCFactory.createTurret(player, this, scaleFactor);
            turret.setPosition(pos);
            spawnEntity(turret);
        }
    }

  /**
   * Spawns the projectile used by the Ghost GPT Enemy
   * @param directionToFire The direction in which the projectile is to be fired.
   * @param source The damage and other statistics that the projectile will use.
   * @return The spawned projectile {@link Entity}
   */
  public Entity spawnGhostGPTProjectile(Vector2 directionToFire, WeaponsStatsComponent source) {
    Entity laser = ProjectileFactory.createEnemyLaserProjectile(directionToFire, source);
    spawnEntityAt(laser, new GridPoint2(0, 0), true, true);
    PhysicsProjectileComponent laserPhysics = laser.getComponent(PhysicsProjectileComponent.class);
    int projectileSpeed = 5; // Should be abstracted from WeaponsStatsComponent in future implementation
    laserPhysics.fire(directionToFire, projectileSpeed);
    return laser;
  }

  protected Map<String, ArrayList<Vector2>> getEnemySpawnPosition(int roomNumber) {
      HashMap<String, ArrayList<Vector2>> positions = new HashMap<>();
      ArrayList<Vector2> respectiveSpawns = new ArrayList<>();
      switch (roomNumber) {
          case 1:
              respectiveSpawns.add(new Vector2(2.5f, 11f));
              respectiveSpawns.add(new Vector2(5.4f, 11f));
              respectiveSpawns.add(new Vector2(8.2f, 11f));
              respectiveSpawns.add(new Vector2(11.1f, 10f));
              positions.put(deepSpin, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(12f, 5f));
              positions.put(turret, (ArrayList<Vector2>) respectiveSpawns.clone());
              break;
          case 2:
              respectiveSpawns.add(new Vector2(5.7f, 5f));
              respectiveSpawns.add(new Vector2(1.5f, 7f));
              positions.put(vroomba, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(11.5f, 10f));
              positions.put(ghostGpt, (ArrayList<Vector2>) respectiveSpawns.clone());
              break;
          case 3:
              respectiveSpawns.add(new Vector2(8.4f, 10f));
              respectiveSpawns.add(new Vector2(2f, 10f));
              positions.put(deepSpin, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(12f, 5f));
              respectiveSpawns.add(new Vector2(2f, 5f));
              positions.put(vroomba, (ArrayList<Vector2>) respectiveSpawns.clone());
              break;
          case 4:
              respectiveSpawns.add(new Vector2(12f, 10f));
              respectiveSpawns.add(new Vector2(2f, 5f));
              positions.put(ghostGpt, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(7f, 11f));
              respectiveSpawns.add(new Vector2(3f, 10f));
              positions.put(deepSpin, (ArrayList<Vector2>) respectiveSpawns.clone());
              break;
          case 5:
              respectiveSpawns.add(new Vector2(2.7f, 8f));
              positions.put(ghostGpt, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(5.6f, 10f));
              positions.put(vroomba, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(8.2f, 11f));
              respectiveSpawns.add(new Vector2(11.1f, 10f));
              positions.put(deepSpin, (ArrayList<Vector2>) respectiveSpawns.clone());
              break;
          case 6:
              respectiveSpawns.add(new Vector2(13f, 4f));
              respectiveSpawns.add(new Vector2(10f, 4f));
              positions.put(ghostGpt, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(8.4f, 10f));
              respectiveSpawns.add(new Vector2(11.3f, 8f));
              positions.put(grokDroid, (ArrayList<Vector2>) respectiveSpawns.clone());
              break;
          case 7:
              respectiveSpawns.add(new Vector2(11f, 10f));
              respectiveSpawns.add(new Vector2(2f, 5f));
              respectiveSpawns.add(new Vector2(11f, 5f));
              positions.put(ghostGpt, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(3f, 10f));
              respectiveSpawns.add(new Vector2(7f, 8f));
              positions.put(grokDroid, (ArrayList<Vector2>) respectiveSpawns.clone());
              break;
          default:
              respectiveSpawns.add(new Vector2(12f, 11f));
              respectiveSpawns.add(new Vector2(7.6f, 4f));
              respectiveSpawns.add(new Vector2(2f, 4f));
              positions.put(ghostGpt, (ArrayList<Vector2>) respectiveSpawns.clone());
              respectiveSpawns.clear();
              respectiveSpawns.add(new Vector2(5f, 10f));
              respectiveSpawns.add(new Vector2(2f, 10f));
              positions.put(grokDroid, (ArrayList<Vector2>) respectiveSpawns.clone());
              break;
      }
      return positions;
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

  /** Convenience to load textures if not already loaded. */
  protected void ensureTextures(String[] texturePaths) {
    ResourceService rs = ServiceLocator.getResourceService();
    List<String> toLoad = new ArrayList<>();
    for (String path : texturePaths) {
      if (!rs.containsAsset(path, Texture.class)) {
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
    ResourceService rs = ServiceLocator.getResourceService();
    if (!rs.containsAsset("images/player.atlas", TextureAtlas.class)) {
      rs.loadTextureAtlases(new String[] {"images/player.atlas"});
      rs.loadAll();
    }
  }

  /** Unload a set of assets if loaded. */
  protected void unloadAssets(String[] assetPaths) {
    ResourceService rs = ServiceLocator.getResourceService();
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
      overlay.addComponent(new SolidColorRenderComponent(overlayColor));
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

  protected Bounds getCameraBounds(CameraComponent cameraComponent) {
    OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
    Vector2 camPos = cameraComponent.getEntity().getPosition();
    float viewWidth = cam.viewportWidth;
    float viewHeight = cam.viewportHeight;
    float leftX = camPos.x - viewWidth / 2f;
    float rightX = camPos.x + viewWidth / 2f;
    float bottomY = camPos.y - VERTICAL_HEIGHT_OFFSET / 2f;
    float topY = camPos.y + VERTICAL_HEIGHT_OFFSET / 2f;
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
    door.addComponent(new DoorComponent(onEnter));
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
    door.addComponent(new DoorComponent(onEnter));
    spawnEntity(door);
  }

  protected <T extends GameArea> void loadArea(Class<T> areaClass) {
    clearAndLoad(() -> {
      try {
        // Pass the concrete terrainFactory and cameraComponent
        return areaClass
                .getConstructor(TerrainFactory.class, CameraComponent.class)
                .newInstance(this.terrainFactory, this.cameraComponent);
      } catch (Exception e) {
        throw new RuntimeException("Failed to create " + areaClass.getSimpleName(), e);
      }
    });
  }
  /** Helper to clear current entities and transition to a new area. */
  protected void clearAndLoad(Supplier<GameArea> nextAreaSupplier) {
    if (!beginTransition()) return;

    for (Entity entity : areaEntities) {
        entity.setEnabled(false);
    }

    /** Ensure transition happens on the render thread to avoid race conditions **/
    Gdx.app.postRunnable(() -> {
      /** Phase 1: disable and dispose current area's entities **/
      for (Entity entity : areaEntities) {
        entity.dispose();
      }
      areaEntities.clear();

      /** Phase 2: on the next frame, build the next area to avoid Box2D world-locked/native races **/
      Gdx.app.postRunnable(() -> {
        try {
          GameArea next = nextAreaSupplier.get();
          ServiceLocator.registerGameArea(next);
          next.create();
        } finally {
          endTransition();
        }
      });
    });
  }
    /**
     * Spawns decorative object doors (non-functional) at given positions.
     *
     * @param leftDoorPos  grid position for the left/bottom decorative door
     * @param rightDoorPos grid position for the right/top decorative door
     */
    protected void spawnObjectDoors(GridPoint2 leftDoorPos, GridPoint2 rightDoorPos) {
        if (rightDoorPos != null) {
            Entity rightDoor = ObstacleFactory.createDoor();
            spawnEntityAt(rightDoor, rightDoorPos, false, false);
        }

        if (leftDoorPos != null) {
            Entity leftDoor = ObstacleFactory.createDoor();
            spawnEntityAt(leftDoor, leftDoorPos, false, false);
        }
    }
  public void spawnItem(Entity item, GridPoint2 position) {
    spawnEntityAt(item, position, false, false);
  }
}
