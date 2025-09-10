package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
  }

  @Test
  void ensureTexturesLoadsMissing() {
    ResourceService rs = mock(ResourceService.class);
    when(rs.containsAsset(any(String.class), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(false);
    ServiceLocator.registerResourceService(rs);

    GameArea area = new GameArea() { @Override public void create() {} };
    area.ensureTextures(new String[] {"a.png", "b.png"});

    verify(rs, atLeastOnce()).loadTextures(any(String[].class));
    verify(rs, atLeastOnce()).loadAll();
  }

  @Test
  void ensurePlayerAtlasLoadsWhenMissing() {
    ResourceService rs = mock(ResourceService.class);
    when(rs.containsAsset(eq("images/player.atlas"), eq(com.badlogic.gdx.graphics.g2d.TextureAtlas.class)))
        .thenReturn(false);
    ServiceLocator.registerResourceService(rs);

    GameArea area = new GameArea() { @Override public void create() {} };
    area.ensurePlayerAtlas();

    verify(rs).loadTextureAtlases(any(String[].class));
    verify(rs, atLeastOnce()).loadAll();
  }

  @Test
  void setupTerrainWithOverlaySpawnsTerrainAndOverlay() {
    // Prepare services required by spawnEntity (EntityService)
    ServiceLocator.registerEntityService(new com.csse3200.game.entities.EntityService());
    // Register RenderService for SolidColorRenderComponent
    ServiceLocator.registerRenderService(mock(com.csse3200.game.rendering.RenderService.class));

    TerrainFactory factory = mock(TerrainFactory.class);
    TerrainComponent terrain = mock(TerrainComponent.class);
    when(factory.createTerrain(any())).thenReturn(terrain);

    GameArea area = new GameArea() { @Override public void create() {} };

    int before = area.getEntities().size();
    area.setupTerrainWithOverlay(factory, TerrainFactory.TerrainType.FOREST_DEMO, new Color(1f, 0f, 0f, 0.2f));
    int after = area.getEntities().size();

    // Should add terrain entity + overlay entity
    assert after == before + 2;

    // One of the spawned entities has the TerrainComponent, and one has SolidColorRenderComponent
    boolean hasTerrain = false;
    boolean hasOverlay = false;
    for (Entity e : area.getEntities()) {
      if (e.getComponent(TerrainComponent.class) != null) hasTerrain = true;
      if (e.getComponent(com.csse3200.game.rendering.SolidColorRenderComponent.class) != null) hasOverlay = true;
    }
    assert hasTerrain;
    assert hasOverlay;
  }

  private static class FlagArea extends GameArea {
    private final Runnable onCreate;
    private FlagArea(Runnable onCreate) { this.onCreate = onCreate; }
    @Override public void create() { if (onCreate != null) onCreate.run(); }
  }

  @Test
  void clearAndLoadTransitionsToNextArea() {
    // Needed for spawnEntity during disposal in some implementations
    ServiceLocator.registerEntityService(new com.csse3200.game.entities.EntityService());

    GameArea current = new FlagArea(null);
    final boolean[] called = {false};
    current.clearAndLoad(() -> new FlagArea(() -> called[0] = true));
    assert called[0];
  }
}


