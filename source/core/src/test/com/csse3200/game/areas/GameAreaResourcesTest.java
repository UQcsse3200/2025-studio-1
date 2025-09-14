package com.csse3200.game.areas;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaResourcesTest {

  @BeforeEach
  void setup() {
    ServiceLocator.clear();
  }

  @Test
  void ensureTexturesSkipsWhenPresent() {
    ResourceService rs = mock(ResourceService.class);
    when(rs.containsAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(true);
    ServiceLocator.registerResourceService(rs);

    // Mock the abstract GameArea class
    GameArea area = mock(GameArea.class, CALLS_REAL_METHODS);

    // Call the real method on the mock
    area.ensureTextures(new String[]{"x.png", "y.png"});

    verify(rs, never()).loadTextures(any(String[].class));
    verify(rs, never()).loadAll();
  }

  @Test
  void unloadAssetsDelegatesToResourceService() {
    ResourceService rs = mock(ResourceService.class);
    ServiceLocator.registerResourceService(rs);

    // Mock the abstract GameArea class
    GameArea area = mock(GameArea.class, CALLS_REAL_METHODS);

    String[] paths = new String[]{"a.png", "b.png"};
    area.unloadAssets(paths);

    verify(rs).unloadAssets(paths);
  }
}
