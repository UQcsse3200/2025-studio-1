package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShieldDisplayTest {

    private MockedStatic<ServiceLocator> sl;
    private ResourceService resourceService;
    private RenderService renderService;
    private Stage stage;
    private Viewport viewport;

    private static final String ICON_PATH = "images/npcshield.png";

    @BeforeEach
    void setUp() {
        // Mocks
        resourceService = mock(ResourceService.class);
        renderService = mock(RenderService.class);
        stage = mock(Stage.class);
        viewport = mock(Viewport.class);

        // ServiceLocator stubs
        sl = mockStatic(ServiceLocator.class);
        sl.when(ServiceLocator::getResourceService).thenReturn(resourceService);
        sl.when(ServiceLocator::getRenderService).thenReturn(renderService);

        // RenderService -> Stage -> Viewport
        when(renderService.getStage()).thenReturn(stage);
        when(stage.getViewport()).thenReturn(viewport);
        when(viewport.getWorldHeight()).thenReturn(720f);

        // ResourceService -> Texture
        Texture tex = mock(Texture.class);
        when(resourceService.containsAsset(ICON_PATH, Texture.class)).thenReturn(true);
        when(resourceService.getAsset(ICON_PATH, Texture.class)).thenReturn(tex);
    }

    @AfterEach
    void tearDown() {
        if (sl != null) sl.close();
    }

    @Test
    void create_addsIconToStage_andStartsHidden() throws Exception {
        Entity nurse = new Entity();
        ShieldDisplay hud = new ShieldDisplay();
        nurse.addComponent(hud);

        assertDoesNotThrow(nurse::create); // no crash on create
        verify(stage).addActor(any());     // icon added to stage

        // icon should be hidden initially
        Object icon = getPrivate(hud, "icon");
        assertNotNull(icon, "icon should be created");
        boolean visible = (boolean) icon.getClass().getMethod("isVisible").invoke(icon);
        assertFalse(visible, "icon should start hidden");
    }

    @Test
    void shieldStart_showsIcon() throws Exception {
        Entity nurse = new Entity();
        ShieldDisplay hud = new ShieldDisplay();
        nurse.addComponent(hud);
        nurse.create();

        nurse.getEvents().trigger("shieldStart");

        Object icon = getPrivate(hud, "icon");
        boolean visible = (boolean) icon.getClass().getMethod("isVisible").invoke(icon);
        assertTrue(visible, "icon should be visible after shieldStart");
    }

    @Test
    void shieldEnd_hidesIcon() throws Exception {
        Entity nurse = new Entity();
        ShieldDisplay hud = new ShieldDisplay();
        nurse.addComponent(hud);
        nurse.create();

        nurse.getEvents().trigger("shieldStart");
        nurse.getEvents().trigger("shieldEnd");

        Object icon = getPrivate(hud, "icon");
        boolean visible = (boolean) icon.getClass().getMethod("isVisible").invoke(icon);
        assertFalse(visible, "icon should be hidden after shieldEnd");
    }

    @Test
    void dispose_removesActorAndClearsReference() throws Exception {
        Entity nurse = new Entity();
        ShieldDisplay hud = new ShieldDisplay();
        nurse.addComponent(hud);
        nurse.create();

        hud.dispose();

        // icon field should be nulled out
        assertNull(getPrivate(hud, "icon"), "icon reference should be cleared on dispose");
    }

    //helper to read private fields
    private static Object getPrivate(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }
}


