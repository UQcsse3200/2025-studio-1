package com.csse3200.game.lighting;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class LightingEngineTest {

    @Test
    @DisplayName("exposes RayHandler")
    void getRayHandler_notNullWhenInjected() {
        RayHandler rh = mock(RayHandler.class);
        CameraComponent cam = mock(CameraComponent.class);
        when(cam.getCamera()).thenReturn(new OrthographicCamera());

        LightingEngine engine = new LightingEngine(rh, cam);
        assertNotNull(engine.getRayHandler());
    }

    @Test
    @DisplayName("renders with camera")
    void render_usesCameraAndUpdates() {
        RayHandler rh = mock(RayHandler.class);
        CameraComponent cam = mock(CameraComponent.class);
        when(cam.getCamera()).thenReturn(new OrthographicCamera());

        LightingEngine engine = new LightingEngine(rh, cam);
        engine.render();

        verify(rh).setCombinedMatrix(any(OrthographicCamera.class));
        verify(rh).updateAndRender();
        verifyNoMoreInteractions(rh);
    }

    @Test
    @DisplayName("forwards ambient to RayHandler")
    void setAmbientLight_forwardsToRayHandler() {
        RayHandler rh = mock(RayHandler.class);
        CameraComponent cam = mock(CameraComponent.class);
        when(cam.getCamera()).thenReturn(new OrthographicCamera());

        LightingEngine engine = new LightingEngine(rh, cam);
        engine.setAmbientLight(0.42f);

        verify(rh).setAmbientLight(0.42f);
    }

    @Test
    @DisplayName("switches camera used for render")
    void setCamera_changesMatrixSource() {
        RayHandler rh = mock(RayHandler.class);
        CameraComponent camA = mock(CameraComponent.class);
        CameraComponent camB = mock(CameraComponent.class);
        when(camA.getCamera()).thenReturn(new OrthographicCamera());
        when(camB.getCamera()).thenReturn(new OrthographicCamera());

        LightingEngine engine = new LightingEngine(rh, camA);

        engine.render();
        verify(rh).setCombinedMatrix(any(OrthographicCamera.class));
        verify(rh).updateAndRender();
        clearInvocations(rh);

        engine.setCamera(camB);
        engine.render();
        verify(rh).setCombinedMatrix(any(OrthographicCamera.class));
        verify(rh).updateAndRender();
    }

    @Test
    @DisplayName("disposes RayHandler")
    void dispose_disposesRayHandler() {
        RayHandler rh = mock(RayHandler.class);
        CameraComponent cam = mock(CameraComponent.class);
        when(cam.getCamera()).thenReturn(new OrthographicCamera());

        LightingEngine engine = new LightingEngine(rh, cam);
        engine.dispose();
        verify(rh).dispose();
    }
}