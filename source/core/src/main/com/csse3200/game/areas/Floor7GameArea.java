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

/** Room 7 placed to the left of Floor 4. */
public class Floor7GameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  public Floor7GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
  }

  @Override
  public void create() {
    // Distinct terrain look
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO_ISO);
    spawnEntity(new Entity().addComponent(terrain));

    Entity overlay = new Entity();
    overlay.setScale(1000f, 1000f);
    overlay.setPosition(-500f, -500f);
    overlay.addComponent(new SolidColorRenderComponent(new Color(0.05f, 0.15f, 0.2f, 0.25f)));
    spawnEntity(overlay);

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();

    Entity ui = new Entity();
    ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 7"));
    spawnEntity(ui);
    // Spawn keycards in valid floors
    com.csse3200.game.areas.KeycardSpawnerSystem.spawnKeycards(this);

    // Add gate to next floor (if applicable)
    Entity gateToNextFloor = new Entity()
            .addComponent(new ColliderComponent())
            .addComponent(new KeycardGateComponent(1)); // Replace X with required level
    float x1 = MathUtils.random(4f, 18f);
    float y1 = MathUtils.random(4f, 18f);
    Vector2 keycardPos = new Vector2(x1, y1);
    Entity keycard = KeycardFactory.createKeycard(1); // Level 1 keycard
    keycard.setPosition(new Vector2(x1, y1));
    spawnEntity(keycard);
    spawnEntity(gateToNextFloor);

  }


  private void spawnBordersAndDoors() {
    OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
    Vector2 camPos = cameraComponent.getEntity().getPosition();
    float viewWidth = cam.viewportWidth;
    float viewHeight = cam.viewportHeight;
    float leftX = camPos.x - viewWidth / 2f;
    float rightX = camPos.x + viewWidth / 2f;
    float bottomY = camPos.y - viewHeight / 2f;
    float topY = camPos.y + viewHeight / 2f;

    // Solid borders on top/bottom and solid left
    Entity top = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
    top.setPosition(leftX, topY - WALL_WIDTH);
    spawnEntity(top);
    Entity bottom = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
    bottom.setPosition(leftX, bottomY);
    spawnEntity(bottom);
    Entity left = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
    left.setPosition(leftX, bottomY);
    spawnEntity(left);

    // Right border split with a vertical door -> back to Floor 4
    float rightDoorHeight = Math.max(1f, viewHeight * 0.2f);
    float rightDoorY = camPos.y - rightDoorHeight / 2f;
    float rightTopSegHeight = Math.max(0f, (topY) - (rightDoorY + rightDoorHeight));
    if (rightTopSegHeight > 0f) {
      Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
      rightTop.setPosition(rightX - WALL_WIDTH, rightDoorY + rightDoorHeight);
      spawnEntity(rightTop);
    }
    float rightBottomSegHeight = Math.max(0f, (rightDoorY - bottomY));
    if (rightBottomSegHeight > 0f) {
      Entity rightBottom = ObstacleFactory.createWall(WALL_WIDTH, rightBottomSegHeight);
      rightBottom.setPosition(rightX - WALL_WIDTH, bottomY);
      spawnEntity(rightBottom);
    }
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(rightX - WALL_WIDTH - 0.001f, rightDoorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor4));
    spawnEntity(rightDoor);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayerWithArrowKeys();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadBackToFloor4() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
    areaEntities.clear();
    Floor4GameArea room4 = new Floor4GameArea(terrainFactory, cameraComponent);
    room4.create();
  }
}


