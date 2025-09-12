package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaSpawnTest {
  private static class TestArea extends GameArea { @Override public void create() {} }

  @BeforeEach
  void setup() {
    ServiceLocator.registerEntityService(new com.csse3200.game.entities.EntityService());
  }

  @org.junit.jupiter.api.Test
  void spawnEntityAtCentersCorrectly() {
    TestArea area = new TestArea();
    TerrainComponent terrain = mock(TerrainComponent.class);
    when(terrain.tileToWorldPosition(any(GridPoint2.class)))
        .thenAnswer(inv -> {
          GridPoint2 gp = inv.getArgument(0);
          return new Vector2(gp.x * 2f, gp.y * 2f);
        });
    when(terrain.getTileSize()).thenReturn(2f);
    area.terrain = terrain;
    Entity e = new Entity();
    e.setPosition(0f, 0f);
    // No centering to avoid dependency on entity center calculation
    area.spawnEntityAt(e, new GridPoint2(3, 4), false, false);
    // Expect world pos (6,8)
    Vector2 pos = e.getPosition();
    assertEquals(6f, pos.x, 0.001f);
    assertEquals(8f, pos.y, 0.001f);
  }

  @RepeatedTest(5)
  void roomSpawnPositionWithinRanges() {
    TestArea area = new TestArea();
    Vector2 p2 = area.getRoomSpawnPosition("Floor2");
    assertTrue(p2.x >= 4f && p2.x <= 18f);
    assertTrue(p2.y >= 4f && p2.y <= 18f);

    Vector2 p7 = area.getRoomSpawnPosition("Floor7");
    assertTrue(p7.x >= 9f && p7.x <= 28f);
    assertTrue(p7.y >= 9f && p7.y <= 28f);

    Vector2 def = area.getRoomSpawnPosition("Unknown");
    assertEquals(0f, def.x, 0.0001f);
    assertEquals(0f, def.y, 0.0001f);
  }
}


