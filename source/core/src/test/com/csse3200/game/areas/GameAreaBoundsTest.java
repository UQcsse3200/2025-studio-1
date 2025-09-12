package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class GameAreaBoundsTest {
  @Test
  void computesCameraBoundsCorrectly() {
    OrthographicCamera cam = new OrthographicCamera();
    cam.viewportWidth = 30f;
    cam.viewportHeight = 20f;
    Entity camEntity = new Entity();
    camEntity.setPosition(10f, 5f);
    CameraComponent cameraComponent = mock(CameraComponent.class);
    when(cameraComponent.getCamera()).thenReturn(cam);
    when(cameraComponent.getEntity()).thenReturn(camEntity);

    GameArea area = new GameArea() { @Override public void create() {} };
    GameArea.Bounds b = area.getCameraBounds(cameraComponent);

    assertEquals(-5f, b.leftX, 0.0001f);
    assertEquals(25f, b.rightX, 0.0001f);
    assertEquals(-5f, b.bottomY, 0.0001f);
    assertEquals(15f, b.topY, 0.0001f);
    assertEquals(30f, b.viewWidth, 0.0001f);
    assertEquals(20f, b.viewHeight, 0.0001f);
    assertEquals(new Vector2(10f, 5f), b.camPos);
  }
}


