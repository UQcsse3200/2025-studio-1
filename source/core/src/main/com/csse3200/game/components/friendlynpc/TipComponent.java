package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.services.ServiceLocator;

public class TipComponent extends Component {
    private final Entity npc;
    private final Entity player;
    private final float triggerDist;
    private Entity tip;
    private boolean dialogueEnded = false;

    public TipComponent(Entity npc, Entity player, float triggerDist) {
        this.npc = npc;
        this.player = player;
        this.triggerDist = triggerDist;
    }

    @Override
    public void create() {
        super.create();
        npc.getEvents().addListener("npcDialogueEnd", this::onDialogueEnd);
    }

    private void onDialogueEnd() {
        dialogueEnded = true;
        destroyTipIfPresent();
    }

    @Override
    public void update() {
        if (dialogueEnded) {
            return;
        }
        float d = npc.getPosition().dst(player.getPosition());

        if (d <= triggerDist && tip == null) {
            tip = FriendlyNPCFactory.createTip();
            tip.setPosition(npc.getPosition().x, npc.getPosition().y + 1f);

            // spawn via GameArea so it is tied to the room lifecycle
            ServiceLocator.getGameArea().spawnEntity(tip);
        }

        if (d > triggerDist && tip != null) {
            destroyTipIfPresent();
        }
    }

    private void destroyTipIfPresent() {
        if (tip != null) {
            // Unregister and dispose safely
            try {
                ServiceLocator.getEntityService().unregister(tip);
            } catch (Exception ignored) { // safe guard }
            try {
                tip.dispose();
            } catch (Exception ignored) { // safe guard }
            tip = null;
        }
    }

    @Override
    public void dispose() {
        destroyTipIfPresent();

    }
}
