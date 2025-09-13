package com.csse3200.game.areas;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaHelpersTest {

  @BeforeEach
  void beforeEach() {
    // Reset services that we care about per test
    ServiceLocator.clear();

    // Mock RenderService to avoid NPE during SolidColorRenderComponent.create()
    ServiceLocator.registerRenderService(mock(com.csse3200.game.rendering.RenderService.class));

    // Mock Gdx.app to run postRunnable synchronously
    Gdx.app = mock(Application.class);
    doAnswer(invocation -> {
      Runnable r = invocation.getArgument(0);
      r.run(); // Execute immediately
      return null;
    }).when(Gdx.app).postRunnable(any(Runnable.class));
  }


  @Test
  void ensureTexturesLoadsMissing() {
    ResourceService rs = mock(ResourceService.class);
    when(rs.containsAsset(any(String.class), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(false);
    ServiceLocator.registerResourceService(rs);

    GameArea area = mock(GameArea.class);
    doCallRealMethod().when(area).ensureTextures(any(String[].class));

    // Call the real method
    area.ensureTextures(new String[] {"a.png", "b.png"});

    verify(rs, atLeastOnce()).loadTextures(any(String[].class));
    verify(rs, atLeastOnce()).loadAll();
  }

  @Test
  void ensurePlayerAtlasLoadsWhenMissing() {
    // Mock ResourceService
    ResourceService rs = mock(ResourceService.class);
    when(rs.containsAsset("images/player.atlas", com.badlogic.gdx.graphics.g2d.TextureAtlas.class))
            .thenReturn(false);
    ServiceLocator.registerResourceService(rs);

    // Spy on a GameArea so real methods run
    GameArea area = spy(GameArea.class);

    // Call the real ensurePlayerAtlas method
    doCallRealMethod().when(area).ensurePlayerAtlas();
    area.ensurePlayerAtlas();

    // Verify that ResourceService was asked to load the atlas
    verify(rs).loadTextureAtlases(any(String[].class));
    verify(rs, atLeastOnce()).loadAll();
  }

  @Test
  void setupTerrainWithOverlaySpawnsTerrainAndOverlay() {
    // Prepare services required by spawnEntity (EntityService)
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(mock(com.csse3200.game.rendering.RenderService.class));

    // Mock TerrainFactory and TerrainComponent
    TerrainFactory factory = mock(TerrainFactory.class);
    TerrainComponent terrain = mock(TerrainComponent.class);
    when(factory.createTerrain(any())).thenReturn(terrain);

    // Use a spy on GameArea to allow real method calls
    GameArea area = spy(GameArea.class);
    doCallRealMethod().when(area).setupTerrainWithOverlay(any(), any(), any());

    int before = area.getEntities().size();
    area.setupTerrainWithOverlay(factory, TerrainFactory.TerrainType.FOREST_DEMO, new Color(1f, 0f, 0f, 0.2f));
    int after = area.getEntities().size();

    // Assert that exactly 2 entities were added
    assertEquals(before + 2, after, "setupTerrainWithOverlay should add 2 entities");

    // Check that one entity has TerrainComponent and one has SolidColorRenderComponent
    boolean hasTerrain = area.getEntities().stream()
            .anyMatch(e -> e.getComponent(TerrainComponent.class) != null);
    boolean hasOverlay = area.getEntities().stream()
            .anyMatch(e -> e.getComponent(SolidColorRenderComponent.class) != null);

    Assertions.assertTrue(hasTerrain, "There should be an entity with TerrainComponent");
    Assertions.assertTrue(hasOverlay, "There should be an entity with SolidColorRenderComponent");
  }

  private static class FlagArea extends GameArea {
    private final Runnable onCreate;
    private FlagArea(Runnable onCreate) { this.onCreate = onCreate; }
    @Override public void create() { if (onCreate != null) onCreate.run(); }
  }

  @Test
  void clearAndLoadTransitionsToNextArea() {
    // Needed for spawnEntity during disposal in some implementations
    ServiceLocator.registerEntityService(new EntityService());

    final boolean[] called = {false};
    GameArea current = new FlagArea(null);
    current.clearAndLoad(() -> new FlagArea(() -> called[0] = true));

    assertTrue(called[0]);
  }
}


