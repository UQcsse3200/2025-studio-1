// src/test/java/com/csse3200/game/components/friendlynpc/CompanionFollowShootComponentTest.java
package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.events.listeners.EventListener2;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanionFollowShootComponentTest {

    private Entity self;           // 组件挂载的实体
    private Entity player1;        // 初始玩家
    private Entity player2;        // 新玩家（用于重绑场景）
    private EventHandler selfEvents;
    private EventHandler player1Events;
    private EventHandler player2Events;

    private PlayerActions playerActions;
    private WeaponsStatsComponent weaponStats;

    private EntityService entityService;
    private GameArea gameArea;
    private GameTime time;

    @BeforeEach
    void setup() {
        self = mock(Entity.class);
        player1 = mock(Entity.class);
        player2 = mock(Entity.class);

        selfEvents = mock(EventHandler.class);
        player1Events = mock(EventHandler.class);
        player2Events = mock(EventHandler.class);

        when(self.getEvents()).thenReturn(selfEvents);
        when(player1.getEvents()).thenReturn(player1Events);
        when(player2.getEvents()).thenReturn(player2Events);

        // 自身中心点
        when(self.getCenterPosition()).thenReturn(new Vector2(3f, 4f));

        // 玩家动作与武器
        playerActions = mock(PlayerActions.class);
        weaponStats = mock(WeaponsStatsComponent.class);
        when(player1.getComponent(PlayerActions.class)).thenReturn(playerActions);
        when(player2.getComponent(PlayerActions.class)).thenReturn(playerActions);
        when(playerActions.getCurrentWeaponStats()).thenReturn(weaponStats);

        // 服务
        entityService = mock(EntityService.class);
        gameArea = mock(GameArea.class);
        time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(0.016f); // 默认 60fps 的 dt
    }

    /** 统一 mock ServiceLocator 静态方法 */
    private MockedStatic<ServiceLocator> mockServices(Entity currentPlayer, boolean areaPresent) {
        MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
        sl.when(ServiceLocator::getPlayer).thenReturn(currentPlayer);
        sl.when(ServiceLocator::getEntityService).thenReturn(entityService);
        sl.when(ServiceLocator::getTimeSource).thenReturn(time);
        if (areaPresent) {
            sl.when(ServiceLocator::getGameArea).thenReturn(gameArea);
        } else {
            sl.when(ServiceLocator::getGameArea).thenReturn(null);
        }
        return sl;
    }

    /** 构造一发可用的子弹（带 PhysicsProjectileComponent） */
    private static class BulletPack {
        final Entity bullet = mock(Entity.class);
        final PhysicsProjectileComponent proj = mock(PhysicsProjectileComponent.class);
        BulletPack() {
            when(bullet.getScale()).thenReturn(new Vector2(1f, 1f));
            when(bullet.getComponent(PhysicsProjectileComponent.class)).thenReturn(proj);
        }
    }

    @Test
    void create_bindsListener_toCurrentPlayer_and_rebindsOnPlayerChange() {
        try (var sl = mockServices(player1, true)) {
            CompanionFollowShootComponent c = new CompanionFollowShootComponent();
            c.setEntity(self);

            // create() 时应绑定 player1 的监听
            c.create();
            verify(player1Events, times(1))
                    .addListener(eq("player_shoot_order"), any(EventListener2.class));

            // 切换当前玩家，update() 应重绑一次
            sl.when(ServiceLocator::getPlayer).thenReturn(player2);
            c.update(); // 触发 tryBindToCurrentPlayer()
            verify(player2Events, times(1))
                    .addListener(eq("player_shoot_order"), any(EventListener2.class));
        }
    }

    @Test
    void onShootOrder_happyPath_spawnsViaGameArea_setsCooldown_andFiresEvent() {
        // 捕获监听器
        ArgumentCaptor<EventListener2<Vector2, Vector2>> cap = ArgumentCaptor.forClass(EventListener2.class);

        try (var sl = mockServices(player1, true);
             MockedStatic<ProjectileFactory> pf = mockStatic(ProjectileFactory.class)) {

            // 组件
            CompanionFollowShootComponent c = new CompanionFollowShootComponent();
            c.setEntity(self);
            c.setAttack(true);     // 允许攻击
            c.cooldown(0.25f);     // 冷却 0.25s
            c.create();

            // 捕获 addListener 注册的回调
            verify(player1Events).addListener(eq("player_shoot_order"), cap.capture());
            EventListener2<Vector2, Vector2> listener = cap.getValue();
            assertNotNull(listener);

            // 造一发子弹
            BulletPack pack = new BulletPack();
            pf.when(() -> ProjectileFactory.createFireballBullet(weaponStats))
                    .thenReturn(pack.bullet);

            // world 与 from(3,4)不同 → 走“朝 world 方向”分支
            Vector2 world = new Vector2(5f, 4f); // dir = (2, 0)
            Vector2 playerDir = new Vector2(-1f, 0f); // 不会用到

            // 触发射击
            listener.handle(world, playerDir);

            // 验证：走 GameArea 分支
            verify(gameArea, times(1)).spawnEntity(pack.bullet);
            // fire 调用方向正确、速度=5
            verify(pack.proj, times(1))
                    .fire(argThat(v -> v.epsilonEquals(new Vector2(2f, 0f), 1e-6f)), eq(5f));
            // 触发 fired 事件
            verify(selfEvents, times(1)).trigger("fired");

            // 冷却期间再次触发，不应再发射
            listener.handle(world, playerDir);
            verify(gameArea, times(1)).spawnEntity(any()); // 仍为 1 次
            verify(pack.proj, times(1)).fire(any(), anyFloat());

            // 经过时间 < 冷却，仍不该发射
            when(time.getDeltaTime()).thenReturn(0.1f);
            c.update();
            listener.handle(world, playerDir);
            verify(pack.proj, times(1)).fire(any(), anyFloat());

            // 再过时间让冷却结束
            when(time.getDeltaTime()).thenReturn(0.2f);
            c.update();
            // 再触发一次应能发射
            BulletPack pack2 = new BulletPack();
            pf.when(() -> ProjectileFactory.createFireballBullet(weaponStats))
                    .thenReturn(pack2.bullet);
            listener.handle(world, playerDir);
            verify(gameArea, times(2)).spawnEntity(any());
            verify(pack2.proj, times(1)).fire(any(), eq(5f));
        }
    }

    @Test
    void onShootOrder_whenNoGameArea_registersViaEntityService_andZeroDirFallsBackToPlayerDir() {
        // 捕获监听器
        ArgumentCaptor<EventListener2<Vector2, Vector2>> cap = ArgumentCaptor.forClass(EventListener2.class);

        try (var sl = mockServices(player1, false);  // GameArea = null
             MockedStatic<ProjectileFactory> pf = mockStatic(ProjectileFactory.class)) {

            CompanionFollowShootComponent c = new CompanionFollowShootComponent();
            c.setEntity(self);
            c.setAttack(true);
            c.create();

            verify(player1Events).addListener(eq("player_shoot_order"), cap.capture());
            EventListener2<Vector2, Vector2> listener = cap.getValue();

            // 子弹
            BulletPack pack = new BulletPack();
            pf.when(() -> ProjectileFactory.createFireballBullet(weaponStats))
                    .thenReturn(pack.bullet);

            // world == from → dirToSameWorld = (0,0)，应回退到 playerDir
            Vector2 world = new Vector2(3f, 4f);
            Vector2 playerDir = new Vector2(0f, 1f);

            listener.handle(world, playerDir);

            // 走 EntityService.register 分支
            verify(entityService, times(1)).register(pack.bullet);
            // fire 使用了 playerDir
            verify(pack.proj, times(1))
                    .fire(argThat(v -> v.epsilonEquals(new Vector2(0f, 1f), 1e-6f)), eq(5f));
            verify(selfEvents, times(1)).trigger("fired");
        }
    }

    @Test
    void onShootOrder_earlyReturns_whenAttackDisabled_orNoStats() {
        ArgumentCaptor<EventListener2<Vector2, Vector2>> cap = ArgumentCaptor.forClass(EventListener2.class);

        try (var sl = mockServices(player1, true);
             MockedStatic<ProjectileFactory> pf = mockStatic(ProjectileFactory.class)) {

            // 1) attack=false → 直接返回
            CompanionFollowShootComponent c1 = new CompanionFollowShootComponent();
            c1.setEntity(self);
            c1.setAttack(false);
            c1.create();

            verify(player1Events).addListener(eq("player_shoot_order"), cap.capture());
            EventListener2<Vector2, Vector2> l1 = cap.getValue();
            l1.handle(new Vector2(10, 10), new Vector2(1, 0));
            pf.verifyNoInteractions();

            // 2) attack=true，但 stats 为 null → 直接返回
            //   先把 captor 清空
            clearInvocations(player1Events);
            // playerActions.getCurrentWeaponStats() 返回 null
            when(playerActions.getCurrentWeaponStats()).thenReturn(null);

            CompanionFollowShootComponent c2 = new CompanionFollowShootComponent();
            c2.setEntity(self);
            c2.setAttack(true);
            c2.create();

            verify(player1Events).addListener(eq("player_shoot_order"), cap.capture());
            EventListener2<Vector2, Vector2> l2 = cap.getValue();
            l2.handle(new Vector2(8, 8), new Vector2(0, 1));
            pf.verifyNoMoreInteractions();
        }
    }

    @Test
    void accessors_setAttack_and_fluentCooldown_work() {
        try (var sl = mockServices(player1, true)) {
            CompanionFollowShootComponent c = new CompanionFollowShootComponent();
            assertFalse(c.isAttack());
            c.setAttack(true);
            assertTrue(c.isAttack());

            // 链式设置（仅验返回 this，不抛异常）
            assertSame(c, c.cooldown(0.1f));
        }
    }
}

