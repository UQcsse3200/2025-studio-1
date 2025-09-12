package com.csse3200.game.entities.factories;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.components.KeycardPickupComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;
/**
 * Factory class for creating keycard entities used in gated access gameplay.
 * Provides methods to create specific, random, or all keycard levels with appropriate components.
 */

public class KeycardFactory {


    public static Entity createKeycard(int level) {
        /** Creates a keycard entity with the specified level, physics, rendering, and pickup logic.*/
        Entity keycard = new Entity()
                .addComponent(new TextureRenderComponent("images/keycard_lvl" + level + ".png"))
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new KeycardPickupComponent(level));


        com.csse3200.game.physics.PhysicsUtils.setScaledCollider(keycard, 0.5f, 0.5f);
       // keycard.getComponent(PhysicsComponent.class).BodyUserData(keycard);
        return keycard;
    }

    public static List<Entity> createAllKeycards() {
        /** Generates a list of keycard entities for levels 1 through 4.*/
        List<Entity> keycards = new ArrayList<>();
        for (int level = 1; level <= 4; level++) {
            keycards.add(createKeycard(level));
        }
        return keycards;
    }

    public static Entity createRandomKeycard() {
        //not needed for this sprint just for future refrences
        int level = MathUtils.random(1, 4);
        return createKeycard(level);
    }
}