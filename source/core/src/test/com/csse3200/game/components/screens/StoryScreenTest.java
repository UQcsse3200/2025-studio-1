package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.GdxGame;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class StoryScreenTest {
    private GdxGame mockGame;
    private StoryScreen screen;
    private Label mockLabel;

    @Before
    public void setUp() throws Exception {
        mockGame = Mockito.mock(GdxGame.class);
        screen = new StoryScreen(mockGame);

        mockLabel = Mockito.mock(Label.class);

        Field labelField = StoryScreen.class.getDeclaredField("dialogueLabel");
        labelField.setAccessible(true);
        labelField.set(screen, mockLabel);
    }

    @Test
    public void escapeTest() throws Exception {
        Method endStory = StoryScreen.class.getDeclaredMethod("endStory");
        endStory.setAccessible(true);
        endStory.invoke(screen);

        Mockito.verify(mockGame).setScreen(GdxGame.ScreenType.MAIN_GAME);
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
}
