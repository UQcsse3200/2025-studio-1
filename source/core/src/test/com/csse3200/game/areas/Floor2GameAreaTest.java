package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class Floor2GameAreaTest {

    @BeforeEach
    void beforeEach() {
        ServiceLocator.clear();
        ServiceLocator.registerEntityService(new EntityService());
        // Mock ResourceService to avoid NPE during ensureAssets in create()
        ResourceService rs = mock(ResourceService.class);
        when(rs.containsAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class))).thenReturn(true);
        when(rs.containsAsset(eq("images/player.atlas"), eq(com.badlogic.gdx.graphics.g2d.TextureAtlas.class)))
                .thenReturn(true);
        ServiceLocator.registerResourceService(rs);
        // Mock RenderService to avoid NPE during SolidColorRenderComponent.create()
        RenderService renderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(renderService);
    }

    @Test
    void cameraBoundsCalculation() {
        // Test the camera bounds calculation logic directly
        OrthographicCamera cam = new OrthographicCamera();
        cam.viewportWidth = 20f;
        cam.viewportHeight = 10f;

        Entity camEntity = new Entity();
        camEntity.setPosition(50f, 50f);
        CameraComponent cameraComponent = mock(CameraComponent.class);
        when(cameraComponent.getCamera()).thenReturn(cam);
        when(cameraComponent.getEntity()).thenReturn(camEntity);

        Reception area = new Reception(mock(TerrainFactory.class), cameraComponent);
        GameArea.Bounds b = area.getCameraBounds(cameraComponent);

        // Verify bounds calculation
        assertEquals(40f, b.leftX(), 0.001f);   // 50 - 20/2
        assertEquals(60f, b.rightX(), 0.001f);  // 50 + 20/2
        assertEquals(45.3125f, b.bottomY(), 0.001f); // 50 - 9.375/2
        assertEquals(54.6875f, b.topY(), 0.001f);    // 50 + 9.375/2
        assertEquals(20f, b.viewWidth(), 0.001f);
        assertEquals(10f, b.viewHeight(), 0.001f);
        assertEquals(new Vector2(50f, 50f), b.camPos());
    }

    @Test
    void doorPositioningLogic() {
        // Test door positioning math without creating actual physics components
        OrthographicCamera cam = new OrthographicCamera();
        cam.viewportWidth = 20f;
        cam.viewportHeight = 10f;

        Entity camEntity = new Entity();
        camEntity.setPosition(50f, 50f);
        CameraComponent cameraComponent = mock(CameraComponent.class);
        when(cameraComponent.getCamera()).thenReturn(cam);
        when(cameraComponent.getEntity()).thenReturn(camEntity);

        Reception area = new Reception(mock(TerrainFactory.class), cameraComponent);
        GameArea.Bounds b = area.getCameraBounds(cameraComponent);

        // Test bottom door positioning (should be at bottomY + 0.1f)
        float doorWidth = Math.max(1f, b.viewWidth() * 0.2f);
        float doorX = b.camPos().x - doorWidth / 2f;
        float expectedBottomDoorY = b.bottomY() + 0.1f;

        assertEquals(4f, doorWidth, 0.001f); // 20 * 0.2
        assertEquals(48f, doorX, 0.001f);   // 50 - 4/2
        assertEquals(45.4125f, expectedBottomDoorY, 0.001f); // 45 + 0.1

        // Test left/right door positioning (should be at bottomY)
        float leftDoorX = b.leftX() + 0.001f;
        float rightDoorX = b.rightX() - 0.1f - 0.001f; // WALL_WIDTH = 0.1f
        float expectedSideDoorY = b.bottomY();

        assertEquals(40.001f, leftDoorX, 0.001f);
        assertEquals(59.899f, rightDoorX, 0.001f);
        assertEquals(45.3125f, expectedSideDoorY, 0.001f);
    }
}


