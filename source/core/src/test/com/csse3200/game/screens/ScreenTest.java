package com.csse3200.game.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.screens.TutorialScreenDisplay;
import com.csse3200.game.components.screens.BaseEndScreenDisplays;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ScreenTest {

    private GdxGame mockGame;
    private ResourceService mockResourceService;

    private MockedStatic<RenderFactory> renderFactoryStatic;
    private MockedStatic<ServiceLocator> serviceLocatorStatic;

    @BeforeEach
    void setUp() {
        mockGame = mock(GdxGame.class);
        Renderer mockRenderer = mock(Renderer.class);
        CameraComponent mockCamera = mock(CameraComponent.class);
        Entity mockCameraEntity = mock(Entity.class);
        Stage mockStage = mock(Stage.class);

        when(mockRenderer.getCamera()).thenReturn(mockCamera);
        when(mockCamera.getEntity()).thenReturn(mockCameraEntity);

        RenderService mockRenderService = mock(RenderService.class);
        when(mockRenderService.getStage()).thenReturn(mockStage);

        mockResourceService = mock(ResourceService.class);
        EntityService mockEntityService = mock(EntityService.class);

        renderFactoryStatic = Mockito.mockStatic(RenderFactory.class);
        serviceLocatorStatic = Mockito.mockStatic(ServiceLocator.class);

        renderFactoryStatic.when(RenderFactory::createRenderer).thenReturn(mockRenderer);
        serviceLocatorStatic.when(ServiceLocator::getRenderService).thenReturn(mockRenderService);
        serviceLocatorStatic.when(ServiceLocator::getResourceService).thenReturn(mockResourceService);
        serviceLocatorStatic.when(ServiceLocator::getEntityService).thenReturn(mockEntityService);
        serviceLocatorStatic.when(ServiceLocator::clearExceptPlayer).thenAnswer(i -> null);
        serviceLocatorStatic.when(() -> ServiceLocator.registerRenderService(any())).thenAnswer(i -> null);
        serviceLocatorStatic.when(() -> ServiceLocator.registerTimeSource(any())).thenAnswer(i -> null);
    }

    @AfterEach
    void tearDown() {
        renderFactoryStatic.close();
        serviceLocatorStatic.close();
    }

    @Test
    void constructor_initializesDeathScreenUiAndStage_withoutErrors() {
        Texture tutBg = mock(Texture.class);
        when(mockResourceService.getAsset("images/death_screen_background.png", Texture.class)).thenReturn(tutBg);

        DeathScreen deathScreen = new DeathScreen(mockGame);

        assertNotNull(deathScreen.getStage(), "DeathScreen stage should be initialized");
        assertNotNull(deathScreen.getUiDisplay(), "DeathScreen UI display should be initialized");

        TutorialScreen tutScreen = new TutorialScreen(mockGame);
        Entity deathUi = deathScreen.createUIScreen(tutScreen.getStage());
        assertNotNull(deathUi.getComponent(InputDecorator.class), "DeathScreen UI should have InputDecorator");
        assertNotNull(deathUi.getComponent(BaseEndScreenDisplays.class), "DeathScreen UI should have BaseEndScreenDisplays");
    }

    @Test
    void constructor_initializesWinScreenUiAndStage_withoutErrors() {
        Texture tutBg = mock(Texture.class);
        when(mockResourceService.getAsset("images/win_screen_background.png", Texture.class)).thenReturn(tutBg);

        WinScreen winScreen = new WinScreen(mockGame);
        TutorialScreen tutScreen = new TutorialScreen(mockGame);

        assertNotNull(winScreen.getStage(), "WinScreen stage should be initialized");
        assertNotNull(winScreen.getUiDisplay(), "WinScreen UI display should be initialized");

        Entity winUi = winScreen.createUIScreen(tutScreen.getStage());
        assertNotNull(winUi.getComponent(InputDecorator.class), "WinScreen UI should have InputDecorator");
        assertNotNull(winUi.getComponent(BaseEndScreenDisplays.class), "WinScreen UI should have BaseEndScreenDisplays");
    }

    @Test
    void constructor_initializesTutorialScreenUiAndStage_withoutErrors() {
        Texture tutBg = mock(Texture.class);
        when(mockResourceService.getAsset("images/background.png", Texture.class)).thenReturn(tutBg);

        TutorialScreen tutScreen = new TutorialScreen(mockGame);

        assertNotNull(tutScreen.getStage(), "TutorialScreen stage should be initialized");

        Entity tutUi = tutScreen.createUIScreen(tutScreen.getStage());
        assertNotNull(tutUi.getComponent(InputDecorator.class), "TutorialScreen UI should have InputDecorator");
        assertNotNull(tutUi.getComponent(TutorialScreenDisplay.class), "TutorialScreen UI should have TutorialScreenDisplay");
    }

    @Test
    void updateTime_callsDeathUiDisplaySetElapsedSeconds() throws Exception {
        BaseEndScreenDisplays mockUiDisplay = mock(BaseEndScreenDisplays.class);
        DeathScreen deathScreen = new DeathScreen(mockGame);

        var field = DeathScreen.class.getDeclaredField("uiDisplay");
        field.setAccessible(true);
        field.set(deathScreen, mockUiDisplay);

        deathScreen.updateTime(42L);

        verify(mockUiDisplay).setElapsedSeconds(42L);
    }

    @Test
    void updateTime_callsWinUiDisplaySetElapsedSeconds() throws Exception {
        BaseEndScreenDisplays mockUiDisplay = mock(BaseEndScreenDisplays.class);
        WinScreen winScreen = new WinScreen(mockGame);

        var field = WinScreen.class.getDeclaredField("uiDisplay");
        field.setAccessible(true);
        field.set(winScreen, mockUiDisplay);

        winScreen.updateTime(42L);

        verify(mockUiDisplay).setElapsedSeconds(42L);
    }
}