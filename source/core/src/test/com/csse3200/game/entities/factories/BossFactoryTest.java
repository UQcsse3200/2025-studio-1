package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BossFactoryTest {

    @Test
    void createBoss3_shouldAddCoreComponentsAndAttacks() {
        // Arrange: a minimal "player" entity to act as target
        Entity player = new Entity();

        // Act
        Entity boss = BossFactory.createBoss3(player);

        // Assert: core components exist
        assertNotNull(boss.getComponent(PhysicsComponent.class), "PhysicsComponent missing");
        assertNotNull(boss.getComponent(ColliderComponent.class), "ColliderComponent missing");
        HitboxComponent hitbox = boss.getComponent(HitboxComponent.class);
        assertNotNull(hitbox, "HitboxComponent missing");
        assertEquals(PhysicsLayer.NPC, hitbox.getLayer(), "Hitbox layer should be NPC");

        // Rendering & scaling
        TextureRenderComponent tex = boss.getComponent(TextureRenderComponent.class);
        assertNotNull(tex, "TextureRenderComponent missing (Boss_3.png should be set)");

        // If Entity exposes scale, verify it was set to (2f,2f)
        // (Most versions of the template do; adjust/remove if your Entity lacks getScale)
        Vector2 scale = boss.getScale();
        assertNotNull(scale, "Entity scale should be non-null");
        assertEquals(2f, scale.x, 0.0001, "Scale X should be 2f");
        assertEquals(2f, scale.y, 0.0001, "Scale Y should be 2f");

        // Combat stats exist (we don't assert exact values to avoid coupling to configs file)
        assertNotNull(boss.getComponent(CombatStatsComponent.class), "CombatStatsComponent missing");

        // Enemy attack components present
        assertNotNull(
                boss.getComponent(com.csse3200.game.components.enemy.EnemyMudBallAttackComponent.class),
                "EnemyMudBallAttackComponent missing"
        );
        assertNotNull(
                boss.getComponent(com.csse3200.game.components.enemy.EnemyMudRingSprayComponent.class),
                "EnemyMudRingSprayComponent missing"
        );
    }
}
