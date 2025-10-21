package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

/**
 * Tests for {@link Remotetipdisplay}.
 *
 * Notes:
 * - We stub ResourceService to return mocked Texture and Skin so no real files/GL are touched.
 * - We mock Stage to verify addActor() and rely on the returned Table to inspect sizes.
 */
class RemotetipdisplayTest {

    private Application app;

    @BeforeEach
    void setupGdx() {
        // Some LibGDX paths may call Gdx.*; provide a mock Application to be safe.
        app = mock(Application.class);
        Gdx.app = app;
    }

    @Test
    @DisplayName("attach(null) returns null and does nothing")
    void attach_nullStage_returnsNull() {
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            assertNull(Remotetipdisplay.attach(null));
            // No calls into ServiceLocator should be necessary
            sl.verifyNoInteractions();
        }
    }
}

