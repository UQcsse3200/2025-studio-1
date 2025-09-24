package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * Handles "interact" event triggered by pressing E as seen in KeyboardPlayerInputComponent
 */
public class InteractComponent extends Component {

    @Override
    public void create() {
        entity.getEvents().addListener("interact", this::attemptInteract);
    }

    private void attemptInteract() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            if (!e.isInteractable()) {
                continue;
            }

            float distanceFromPlayer = entity.getCenterPosition().dst(e.getCenterPosition());

            float INTERACT_RANGE = 2.0f;
            if (distanceFromPlayer <= INTERACT_RANGE) {
                e.getEvents().trigger("interact");
            }
        }
    }
}
