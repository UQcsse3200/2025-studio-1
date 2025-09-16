package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.services.SaveLoadService;

/** Room 5 with its own background styling. */
public class MainHall extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public MainHall(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    ensureAssets();
    terrain = terrainFactory.createTerrain(TerrainType.MAIN_HALL);
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
      "images/mainHall-background.png",
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
    clearAndLoad(() -> new Reception(terrainFactory, cameraComponent));
  }

  private void loadSecurity() {
    clearAndLoad(() -> new SecurityGameArea(terrainFactory, cameraComponent));
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  public Entity getPlayer() {
    //tempoary placeholder return null to stop errors
    return null;
  }

  @Override
  public String toString() {
    return "Mainhall";
  }

  public static MainHall load(TerrainFactory terrainFactory, CameraComponent camera) {
    return (new MainHall(terrainFactory, camera));
  }
}


