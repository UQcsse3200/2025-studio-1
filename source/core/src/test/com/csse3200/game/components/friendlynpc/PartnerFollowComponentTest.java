package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PartnerFollowComponent:
 * - Early returns when player is null or movement disabled
 * - Teleport when distance > 5
 * - Stop when distance <= 1
 * - Move toward player when distance in (1, 5]
 * - Uses ServiceLocator time dt, and falls back to default dt on exception
 * - Accessor methods for move flag
 */
class PartnerFollowComponentTest {

    /** Helper: mock a GameTime with fixed dt and wire it via ServiceLocator */
    private static MockedStatic<ServiceLocator> mockDt(float dt) {
        GameTime time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(dt);
        MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
        sl.when(ServiceLocator::getTimeSource).thenReturn(time);
        return sl;
    }

    @Test
    void update_returnsEarly_whenPlayerIsNull() {
        Entity self = mock(Entity.class);

        PartnerFollowComponent c = new PartnerFollowComponent(null);
        c.setEntity(self);

        try (MockedStatic<ServiceLocator> ignored = mockDt(0.1f)) {
            c.update();
        }

        // No movement when player is null
        verify(self, never()).setPosition(anyFloat(), anyFloat());
    }

    @Test
    void update_returnsEarly_whenMoveDisabled() {
        Entity self = mock(Entity.class);
        Entity player = mock(Entity.class);

        when(self.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(3f, 0f)); // distance 3 (inside movement band)

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);
        c.setMove(false); // disable movement

        try (MockedStatic<ServiceLocator> ignored = mockDt(0.1f)) {
            c.update();
        }

        verify(self, never()).setPosition(anyFloat(), anyFloat());
    }

    @Test
    void update_teleports_whenDistanceGreaterThan5() {
        Entity self = mock(Entity.class);
        Entity player = mock(Entity.class);

        when(self.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(6f, 0f)); // distance 6 > 5

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);

        try (MockedStatic<ServiceLocator> ignored = mockDt(0.1f)) {
            c.update();
        }

        // Teleport exactly once to (player + 0.8, 0)
        verify(self, times(1)).setPosition(6f + 0.8f, 0f);

        // Do NOT call verifyNoMoreInteractions(self) here — legitimate calls like getPosition()
        // would also be counted and cause a failure.
    }

    @Test
    void update_stops_whenDistanceLessOrEqual1() {
        Entity self = mock(Entity.class);
        Entity player = mock(Entity.class);

        when(self.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(0.6f, 0.8f)); // distance = 1.0

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);

        try (MockedStatic<ServiceLocator> ignored = mockDt(0.1f)) {
            c.update();
        }

        // Inside stop radius: no movement
        verify(self, never()).setPosition(anyFloat(), anyFloat());
    }

    @Test
    void update_movesTowardPlayer_between1and5_withDtFromServiceLocator() {
        Entity self = mock(Entity.class);
        Entity player = mock(Entity.class);

        when(self.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(3f, 0f)); // distance 3 ∈ (1,5]

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);

        // dt = 0.1 -> SPEED(8) * dt = 0.8; direction (1,0) -> new position should be (0.8, 0)
        try (MockedStatic<ServiceLocator> ignored = mockDt(0.1f)) {
            c.update();
        }

        verify(self, times(1)).setPosition(0.8f, 0f);
    }

    @Test
    void update_usesDefaultDtWhenExceptionThrown() {
        Entity self = mock(Entity.class);
        Entity player = mock(Entity.class);

        when(self.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(3f, 0f)); // distance 3

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);

        // Force ServiceLocator.getTimeSource() to throw -> component should use default dt = 0.016
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getTimeSource).thenThrow(new RuntimeException("no time"));
            c.update();
        }

        // Expected displacement = 8 * 0.016 = 0.128, direction (1,0)
        verify(self, times(1)).setPosition(0.128f, 0f);
    }

    @Test
    void accessors_setMove_and_isMove() {
        Entity self = mock(Entity.class);
        PartnerFollowComponent c = new PartnerFollowComponent(mock(Entity.class));
        c.setEntity(self);

        assertTrue(c.isMove());
        c.setMove(false);
        assertFalse(c.isMove());
        c.setMove(true);
        assertTrue(c.isMove());
    }
}
