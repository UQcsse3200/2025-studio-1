package com.csse3200.game.tutorial;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public final class CuesHud extends Component {
    private static final float SHOW_SECONDS = 1.6f;
    private final java.util.Map<Integer, String> cues;
    private Stage stage;
    private Table box;
    private Label label;
    private InputListener listener;
    private float timeLeft = 0f;  // seconds to fade out

    CuesHud(java.util.Map<Integer, String> cues) {
        this.cues = cues;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // UI: small box bottom-left
        box = new Table();
        box.setFillParent(true);
        box.bottom().left().pad(8f);

        var style = new Label.LabelStyle(
                new com.badlogic.gdx.graphics.g2d.BitmapFont(),
                com.badlogic.gdx.graphics.Color.WHITE
        );
        label = new Label("", style);
        label.setColor(1f, 1f, 1f, 0.0f); // start invisible
        box.add(label).left().pad(6f);
        stage.addActor(box);

        // Listen globally; don't consume events
        listener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                String msg = cues.get(keycode);
                if (msg != null) {
                    String keyName = com.badlogic.gdx.Input.Keys.toString(keycode);
                    show(keyName + " — " + msg);
                }
                return false;
            }
        };
        stage.addListener(listener);
    }

    private void show(String text) {
        label.setText(text);
        timeLeft = SHOW_SECONDS;
        label.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void update() {
        if (timeLeft > 0f) {
            timeLeft -= ServiceLocator.getTimeSource().getDeltaTime();
            float a = MathUtils.clamp(timeLeft / SHOW_SECONDS, 0f, 1f);
            label.setColor(1f, 1f, 1f, a);
        }
    }

    @Override
    public void dispose() {
        if (stage != null && listener != null) stage.removeListener(listener);
        if (box != null) box.remove();
    }
}