package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.rendering.SolidColorRenderComponent;

/** Room 5 with its own background styling. */
public class Floor5GameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public Floor5GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    ensureAssets();
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO_HEX);
    spawnEntity(new Entity().addComponent(terrain));

    Entity overlay = new Entity();
    overlay.setScale(1000f, 1000f);
    overlay.setPosition(-500f, -500f);
    overlay.addComponent(new SolidColorRenderComponent(new Color(0.1f, 0.1f, 0.2f, 0.35f)));
    spawnEntity(overlay);

    spawnBordersAndReturnDoor();
    spawnPlayer();
    spawnFloor();


  }

  private void ensureAssets() {
    String[] textures = new String[] {
      "images/hex_grass_1.png", "images/hex_grass_2.png", "images/hex_grass_3.png",
      "foreg_sprites/general/LongFloor.png",
      "foreg_sprites/general/ThickFloor.png",
      "foreg_sprites/general/SmallSquare.png",
      "foreg_sprites/general/SmallStair.png",
      "foreg_sprites/general/SquareTile.png"
    };
    ensureTextures(textures);
    ensurePlayerAtlas();
  }

  private void spawnBordersAndReturnDoor() {
    Bounds b = getCameraBounds(cameraComponent);
   
    addVerticalDoorLeft(b, WALL_WIDTH, this::loadBackToFloor2);
    addVerticalDoorRight(b, WALL_WIDTH, this::loadSecurity);
    addSolidWallTop(b, WALL_WIDTH);
    addSolidWallBottom(b, WALL_WIDTH);
  }

  private void loadBackToFloor2() {
    clearAndLoad(() -> new Floor2GameArea(terrainFactory, cameraComponent));
  }

  private void loadSecurity() {
    clearAndLoad(() -> new SecurityGameArea(terrainFactory, cameraComponent));
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }
}


