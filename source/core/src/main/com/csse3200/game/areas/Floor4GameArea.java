package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.SolidColorRenderComponent;

/** Room 4 with its own background styling. */
public class Floor4GameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  public Floor4GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
  }

  @Override
  public void create() {
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO_ISO);
    spawnEntity(new Entity().addComponent(terrain));

    Entity overlay = new Entity();
    overlay.setScale(1000f, 1000f);
    overlay.setPosition(-500f, -500f);
    overlay.addComponent(new SolidColorRenderComponent(new Color(0.1f, 0.2f, 0.1f, 0.25f)));
    spawnEntity(overlay);

    spawnBordersAndReturnDoor();
    spawnPlayer();
    spawnFloor();

    float keycardX = 14f;
    float keycardY = 12f;
    Entity keycard = KeycardFactory.createKeycard(4);
    keycard.setPosition(new Vector2(keycardX, keycardY));
    spawnEntity(keycard);
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

    // Left border split with a vertical door -> Floor 7
    float leftDoorHeight = Math.max(1f, viewHeight * 0.2f);
    float leftDoorY = camPos.y - leftDoorHeight / 0.7f;
    float leftTopSegHeight = Math.max(0f, (topY) - (leftDoorY + leftDoorHeight));
    if (leftTopSegHeight > 0f) {
      Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
      leftTop.setPosition(leftX, leftDoorY + leftDoorHeight);
      spawnEntity(leftTop);
    }
    float leftBottomSegHeight = Math.max(0f, (leftDoorY - bottomY));
    if (leftBottomSegHeight > 0f) {
      Entity leftBottom = ObstacleFactory.createWall(WALL_WIDTH, leftBottomSegHeight);
      leftBottom.setPosition(leftX, bottomY);
      spawnEntity(leftBottom);
    }
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadFloor7));
    spawnEntity(leftDoor);

    // Right border split with a door to return to Floor 2
    float doorHeight = Math.max(1f, viewHeight * 0.2f);
    float doorY = camPos.y - doorHeight / 0.7f;
    float rightTopSegHeight = Math.max(0f, (topY) - (doorY + doorHeight));
    if (rightTopSegHeight > 0f) {
      Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
      rightTop.setPosition(rightX - WALL_WIDTH, doorY + doorHeight);
      spawnEntity(rightTop);
    }
    float rightBottomSegHeight = Math.max(0f, (doorY - bottomY));
    if (rightBottomSegHeight > 0f) {
      Entity rightBottom = ObstacleFactory.createWall(WALL_WIDTH, rightBottomSegHeight);
      rightBottom.setPosition(rightX - WALL_WIDTH, bottomY);
      spawnEntity(rightBottom);
    }
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, doorHeight);
    rightDoor.setPosition(rightX - WALL_WIDTH - 0.001f, doorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor2));
    spawnEntity(rightDoor);
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

  private void loadFloor7() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
    areaEntities.clear();
    Floor7GameArea room7 = new Floor7GameArea(terrainFactory, cameraComponent);
    room7.create();
  }
}


