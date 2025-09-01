package com.csse3200.game.entities.factories;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.components.KeycardPickupComponent;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class KeycardFactory {

    public static Entity createKeycard(int level) {
        Entity keycard = new Entity()
                .addComponent(new TextureRenderComponent("images/keycard_lvl" + level + ".png"))
                .addComponent(new PhysicsComponent()) // ✅ Required for collision
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.ITEM))
                .addComponent(new KeycardPickupComponent(level));

        // ✅ Scale the collider so it has a physical shape
        com.csse3200.game.physics.PhysicsUtils.setScaledCollider(keycard, 0.5f, 0.5f);

        return keycard;
    }

    public static List<Entity> createAllKeycards() {
        List<Entity> keycards = new ArrayList<>();
        for (int level = 1; level <= 4; level++) {
            keycards.add(createKeycard(level));
        }
        return keycards;
    }

    public static Entity createRandomKeycard() {
        int level = MathUtils.random(1, 4);
        return createKeycard(level);
    }
}