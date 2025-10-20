package com.csse3200.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.ServiceLocator;

public class PromptFactory {
    private PromptFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static void createPrompt() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ithaca.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 28; // base font size
        params.minFilter = Texture.TextureFilter.Linear;  // smooth scaling
        params.magFilter = Texture.TextureFilter.Linear;
        params.borderColor = Color.BLACK;
        params.borderWidth = 2; // gives outline for readability
        params.shadowColor = new Color(0, 0, 0, 0.6f);
        params.shadowOffsetX = 2;
        params.shadowOffsetY = 2;

        BitmapFont font = generator.generateFont(params);
        generator.dispose();

        // Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;

        // Add a background (9-patch image or simple tinted drawable)
        Texture bgTexture = new Texture(Gdx.files.internal("images/textbg.png"));
        NinePatch ninePatch = new NinePatch(bgTexture, 10, 10, 10, 10);
        labelStyle.background = new NinePatchDrawable(ninePatch);

        Label buyPrompt = new Label("", labelStyle);
        buyPrompt.setVisible(false);
        buyPrompt.setAlignment(Align.center); // center text inside background
        buyPrompt.setSize(500f, 100f);   // set box size
        buyPrompt.setWrap(true); // wrap text if needed
        buyPrompt.setOrigin(Align.center);
        buyPrompt.setPosition(
                ServiceLocator.getRenderService().getStage().getWidth() / 2f,
                ServiceLocator.getRenderService().getStage().getHeight() / 2f, // bottom quarter of screen
                Align.center
        );
        buyPrompt.setTouchable(Touchable.disabled); // don't block clicks
        ServiceLocator.getRenderService().getStage().addActor(buyPrompt);
        ServiceLocator.registerPrompt(buyPrompt);
    }
}
