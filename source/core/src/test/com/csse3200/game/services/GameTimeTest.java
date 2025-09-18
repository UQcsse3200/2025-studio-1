package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class GameTimeTest {
    GameTime gameTime;

    @BeforeAll
    static void beforeAll() {
        Gdx.graphics = mock(Graphics.class);
        when(Gdx.graphics.getDeltaTime()).thenReturn(10f);
    }

    @BeforeEach
    void beforeEach() {
        gameTime = new GameTime();
    }

    @Test
    void shouldPause() {
        gameTime.setPaused(true);
        shouldScale(1, 0, 10f);

        gameTime.setPaused(false);
        shouldScale(1, 10f, 10f);
    }

    @Test
    void shouldCheckPause() {
        assertFalse(gameTime.isPaused());

        gameTime.setPaused(true);
        assertTrue(gameTime.isPaused());
    }

    @Test
    void shouldGiveCorrectDelta() {
        shouldScale(1f, 10f, 10f);
    }

    @Test
    void shouldScaleDown() {
        shouldScale(0.5f, 5f, 10f);
    }

    @Test
    void shouldScaleUp() {
        shouldScale(2f, 20f, 10f);
    }

    @Test
    void shouldScaleToZero() {
        shouldScale(0f, 0f, 10f);
    }

    private void shouldScale(float scale, float delta, float rawDelta) {
        gameTime.setTimeScale(scale);
        assertEquals(delta, gameTime.getDeltaTime());
        assertEquals(rawDelta, gameTime.getRawDeltaTime());
    }
}
