package com.csse3200.game.areas;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaHelpersTest {

  private ResourceService rs;
  private TerrainFactory mockFactory;
  private CameraComponent mockCamera;
  private GameArea area; // shared partial mock for all tests

  @BeforeEach
  void beforeEach() {
    ServiceLocator.clear();
    ServiceLocator.registerRenderService(mock(com.csse3200.game.rendering.RenderService.class));

    // Mock Gdx.app to run postRunnable immediately
    Gdx.app = mock(Application.class);
    doAnswer(inv -> { inv.getArgument(0, Runnable.class).run(); return null; })
            .when(Gdx.app).postRunnable(any(Runnable.class));

    // Common mocks
    rs = mock(ResourceService.class);
    ServiceLocator.registerResourceService(rs);

    mockFactory = mock(TerrainFactory.class);
    mockCamera = mock(CameraComponent.class);

    // Create a partial mock of GameArea with constructor args
    area = mock(GameArea.class, withSettings()
            .useConstructor(mockFactory, mockCamera)
            .defaultAnswer(CALLS_REAL_METHODS));
  }

  @Test
  void ensureTexturesLoadsMissing() {
    when(rs.containsAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(false);

    area.ensureTextures(new String[]{"a.png", "b.png"});

    verify(rs, atLeastOnce()).loadTextures(any(String[].class));
    verify(rs, atLeastOnce()).loadAll();
  }

  @Test
  void ensurePlayerAtlasLoadsWhenMissing() {
    when(rs.containsAsset("images/player.atlas", com.badlogic.gdx.graphics.g2d.TextureAtlas.class))
            .thenReturn(false);

    area.ensurePlayerAtlas();

    verify(rs).loadTextureAtlases(any(String[].class));
    verify(rs, atLeastOnce()).loadAll();
  }

  @Test
  void setupTerrainWithOverlaySpawnsTerrainAndOverlay() {
    ServiceLocator.registerEntityService(new EntityService());

    TerrainComponent terrain = mock(TerrainComponent.class);
    when(mockFactory.createTerrain(any())).thenReturn(terrain);

    int before = area.getEntities().size();
    area.setupTerrainWithOverlay(mockFactory, TerrainFactory.TerrainType.FOREST_DEMO,
            new Color(1f, 0f, 0f, 0.2f));
    int after = area.getEntities().size();

    assertEquals(before + 2, after, "setupTerrainWithOverlay should add 2 entities");

    boolean hasTerrain = area.getEntities().stream()
            .anyMatch(e -> e.getComponent(TerrainComponent.class) != null);
    boolean hasOverlay = area.getEntities().stream()
            .anyMatch(e -> e.getComponent(SolidColorRenderComponent.class) != null);

    assertTrue(hasTerrain, "There should be an entity with TerrainComponent");
    assertTrue(hasOverlay, "There should be an entity with SolidColorRenderComponent");
  }

  @Test
  void clearAndLoadTransitionsToNextArea() {
    ServiceLocator.registerEntityService(new EntityService());

    GameArea nextArea = mock(GameArea.class);

    final boolean[] called = {false};
    doAnswer(inv -> { called[0] = true; return null; }).when(nextArea).create();

    area.clearAndLoad(() -> nextArea);

    assertTrue(called[0], "Next area create() should have been called");
  }
}
