package com.csse3200.game.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.difficultymenu.DifficultyMenuDisplay;
import com.csse3200.game.components.screens.LeaderboardScreenDisplay;
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
import org.mockito.ArgumentCaptor;
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
        serviceLocatorStatic.when(() -> ServiceLocator.registerInputService(any())).thenAnswer(i -> null);
        serviceLocatorStatic.when(() -> ServiceLocator.registerResourceService(any())).thenAnswer(i -> null);
        serviceLocatorStatic.when(() -> ServiceLocator.registerEntityService(any())).thenAnswer(i -> null);
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

        Entity deathUi = deathScreen.createUIScreen(deathScreen.getStage());
        assertNotNull(deathUi.getComponent(InputDecorator.class), "DeathScreen UI should have InputDecorator");
        assertNotNull(deathUi.getComponent(BaseEndScreenDisplays.class), "DeathScreen UI should have BaseEndScreenDisplays");
    }

    @Test
    void constructor_initializesWinScreenUiAndStage_withoutErrors() {
        Texture tutBg = mock(Texture.class);
        when(mockResourceService.getAsset("images/win_screen_background.png", Texture.class)).thenReturn(tutBg);

        WinScreen winScreen = new WinScreen(mockGame);

        assertNotNull(winScreen.getStage(), "WinScreen stage should be initialized");
        assertNotNull(winScreen.getUiDisplay(), "WinScreen UI display should be initialized");

        Entity winUi = winScreen.createUIScreen(winScreen.getStage());
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
    void constructor_initializesDifficultyScreenUiAndStage_withoutErrors() {
        DifficultyScreen difficultyScreen = new DifficultyScreen(mockGame);

        assertNotNull(difficultyScreen.getStage(), "DifficultyScreen stage should be initialized");

        Entity diffUi = difficultyScreen.createUIScreen(difficultyScreen.getStage());
        assertNotNull(diffUi.getComponent(InputDecorator.class), "DifficultyScreen UI should have InputDecorator");
        assertNotNull(diffUi.getComponent(DifficultyMenuDisplay.class), "DifficultyScreen UI should have DiffucultyMenuDisplay");
    }

    @Test
    void constructor_initializesLeaderboardScreenUiAndStage_withoutErrors() {
        LeaderboardScreen leaderboardScreen = new LeaderboardScreen(mockGame);

        assertNotNull(leaderboardScreen.getStage(), "LeaderboardScreen stage should be initialized");

        Entity leaderboardUi = leaderboardScreen.createUIScreen(leaderboardScreen.getStage());
        assertNotNull(leaderboardUi.getComponent(InputDecorator.class), "LeaderboardScreen UI should have InputDecorator");
        assertNotNull(leaderboardUi.getComponent(LeaderboardScreenDisplay.class), "LeaderboardScreen UI should have LeaderboardScreenDisplay");
    }

    @Test
    void constructor_initializesMainMenu_withoutErrors() {
        Texture bgTex = mock(Texture.class);
        when(mockResourceService.getAsset("images/menu_background.png", Texture.class)).thenReturn(bgTex);

        Stage mockStage = ServiceLocator.getRenderService().getStage();
        ArgumentCaptor<Actor> actorCap = ArgumentCaptor.forClass(com.badlogic.gdx.scenes.scene2d.Actor.class);
        EntityService mockEntityService = ServiceLocator.getEntityService();
        ArgumentCaptor<Entity> entityCap = ArgumentCaptor.forClass(Entity.class);

        new MainMenuScreen(mockGame);

        verify(mockStage, atLeastOnce()).addActor(actorCap.capture());
        boolean hasBgImage = actorCap.getAllValues().stream().anyMatch(a -> a instanceof com.badlogic.gdx.scenes.scene2d.ui.Image);
        assertTrue(hasBgImage, "Main menu background Image should be added to stage");

        verify(mockEntityService, atLeastOnce()).register(entityCap.capture());
        Entity ui = entityCap.getValue();
        assertNotNull(ui.getComponent(com.csse3200.game.components.mainmenu.MainMenuDisplay.class));
        assertNotNull(ui.getComponent(InputDecorator.class));
        assertNotNull(ui.getComponent(com.csse3200.game.components.mainmenu.MainMenuActions.class));
    }

    @Test
    void constructor_initializesSettingsScreenUiAndStage_withoutErrors() {
        Texture menuBg = mock(Texture.class);
        when(mockResourceService.getAsset("images/menu_background.png", Texture.class)).thenReturn(menuBg);

        SettingsScreen settings = new SettingsScreen(mockGame);

        assertNotNull(settings.getStage(), "SettingsScreen stage should be initialized");

        Entity ui = settings.createUIScreen(settings.getStage());
        assertNotNull(ui.getComponent(InputDecorator.class), "SettingsScreen UI should have InputDecorator");
        assertNotNull(ui.getComponent(com.csse3200.game.components.settingsmenu.SettingsMenuDisplay.class),
                "SettingsScreen UI should have SettingsMenuDisplay");
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

    @Test
    void mainMenu_render_callsEntityUpdate_andRendererRender() {
        Texture bgTex = mock(Texture.class);
        when(mockResourceService.getAsset("images/menu_background.png", Texture.class)).thenReturn(bgTex);

        MainMenuScreen mainMenu = new MainMenuScreen(mockGame);

        mainMenu.render(0.016f);

        EntityService mockEntityService = ServiceLocator.getEntityService();
        verify(mockEntityService, times(1)).update();
        Renderer mockRenderer = RenderFactory.createRenderer();
        verify(mockRenderer, times(1)).render();
    }

    @Test
    void mainMenu_resize_forwardsToRenderer() {
        Texture bgTex = mock(Texture.class);
        when(mockResourceService.getAsset("images/menu_background.png", Texture.class)).thenReturn(bgTex);

        MainMenuScreen mainMenu = new MainMenuScreen(mockGame);
        Renderer mockRenderer = RenderFactory.createRenderer();

        mainMenu.resize(1280, 720);

        verify(mockRenderer, times(1)).resize(1280, 720);
    }

    @Test
    void mainMenu_dispose_cleansServicesAndAssets() {
        // Given
        Texture bgTex = mock(Texture.class);
        when(mockResourceService.getAsset("images/menu_background.png", Texture.class)).thenReturn(bgTex);

        RenderService mockRenderService = ServiceLocator.getRenderService();
        EntityService mockEntityService = ServiceLocator.getEntityService();
        Renderer mockRenderer = RenderFactory.createRenderer(); // returns the mocked renderer from setUp()

        MainMenuScreen mainMenu = new MainMenuScreen(mockGame);

        mainMenu.dispose();

        verify(mockRenderer, times(1)).dispose();
        verify(mockResourceService, times(1)).unloadAssets(new String[]{"images/logo.png", "images/menu_background.png"});
        verify(mockRenderService, times(1)).dispose();
        verify(mockEntityService, times(1)).dispose();
        serviceLocatorStatic.verify(ServiceLocator::clear, times(2));
    }
}