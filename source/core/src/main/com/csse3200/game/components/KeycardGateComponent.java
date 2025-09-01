package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

public class KeycardGateComponent extends Component {
    private final int requiredLevel;
    private boolean unlocked = false;

    public KeycardGateComponent(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        ServiceLocator.getGlobalEvents().addListener("keycard_lvl" + requiredLevel + "_collected", this::unlock);
            }

    private void onCollisionStart(Entity me, Entity other) {
        if (!unlocked) {
            // Optional: show UI message "Requires Keycard Level " + requiredLevel
        } else {
            // Trigger transition logic here
        }
    }

         public void unlock() {
            Gdx.app.log("KeycardGate", "Unlock triggered for level " + requiredLevel);

            if (entity == null) {
                Gdx.app.error("KeycardGate", "Entity is null");
                return;
            }

            PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
            if (physics == null) {
                Gdx.app.error("KeycardGate", "Missing PhysicsComponent on gate entity");
                return;
            }


            ColliderComponent collider = entity.getComponent(ColliderComponent.class);
            if (collider != null) {
                collider.setSensor(true); // disables physical blocking
                Gdx.app.log("KeycardGate", "Gate collider set to sensor");
            }


            // Optional: Add visual or logical unlock behavior here
    }
}