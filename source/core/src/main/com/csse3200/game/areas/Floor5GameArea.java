package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.SolidColorRenderComponent;

/** Room 5 with its own background styling. */
public class Floor5GameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  public Floor5GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
  }

  @Override
  public void create() {
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

    Entity ui = new Entity();
    ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 5"));
    spawnEntity(ui);
  }

  private void spawnBordersAndReturnDoor() {
    OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
    Vector2 camPos = cameraComponent.getEntity().getPosition();
    float viewWidth = cam.viewportWidth;
    float viewHeight = cam.viewportHeight;
    float leftX = camPos.x - viewWidth / 2f;
    float rightX = camPos.x + viewWidth / 2f;
    float bottomY = camPos.y - viewHeight / 2f;
    float topY = camPos.y + viewHeight / 2f;

    // Right border solid
    Entity right = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
    right.setPosition(rightX - WALL_WIDTH, bottomY);
    spawnEntity(right);
    // Left border split with a door to return to Floor 2
    float doorHeight = Math.max(1f, viewHeight * 0.2f);
    float doorY = camPos.y - doorHeight / 2f;
    float leftTopSegHeight = Math.max(0f, (topY) - (doorY + doorHeight));
    if (leftTopSegHeight > 0f) {
      Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
      leftTop.setPosition(leftX, doorY + doorHeight);
      spawnEntity(leftTop);
    }
    float leftBottomSegHeight = Math.max(0f, (doorY - bottomY));
    if (leftBottomSegHeight > 0f) {
      Entity leftBottom = ObstacleFactory.createWall(WALL_WIDTH, leftBottomSegHeight);
      leftBottom.setPosition(leftX, bottomY);
      spawnEntity(leftBottom);
    }
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, doorHeight);
    leftDoor.setPosition(leftX + 0.001f, doorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor2));
    spawnEntity(leftDoor);
    Entity top = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
    top.setPosition(leftX, topY - WALL_WIDTH);
    spawnEntity(top);
    Entity bottom = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
    bottom.setPosition(leftX, bottomY);
    spawnEntity(bottom);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadBackToFloor2() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
    areaEntities.clear();
    Floor2GameArea floor2 = new Floor2GameArea(terrainFactory, cameraComponent);
    floor2.create();
  }
}


