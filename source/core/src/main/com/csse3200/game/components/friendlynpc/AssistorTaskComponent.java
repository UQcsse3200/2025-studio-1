package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
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
        final float x = entity.getPosition().x;
        final float y = entity.getPosition().y;
        Gdx.app.postRunnable(() -> {
            Entity partner = FriendlyNPCFactory.createPartner(player);
            partner.setPosition(x, y);
            ServiceLocator.getEntityService().register(partner);
        });
    }
}