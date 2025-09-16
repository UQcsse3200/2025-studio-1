package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;

import javax.swing.*;

/** Shipping Room*/
public class ShippingGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public ShippingGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
      super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    GenericLayout.ensureGenericAssets(this);
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.SHIPPING,
        new Color(0.12f, 0.12f, 0.10f, 0.26f));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();
    spawnShipmentBoxLid();
    spawnShipmentCrane();
    spawnConveyor();

    Entity ui = new Entity();
    ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Shipping"));
    spawnEntity(ui);
  }

  private void spawnShipmentBoxLid() {
    float lidX = 7.25f;
    float lidY = 5.1f;

    Entity BoxLid = ObstacleFactory.createShipmentBoxes();
    BoxLid.setPosition(lidX, lidY);

    spawnEntity(BoxLid);
  }

  private void spawnShipmentCrane() {
    float craneX = 8.7f;
    float craneY = 7.85f;

    Entity ShipmentCrane = ObstacleFactory.createShipmentCrane();
    ShipmentCrane.setPosition(craneX, craneY);

    spawnEntity(ShipmentCrane);
  }

  private void spawnConveyor() {
    float conveyorX = 10.7f;
    float conveyorY = 8f;

    Entity Conveyor = ObstacleFactory.createConveyor();
    Conveyor.setPosition(conveyorX, conveyorY);

    spawnEntity(Conveyor);
  }

//  private void spawnBordersAndDoors() {
//    GenericLayout.addLeftRightDoorsAndWalls(this, cameraComponent, WALL_WIDTH,
//        this::loadTunnel, this::loadServer);
//  }
  private void spawnBordersAndDoors() {
    if (cameraComponent == null) return;
    Bounds b = getCameraBounds(cameraComponent);
    addSolidWallLeft(b, WALL_WIDTH);
    float leftDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float leftDoorY = b.bottomY;
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(b.leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadTunnel));
    spawnEntity(leftDoor);

    addSolidWallRight(b, WALL_WIDTH);

    float rightDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float rightDoorY = b.topY - rightDoorHeight;
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(b.rightX - WALL_WIDTH - 0.001f, rightDoorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadServer));
    spawnEntity(rightDoor);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadTunnel() {
    clearAndLoad(() -> new TunnelGameArea(terrainFactory, cameraComponent));
  }

  private void loadServer() {
    clearAndLoad(() -> new ServerGameArea(terrainFactory, cameraComponent));
  }

  @Override
  public String toString() {
    return "Shipping";
  }

  public Entity getPlayer(){
    //placeholder
    return null;
  }

  public static ShippingGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
    return (new ShippingGameArea(terrainFactory, camera));
  }
}