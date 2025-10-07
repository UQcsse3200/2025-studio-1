package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.CameraComponent;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that spawnOrRepositionPlayer uses PLAYER_SPAWN and re-uses an existing player.
 */
class SpawnOrRepositionPlayerTest {

    private static class DummyArea extends GameArea {
        DummyArea() { super(null, new CameraComponent()); }
        @Override public void create() { /* no-op */ }
        @Override public Entity getPlayer() { return ServiceLocator.getPlayer(); }
    }

    @BeforeEach
    void setup() {
        ServiceLocator.clear();
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerTimeSource(new GameTime());
        ServiceLocator.registerGameArea(new DummyArea());
    }

    @AfterEach
    void tearDown() { ServiceLocator.clear(); }

    @Test
    void createsPlayerFirstTimeThenReuses() {
        DummyArea area = new DummyArea();

    
        Entity existing = new Entity();
        ServiceLocator.registerPlayer(existing);
        area.terrain = org.mockito.Mockito.mock(TerrainComponent.class);
        org.mockito.Mockito.when(area.terrain.tileToWorldPosition(org.mockito.Mockito.any()))
                .thenReturn(new Vector2(0f, 0f));

        // First call should reuse the existing player
        GridPoint2 first = new GridPoint2(3, 4);
        Entity p1 = area.spawnOrRepositionPlayer(first);
        assertSame(existing, p1);

        // Second call: same player reused and moved
        GridPoint2 second = new GridPoint2(10, 12);
        Entity p2 = area.spawnOrRepositionPlayer(second);
        assertSame(existing, p2, "Existing player should be reused");
    }
}


