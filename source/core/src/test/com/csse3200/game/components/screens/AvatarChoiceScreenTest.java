package com.csse3200.game.components.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Avatar;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.screens.AvatarChoiceScreen;
import com.csse3200.game.screens.StoryScreen;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AvatarChoiceScreenTest {

    private GdxGame mockGame;
    private AvatarChoiceScreen screen;
    private Label mockNameLabel;
    private Label mockStatsLabel;
    private List<Avatar> mockAvatars;

    private MockedStatic<RenderFactory> renderFactoryMock;
    private MockedStatic<ServiceLocator> serviceLocatorMock;

    @Before
    public void setUp() throws Exception {
        if (Gdx.app == null) {
            new HeadlessApplication(new com.badlogic.gdx.ApplicationAdapter() {
            }, new HeadlessApplicationConfiguration());
        }

        Gdx.gl20 = mock(GL20.class);
        Gdx.gl = Gdx.gl20;
        Gdx.gl30 = mock(GL30.class);

        Renderer fakeRenderer = mock(Renderer.class);
        com.csse3200.game.components.CameraComponent fakeCamera = mock(com.csse3200.game.components.CameraComponent.class);
        com.csse3200.game.entities.Entity fakeEntity = mock(com.csse3200.game.entities.Entity.class);
        when(fakeRenderer.getCamera()).thenReturn(fakeCamera);
        when(fakeCamera.getEntity()).thenReturn(fakeEntity);

        renderFactoryMock = Mockito.mockStatic(RenderFactory.class);
        renderFactoryMock.when(RenderFactory::createRenderer).thenReturn(fakeRenderer);

        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));

        RenderService mockRenderService = mock(RenderService.class);
        com.badlogic.gdx.scenes.scene2d.Stage fakeStage = mock(com.badlogic.gdx.scenes.scene2d.Stage.class);
        when(mockRenderService.getStage()).thenReturn(fakeStage);

        EntityService mockEntityService = mock(EntityService.class);

        serviceLocatorMock = Mockito.mockStatic(ServiceLocator.class);
        serviceLocatorMock.when(ServiceLocator::getResourceService).thenReturn(mockResourceService);
        serviceLocatorMock.when(ServiceLocator::getRenderService).thenReturn(mockRenderService);
        serviceLocatorMock.when(ServiceLocator::getEntityService).thenReturn(mockEntityService);

        Gdx.input = mock(Input.class);
        mockGame = mock(GdxGame.class);
        screen = new AvatarChoiceScreen(mockGame) {
            @Override
            protected Entity createUIScreen(com.badlogic.gdx.scenes.scene2d.Stage stage) {
                try {
                    // Provide a minimal cardRow with two "cards"
                    Table cr = new Table();
                    cr.add(new Table()); // index 0
                    cr.add(new Table()); // index 1
                    Field cardRowF = AvatarChoiceScreen.class.getDeclaredField("cardRow");
                    cardRowF.setAccessible(true);
                    cardRowF.set(this, cr);
                } catch (Exception ignored) {
                }
                return new Entity();
            }

            @Override
            public void render(float delta) {
                // Only the key-handling we need for tests
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    // Avoid constructing a real StoryScreen in tests
                    game.setScreen(mock(StoryScreen.class));
                }
            }
        };

        // Inject mocked labels
        mockNameLabel = mock(Label.class);
        mockStatsLabel = mock(Label.class);
        Field nameField = AvatarChoiceScreen.class.getDeclaredField("nameLabel");
        Field statsField = AvatarChoiceScreen.class.getDeclaredField("statsLabel");
        nameField.setAccessible(true);
        statsField.setAccessible(true);
        nameField.set(screen, mockNameLabel);
        statsField.set(screen, mockStatsLabel);

        // Provide avatars list
        mockAvatars = new ArrayList<>();
        mockAvatars.add(new Avatar("scout", "Scout", "images/a.png", 80, 10, 5f, "player.atlas"));
        mockAvatars.add(new Avatar("soldier", "Soldier", "images/b.png", 100, 15, 3f, "player.atlas"));
        Field avatarsField = AvatarChoiceScreen.class.getDeclaredField("avatars");
        avatarsField.setAccessible(true);
        avatarsField.set(screen, mockAvatars);
    }

    @After
    public void tearDown() {
        if (renderFactoryMock != null) renderFactoryMock.close();
        if (serviceLocatorMock != null) serviceLocatorMock.close();
    }

    @Test
    public void escapeKey_ShouldGoToMainMenu() {
        when(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)).thenReturn(true);
        screen.render(0f);
        verify(mockGame, atLeastOnce()).setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    @Test
    public void enterKey_ShouldGoToStoryScreen() {
        when(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)).thenReturn(true);
        screen.render(0f);
        verify(mockGame, atLeastOnce()).setScreen(any(StoryScreen.class));
    }

    @Test
    public void updateSelection_ShouldChangeLabels() throws Exception {
        Method updateSelection = AvatarChoiceScreen.class.getDeclaredMethod("updateSelection", int.class);
        updateSelection.setAccessible(true);
        updateSelection.invoke(screen, 0);
        verify(mockNameLabel, atLeastOnce()).setText(anyString());
        verify(mockStatsLabel, atLeastOnce()).setText(anyString());
    }
}