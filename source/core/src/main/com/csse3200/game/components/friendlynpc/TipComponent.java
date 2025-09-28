package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;


public class TipComponent extends Component {
    private final Entity npc;
    private final Entity player;
    private final float triggerDist;

    private Entity tip;

    public TipComponent(Entity npc, Entity player, float triggerDist) {
        this.npc = npc;
        this.player = player;
        this.triggerDist = triggerDist;
    }

    @Override
    public void update() {
        float d = npc.getPosition().dst(player.getPosition());
        if (d <= triggerDist && tip == null) {
            tip = new Entity().addComponent(new TextureRenderComponent("images/!.png"));
            tip.getComponent(TextureRenderComponent.class).scaleEntity();
            tip.setPosition(npc.getPosition().x, npc.getPosition().y + 1f);
            ServiceLocator.getEntityService().register(tip);
        }

        if (d > triggerDist && tip != null) {
            ServiceLocator.getEntityService().unregister(tip);
            tip.dispose();
            tip = null;
        }
    }
}