package com.csse3200.game.components.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ButtonSoundService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PauseMenuDisplayTest {

    private static HeadlessApplication app;
    private GdxGame mockGame;
    private PauseMenuDisplay display;
    private ButtonSoundService mockButtonSound;
    private MockedStatic<ServiceLocator> serviceLocatorStatic;

    @BeforeAll
    static void startHeadless() {
        app = new HeadlessApplication(new ApplicationAdapter() {}); // starts a headless application
        Gdx.gl20 = mock(GL20.class);
        Gdx.gl = Gdx.gl20;
    }

    @AfterAll
    static void stopHeadless() {
        if (app != null) app.exit();      // exits the headless application after all tests
    }

    @BeforeEach
    void setup() {
        mockGame = mock(GdxGame.class);
        mockButtonSound = mock(ButtonSoundService.class);

        serviceLocatorStatic = mockStatic(ServiceLocator.class);
        serviceLocatorStatic.when(ServiceLocator::getButtonSoundService).thenReturn(mockButtonSound);

        display = spy(new PauseMenuDisplay(mockGame));

        doAnswer(inv -> {
            String label = inv.getArgument(0, String.class);
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = new BitmapFont();
            return new TextButton(label, style);
        }).when(display).button(anyString(), anyFloat(), any());
    }

    @AfterEach
    void tearDown() {
        if (serviceLocatorStatic != null) serviceLocatorStatic.close();
    }

    @Test
    void getZIndex_returns100() {
        assertEquals(100f, display.getZIndex());
    }

    @Test
    void clickplaysButtonSound() {
        display = spy(new PauseMenuDisplay(mockGame));

        Runnable buttonAction = () -> ServiceLocator.getButtonSoundService().playClick();
        buttonAction.run();

        verify(mockButtonSound, times(1)).playClick();
    }
}


