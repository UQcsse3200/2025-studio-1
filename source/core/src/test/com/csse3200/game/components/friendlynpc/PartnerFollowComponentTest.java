// src/test/java/com/csse3200/game/components/friendlynpc/PartnerFollowComponentTest.java
package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.friendlynpc.PartnerFollowComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PartnerFollowComponentTest {

    /** 辅助：mock 出一个固定 dt 的 GameTime */
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

        // 不应去 setPosition
        verify(self, never()).setPosition(anyFloat(), anyFloat());
    }

    @Test
    void update_returnsEarly_whenMoveDisabled() {
        Entity self = mock(Entity.class);
        Entity player = mock(Entity.class);

        when(self.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(3f, 0f)); // 距离 3，在移动区间

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);
        c.setMove(false); // 关闭移动

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
        when(player.getPosition()).thenReturn(new Vector2(6f, 0f)); // 距离 6 > 5

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);

        try (MockedStatic<ServiceLocator> ignored = mockDt(0.1f)) {
            c.update();
        }

        // 断言：只瞬移一次到 (player + 0.8, 0)
        verify(self, times(1)).setPosition(6f + 0.8f, 0f);

        // ✅ 不要再调用 verifyNoMoreInteractions(self)，
        // 因为 getPosition() 等合法交互也会被记录，导致失败。
    }

    @Test
    void update_stops_whenDistanceLessOrEqual1() {
        Entity self = mock(Entity.class);
        Entity player = mock(Entity.class);

        when(self.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(0.6f, 0.8f)); // 距离 = 1.0

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);

        try (MockedStatic<ServiceLocator> ignored = mockDt(0.1f)) {
            c.update();
        }

        // 在停止半径内不移动
        verify(self, never()).setPosition(anyFloat(), anyFloat());
    }

    @Test
    void update_movesTowardPlayer_between1and5_withDtFromServiceLocator() {
        Entity self = mock(Entity.class);
        Entity player = mock(Entity.class);

        when(self.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(3f, 0f)); // 距离 3 ∈ (1,5]

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);

        // dt = 0.1 → SPEED(8) * dt = 0.8，方向 (1,0)，新位置应为 (0.8, 0)
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
        when(player.getPosition()).thenReturn(new Vector2(3f, 0f)); // 距离 3

        PartnerFollowComponent c = new PartnerFollowComponent(player);
        c.setEntity(self);

        // 让 ServiceLocator.getTimeSource() 抛异常 → 应使用默认 dt = 0.016
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getTimeSource).thenThrow(new RuntimeException("no time"));
            c.update();
        }

        // 期望位移 = 8 * 0.016 = 0.128，方向 (1,0)
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
