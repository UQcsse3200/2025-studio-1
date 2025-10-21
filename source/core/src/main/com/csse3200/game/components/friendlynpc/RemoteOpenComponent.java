package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class RemoteOpenComponent extends Component {
    private Table tipRoot;
    private Table panelRoot;
    private int key = Input.Keys.C;
    private float tipSizeParam   = 0.22f;
    private float panelSizeParam = 0.44f;
    public RemoteOpenComponent key(int k) { this.key = k; return this; }
    public RemoteOpenComponent tipSize(float v) { this.tipSizeParam = v; return this; }
    public RemoteOpenComponent panelSize(float v) { this.panelSizeParam = v; return this; }
    @Override
    public void create() {
        showTip();
    }
    @Override
    public void update() {
        if (!Gdx.input.isKeyJustPressed(key)) return;
        Stage stage = ServiceLocator.getRenderService().getStage();
        if (stage == null) return;
        if (panelRoot == null || panelRoot.getStage() == null) {
            removeTip();
            panelRoot = CompanionControlPanel.attach(stage, entity, panelSizeParam);
        } else {
            removePanel();
            showTip();
        }
    }

    private void showTip() {
        if (tipRoot != null && tipRoot.getStage() != null) return;
        Stage stage = ServiceLocator.getRenderService().getStage();
        if (stage == null) return;
        tipRoot = Remotetipdisplay.attach(stage, tipSizeParam);
    }

    private void removeTip() {
        if (tipRoot != null) {
            tipRoot.remove();
            tipRoot = null;
        }
    }

    private void removePanel() {
        if (panelRoot != null) {
            panelRoot.remove();
            panelRoot = null;
        }
    }

    @Override
    public void dispose() {
        removeTip();
        removePanel();
    }
}


