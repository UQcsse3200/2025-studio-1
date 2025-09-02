package com.csse3200.game.physics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class PhysicsProjectileComponentTest {
    @Mock PhysicsEngine engine;
    @Mock Body body;

    @BeforeEach
    void beforeEach() {
        when(engine.createBody(any())).thenReturn(body);
        PhysicsService service = new PhysicsService(engine);
        ServiceLocator.registerPhysicsService(service);
    }

    @Test
    void correctVelocity() {
        Entity entity = new Entity();
        PhysicsComponent physicsComponent = new PhysicsComponent();
        PhysicsProjectileComponent projectileComponent = new PhysicsProjectileComponent();
        entity.addComponent(physicsComponent);
        entity.addComponent(projectileComponent);
        entity.create();

        entity.getComponent(PhysicsProjectileComponent.class)
                        .fire(new Vector2(1, 0), 3f);

        verify(body).setLinearVelocity(new Vector2(3f, 0));
    }

}
