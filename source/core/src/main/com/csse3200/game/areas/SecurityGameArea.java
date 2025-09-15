package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.items.ItemHoldComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.entities.configs.Consumables;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.entities.factories.items.ConsumableFactory;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.entities.factories.ShopFactory;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.components.shop.CatalogService;
import com.csse3200.game.components.shop.ShopDemo;


import javax.naming.spi.ObjectFactory;
import java.util.Collections;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Minimal generic Security room: walls, doors, and a subtle background overlay. */
public class SecurityGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public SecurityGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    GenericLayout.ensureGenericAssets(this);
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.SECURITY_ROOM,
        new Color(0.08f, 0.08f, 0.1f, 0.30f));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();
    spawnPlatforms();
    spawnSecurityProps();
  }

  // Assets ensured via GenericLayout

  private void spawnBordersAndDoors() {
    GenericLayout.addLeftRightDoorsAndWalls(this, cameraComponent, WALL_WIDTH,
        this::loadBackToFloor5, this::loadOffice);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadBackToFloor5() {
    clearAndLoad(() -> new Floor5GameArea(terrainFactory, cameraComponent));
  }

  private void loadOffice() {
    clearAndLoad(() -> new OfficeGameArea(terrainFactory, cameraComponent));
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
}
