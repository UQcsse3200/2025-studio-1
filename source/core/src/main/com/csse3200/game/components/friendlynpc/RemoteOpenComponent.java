package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class RemoteOpenComponent extends Component {
    private Actor panel;
    private int key = Input.Keys.C;

    public RemoteOpenComponent key(int k) { this.key = k; return this; }

    @Override
    public void update() {
        if (!Gdx.input.isKeyJustPressed(key)) return;
        Stage stage = ServiceLocator.getRenderService().getStage();
        if (stage == null) return;
        if (panel == null || panel.getStage() == null) {
            panel = CompanionControlPanel.attach(stage, entity);
        } else {
            panel.remove();
            panel = null;
        }
    }

    @Override
    public void dispose() {
        if (panel != null) { panel.remove(); panel = null; }
    }
}



