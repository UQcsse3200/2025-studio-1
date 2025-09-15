package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.stations.StationComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InteractableStationFactoryTest {

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());

        /* Mock our ResourceService for the textures */
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        Texture mockTexture = mock(Texture.class);
        when(mockTexture.getHeight()).thenReturn(10);
        when(mockResourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
        ServiceLocator.registerResourceService(mockResourceService);
    }

    void baseComponentsAssertion(Entity e) {
        assertNotNull(e.getComponent(PhysicsComponent.class));
        assertNotNull(e.getComponent(ColliderComponent.class));
    }

    @Test
    void shouldCreateBase() {

        Entity e = InteractableStationFactory.createBaseStation();
        this.baseComponentsAssertion(e);
    }

    @Test
    void shouldCreateComputer() {

        Entity e = InteractableStationFactory.createComputerBench();
        this.baseComponentsAssertion(e);
        assertNotNull(e.getComponent(StationComponent.class));
    }
}
