package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.entities.factories.system.ObstacleFactory;

/**
 * Represents the Security Game Area within the game.
 * This area includes walls, doors, floors, security props, and platforms.
 * It handles spawning the player, collidable and decorative entities, and
 * transitioning to other game areas via doors.
 */
public class SecurityGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(2, 10);

  /** Textures used for the security room, including background and props. */
  private static final String[] securityTextures = {
          "images/SecurityBackground.png",
          "foreg_sprites/general/ThinFloor3.png",
          "foreg_sprites/Security/Monitor.png",
          "foreg_sprites/Security/Platform.png",
          "foreg_sprites/Security/RedLight.png",
          "foreg_sprites/Security/SecuritySystem.png",
          "foreg_sprites/futuristic/storage_crate_green2.png",
          "foreg_sprites/futuristic/storage_crate_dark2.png",
          "foreg_sprites/futuristic/SecurityCamera3.png"
  };

  /**
   * Constructs a SecurityGameArea.
   *
   * @param terrainFactory   Factory used to create terrain for this area.
   * @param cameraComponent  Camera component used to determine viewport boundaries.
   */
  public SecurityGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  /**
   * Creates the game area, loading textures, spawning terrain,
   * borders, doors, player, platforms, and security props.
   */
  @Override
  public void create() {
    // Load textures
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(securityTextures);
    resourceService.loadAll();

    // Spawn terrain
    terrain = terrainFactory.createTerrain(TerrainType.SECURITY_ROOM);
    spawnEntity(new Entity().addComponent(terrain));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnPlatforms();
    spawnSecurityProps();
  }

  /**
   * Spawns walls and doors on the left and right sides of the camera viewport.
   * Left door triggers transition back to Floor5GameArea.
   * Right door triggers transition to OfficeGameArea.
   */
  private void spawnBordersAndDoors() {
    if (cameraComponent == null) return;
    Bounds b = getCameraBounds(cameraComponent);

    // Left wall with door at ground level
    addSolidWallLeft(b, WALL_WIDTH);

    float leftDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float leftDoorY = b.bottomY;
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(b.leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor5));
    spawnEntity(leftDoor);

    // Right wall with door at the top-right
    addSolidWallRight(b, WALL_WIDTH);

    float rightDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float rightDoorY = b.topY - rightDoorHeight;
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(b.rightX - WALL_WIDTH - 0.001f, rightDoorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadOffice));
    spawnEntity(rightDoor);
  }

  /**
   * Spawns the player character at the predefined spawn point.
   */
  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  /**
   * Spawns thin floor platforms in the room, including
   * an extra platform below the top-right door.
   */
  private void spawnPlatforms() {
    for (int i = 0; i < 6; i++) {
      GridPoint2 platformPos = new GridPoint2(i * 5 + 2, 5);
      Entity platform = ObstacleFactory.createThinFloor();
      spawnEntityAt(platform, platformPos, true, false);
    }

    // Extra platform just below the top-right door
    GridPoint2 topRightPlatformPos = new GridPoint2(26, 18);
    Entity topRightPlatform = ObstacleFactory.createThinFloor();
    spawnEntityAt(topRightPlatform, topRightPlatformPos, true, false);
  }

  /**
    Spawns security-related props, including:
      Security system (collidable)
      Red light (decorative)
      Monitor (decorative)
      Large security camera (decorative)
      Security platforms (collidable)
   */
  private void spawnSecurityProps() {
    // Security System (collidable)
    GridPoint2 systemPos = new GridPoint2(27, 6);
    Entity system = ObstacleFactory.createSecuritySystem();
    spawnEntityAt(system, systemPos, true, false);

    // Red light (decorative)
    GridPoint2 redLightPos = new GridPoint2(14, 22);
    Entity redLight = ObstacleFactory.createRedLight();
    spawnEntityAt(redLight, redLightPos, false, false);

    // Monitor (decorative)
    GridPoint2 monitorPos = new GridPoint2(5, 6);
    Entity monitor = ObstacleFactory.createSecurityMonitor();
    spawnEntityAt(monitor, monitorPos, false, false);

    // Security camera (decorative, from your ObstacleFactory)
    GridPoint2 cameraPos = new GridPoint2(1, 19);
    Entity securityCamera = ObstacleFactory.createLargeSecurityCamera();
    spawnEntityAt(securityCamera, cameraPos, false, false);

    // 2 Security Platforms (collidable)
    for (int i = 0; i < 2; i++) {
      GridPoint2 platPos = new GridPoint2(24 - i * 5, 10 + i * 4);
      Entity plat = ObstacleFactory.createSecurityPlatform();
      spawnEntityAt(plat, platPos, true, false);
    }
  }

  /**
   * Clears the current game area and loads Floor5GameArea.
   */
  private void loadBackToFloor5() {
    clearAndLoad(() -> new Floor5GameArea(terrainFactory, cameraComponent));
  }
  /**
   * Clears the current game area and loads OfficeGameArea.
   */
  private void loadOffice() {
    clearAndLoad(() -> new OfficeGameArea(terrainFactory, cameraComponent));
  }
}
