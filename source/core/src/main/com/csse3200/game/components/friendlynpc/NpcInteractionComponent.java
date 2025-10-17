package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

import java.awt.*;

public class NpcInteractionComponent extends Component {
    private boolean active = false;

    DialogueDisplay ui;
    NpcDialogueDataComponent data;

    @Override
    public void create() {
         ui = entity.getComponent(DialogueDisplay.class);
         data = entity.getComponent(NpcDialogueDataComponent.class);
         entity.getEvents().addListener("interact", this::handleInteract);
         entity.getEvents().addListener("enteredInteractRadius", this::playerEnteredRange);
         entity.getEvents().addListener("exitedInteractRadius", this::playerLeftRange);
    }

    private void handleInteract(){
        if (!active) {
            hideLabel();
            ui.bindData(data);
            ui.showFirst();
            active = true;
            entity.getEvents().trigger("npcDialogueStart");
            Label interactLabel = ServiceLocator.getPrompt();
            interactLabel.setVisible(false);

        } else {
            showLabel();
            safelyCloseUI();
        }
    }

    // This is a messy way to display this label but oh well
    private void playerEnteredRange(){
        showLabel();
    }

    private void playerLeftRange(){
        hideLabel();
        safelyCloseUI();
    }

    private void showLabel(){
        Label interactLabel = ServiceLocator.getPrompt();
        interactLabel.setText("Press E to interact with NPC");
        interactLabel.setVisible(true);
    }

    private void hideLabel(){
        Label interactLabel = ServiceLocator.getPrompt();
        interactLabel.setVisible(false);
    }

    private void safelyCloseUI() {
        if (active) {
            ui.hide();
            entity.getEvents().trigger("npcDialogueEnd");
            active = false;
        }
    }
}