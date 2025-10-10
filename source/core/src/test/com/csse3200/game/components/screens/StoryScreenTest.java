package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.screens.StoryScreen;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;

public class StoryScreenTest {
    private GdxGame mockGame;
    private StoryScreen screen;
    private Label mockLabel;
    private Stage mockStage;
    private Skin mockSkin;
    private Texture mockTexture;
    private Viewport mockViewport;

    @Before
    public void setUp() throws Exception {
        mockGame = Mockito.mock(GdxGame.class);
        screen = new StoryScreen(mockGame);

        mockLabel = Mockito.mock(Label.class);

        Field labelField = StoryScreen.class.getDeclaredField("dialogueLabel");
        labelField.setAccessible(true);
        labelField.set(screen, mockLabel);

        mockStage = Mockito.mock(Stage.class);
        mockViewport = Mockito.mock(Viewport.class);
        Mockito.when(mockStage.getViewport()).thenReturn(mockViewport);

        mockSkin = Mockito.mock(Skin.class);
        mockTexture = Mockito.mock(Texture.class);

        Field stageField = StoryScreen.class.getDeclaredField("stage");
        stageField.setAccessible(true);
        stageField.set(screen, mockStage);

        Field skinField = StoryScreen.class.getDeclaredField("skin");
        skinField.setAccessible(true);
        skinField.set(screen, mockSkin);

        Field textureField = StoryScreen.class.getDeclaredField("bgTexture");
        textureField.setAccessible(true);
        textureField.set(screen, mockTexture);
    }

    @Test
    public void escapeTest() throws Exception {
        Method endStory = StoryScreen.class.getDeclaredMethod("endStory");
        endStory.setAccessible(true);
        endStory.invoke(screen);

        Mockito.verify(mockGame).setScreen(GdxGame.ScreenType.LOADING);
    }

    @Test
    public void spaceTest() throws Exception {
        Method advance = StoryScreen.class.getDeclaredMethod("advanceDialogue");
        advance.setAccessible(true);

        // First click of space
        advance.invoke(screen);
        Mockito.verify(mockLabel, Mockito.atLeastOnce()).setText(Mockito.anyString());

        // Second click of space
        advance.invoke(screen);
        Mockito.verify(mockLabel, Mockito.atLeastOnce()).setText(Mockito.anyString());

        assertTrue(true);
    }

    @Test
    public void testDispose() {
        screen.dispose();
        Mockito.verify(mockStage).dispose();
        Mockito.verify(mockSkin).dispose();
        Mockito.verify(mockTexture).dispose();
    }

    @Test
    public void testResize() {
        int width = 1024;
        int height = 720;
        screen.resize(width, height);
        Mockito.verify(mockViewport).update(width, height, true);
    }

}
