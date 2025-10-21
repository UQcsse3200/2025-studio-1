
package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.GdxGame;


/**
 * Loading screen with background and large text.
 * Ensures the screen is actually drawn before switching to MAIN_GAME.
 */
public class LoadingScreen extends ScreenAdapter {
    private final GdxGame game;
    private final SpriteBatch batch;
    private final Texture background;
    private final BitmapFont font;

    private boolean firstFrame = true;

    public LoadingScreen(GdxGame game) {
        this.game = game;

        this.batch = new SpriteBatch();
        this.background = new Texture("images/menu_background.png");

        // Use default bitmap font and scale it up
        this.font = new BitmapFont();
        font.getData().setScale(3f); // Make text bigger
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background and text
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font.draw(batch, "LOADING...", Gdx.graphics.getWidth() / 2f - 100,
                Gdx.graphics.getHeight() / 2f);
        batch.end();

        // Switch to main game safely after first render
        if (firstFrame) {
            firstFrame = false;
            Gdx.app.postRunnable(() -> game.setScreen(GdxGame.ScreenType.MAIN_GAME));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        font.dispose();
    }
}