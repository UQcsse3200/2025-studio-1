package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;

public class AssistorTaskComponent extends Component {

    private final Entity player;

    public AssistorTaskComponent(Entity player) {
        this.player = player;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("npcDialogueEnd", this::onDialogueEnd);
    }
    private void onDialogueEnd() {
        Entity partner = FriendlyNPCFactory.createPartner(player);
        partner.setPosition(entity.getPosition().x, entity.getPosition().y);
        ServiceLocator.getEntityService().register(partner);
    }
}