package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;

public class NpcInteractionComponent extends Component {
    private boolean active = false;

    DialogueDisplay ui;
    NpcDialogueDataComponent data;

    @Override
    public void create() {
         ui = entity.getComponent(DialogueDisplay.class);
         data = entity.getComponent(NpcDialogueDataComponent.class);
         entity.getEvents().addListener("interact", this::handleInteract);
         entity.getEvents().addListener("exitedInteractRadius", this::safelyCloseUI);
    }

    private void handleInteract(){
        if (!active) {
            ui.bindData(data);
            ui.showFirst();
            active = true;
            entity.getEvents().trigger("npcDialogueStart");

        } else {
            safelyCloseUI();
        }
    }

    private void safelyCloseUI() {
        if (active) {
            ui.hide();
            entity.getEvents().trigger("npcDialogueEnd");
            active = false;
        }
    }
}