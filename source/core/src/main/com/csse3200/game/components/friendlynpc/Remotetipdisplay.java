package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.services.ServiceLocator;

public final class Remotetipdisplay{
    private Remotetipdisplay() {}
    public static Table attach(Stage stage) {
        return attach(stage, 0.22f);
    }
    public static Table attach(Stage stage, float sizeParam) {
        if (stage == null) return null;
        final Skin skin = trySkin();
        float minDim = Math.min(stage.getWidth(), stage.getHeight());
        float target;
        if (sizeParam > 1f) {
            float maxPx = Math.max(48f, minDim * 0.60f);
            target = MathUtils.clamp(sizeParam, 24f, maxPx);
        } else {
            float px = minDim * MathUtils.clamp(sizeParam, 0.06f, 0.60f);
            target = MathUtils.clamp(px, 24f, minDim * 0.60f);
        }
        target = Math.round(target / 8f) * 8f;
        Table root = new Table(skin);
        root.setFillParent(true);
        root.setTouchable(Touchable.disabled);
        root.left();
        stage.addActor(root);
        Drawable tipDrawable = getTipDrawable();
        if (tipDrawable instanceof TextureRegionDrawable trd) {
            trd.setMinWidth(target);
            trd.setMinHeight(target);
        }
        Image tip = new Image(tipDrawable);
        tip.setScaling(Scaling.stretch);
        tip.setSize(target, target);
        tip.setTouchable(Touchable.disabled);
        Stack stack = new Stack();
        stack.setSize(target, target);
        stack.setTouchable(Touchable.disabled);
        stack.add(tip);
        root.add(stack)
                .size(target, target)
                .padLeft(16f)
                .expandY()
                .left();

        root.toFront();
        return root;
    }

    private static Drawable getTipDrawable() {
        Texture tex = ServiceLocator.getResourceService().getAsset("images/remotetip.png", Texture.class);
        if (tex == null) tex = new Texture(Gdx.files.internal("images/remotetip.png"));
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return new TextureRegionDrawable(tex);
    }

    private static Skin trySkin() {
        if (ServiceLocator.getResourceService().containsAsset("uiskin.json", Skin.class)) {
            return ServiceLocator.getResourceService().getAsset("uiskin.json", Skin.class);
        } else {
            return new Skin(Gdx.files.internal("uiskin.json"));
        }
    }
}
