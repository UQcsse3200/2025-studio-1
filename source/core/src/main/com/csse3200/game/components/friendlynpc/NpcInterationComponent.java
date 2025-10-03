package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

public class NpcInterationComponent extends Component {
    private final Entity player;
    private final float triggerDist;
    private final int interactKey;

    private boolean consumedEver = false;

    public NpcInterationComponent(Entity player, float triggerDist) {
        this(player, triggerDist, Input.Keys.F);
    }

    public NpcInterationComponent(Entity player, float triggerDist, int interactKey) {
        this.player = player;
        this.triggerDist = triggerDist;
        this.interactKey = interactKey;
    }

    @Override
    public void update() {
        DialogueDisplay ui = entity.getComponent(DialogueDisplay.class);
        NpcDialogueDataComponent data = entity.getComponent(NpcDialogueDataComponent.class);
        float dist = entity.getPosition().dst(player.getPosition());
        if (dist > triggerDist) {
            ui.hide();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            ui.hide();
            entity.getEvents().trigger("npcDialogueEnd");
            return;
        }

        if (!consumedEver && Gdx.input.isKeyJustPressed(interactKey)) {
            ui.bindData(data);
            ui.showFirst();
            consumedEver = true;
            entity.getEvents().trigger("npcDialogueStart");
        }
    }
}