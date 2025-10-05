package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public final class CompanionControlPanel {
    private CompanionControlPanel() {}

    public static Table attach(Stage stage, Entity comp) {
        return attach(stage, comp, 0.44f);
    }

    public static Table attach(Stage stage, Entity comp, float sizeParam) {
        if (stage == null || comp == null) return null;

        final Skin skin = trySkin();
        float minDim = Math.min(stage.getWidth(), stage.getHeight());
        float target;

        if (sizeParam > 1f) {
            float maxPx = Math.max(96f, minDim * 0.60f);
            target = MathUtils.clamp(sizeParam, 48f, maxPx);
        } else {
            float px = minDim * MathUtils.clamp(sizeParam, 0.10f, 0.60f);
            target = MathUtils.clamp(px, 48f, minDim * 0.60f);
        }

        target = Math.round(target / 8f) * 8f;

        Table root = new Table(skin);
        root.setFillParent(true);
        root.setTouchable(Touchable.childrenOnly);
        root.left();
        stage.addActor(root);

        Drawable drawable = getRemoteDrawable();
        if (drawable instanceof TextureRegionDrawable trd) {
            trd.setMinWidth(target);
            trd.setMinHeight(target);
        }

        Image remote = new Image(drawable);
        remote.setScaling(Scaling.stretch);
        remote.setSize(target, target);
        remote.setTouchable(Touchable.disabled);

        Table overlay = new Table(skin);
        overlay.setTouchable(Touchable.enabled);

        float scale = target / 256f;
        float padTop = clamp(110f * scale, 6f, target * 0.65f);
        overlay.padTop(padTop)
                .padBottom(4f * scale)
                .padLeft(6f * scale)
                .padRight(6f * scale);
        overlay.defaults().left().pad(2f * scale);

        float fs = Math.max(0.56f * scale, 0.44f);

        final CheckBox follow = new CheckBox(" Follow", skin);
        final CheckBox attack = new CheckBox(" Auto", skin);
        follow.getLabel().setColor(Color.LIGHT_GRAY);
        attack.getLabel().setColor(Color.LIGHT_GRAY);
        follow.getLabel().setFontScale(fs);
        attack.getLabel().setFontScale(fs);

        // 读取组件状态
        var followComp = comp.getComponent(PartnerFollowComponent.class);
        if (followComp != null) {
            follow.setChecked(followComp.isMove());
        }

        var autoComp = comp.getComponent(AutoCompanionShootComponent.class);
        var orderComp = comp.getComponent(CompanionFollowShootComponent.class);

        if (autoComp != null) {
            attack.setChecked(autoComp.isAttack());
        } else if (orderComp != null) {
            attack.setChecked(!orderComp.isAttack());
        } else {
            attack.setChecked(false);
        }

        follow.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                var ff = comp.getComponent(PartnerFollowComponent.class);
                if (ff != null) ff.setMove(follow.isChecked());
            }
        });

        attack.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                boolean autoOn = attack.isChecked();
                var auto = comp.getComponent(AutoCompanionShootComponent.class);
                var order = comp.getComponent(CompanionFollowShootComponent.class);
                if (auto != null) auto.setAttack(autoOn);
                if (order != null) order.setAttack(!autoOn);
            }
        });

        overlay.add(follow).left().row();
        overlay.add(attack).left().row();

        Stack stack = new Stack();
        stack.setSize(target, target);
        stack.setTouchable(Touchable.childrenOnly);
        stack.add(remote);
        stack.add(overlay);

        root.add(stack)
                .size(target, target)
                .padLeft(16f)
                .expandY()
                .left();

        root.toFront();
        return root;
    }

    // ===== 工具 =====
    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static Drawable getRemoteDrawable() {
        Texture tex = ServiceLocator.getResourceService().getAsset("images/remote.png", Texture.class);
        if (tex == null) tex = new Texture(Gdx.files.internal("images/remote.png"));
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









