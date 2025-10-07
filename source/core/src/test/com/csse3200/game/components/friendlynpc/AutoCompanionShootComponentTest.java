// src/test/java/com/csse3200/game/components/friendlynpc/AutoCompanionShootComponentTest.java
package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AutoCompanionShootComponent.
 */
class AutoCompanionShootComponentTest {

    private EntityService entityService;
    private GameArea gameArea;
    private GameTime time;
    private Entity player;
    private Entity self; // 挂载组件的实体
    private PlayerActions playerActions;
    private WeaponsStatsComponent weaponStats;

    @BeforeEach
    void setup() {
        entityService = mock(EntityService.class);
        gameArea      = mock(GameArea.class);
        time          = mock(GameTime.class);
        player        = mock(Entity.class);
        self          = mock(Entity.class);
        playerActions = mock(PlayerActions.class);
        weaponStats   = mock(WeaponsStatsComponent.class);

        // 默认返回“空实体列表”以避免 findTarget() NPE
        when(entityService.getEntities()).thenReturn(new Array<>());

        // 自身位置 & 事件
        when(self.getCenterPosition()).thenReturn(new Vector2(3f, 4f));
        when(self.getPosition()).thenReturn(new Vector2(3f, 4f));
        var events = mock(com.csse3200.game.events.EventHandler.class);
        when(self.getEvents()).thenReturn(events);

        // 玩家 → PlayerActions → 武器参数
        when(player.getComponent(PlayerActions.class)).thenReturn(playerActions);
        when(playerActions.getCurrentWeaponStats()).thenReturn(weaponStats);

        // 时间步长：等于扫描周期，保证每帧会尝试扫描
        when(time.getDeltaTime()).thenReturn(0.12f);
    }

    /** 统一 mock ServiceLocator 静态方法 */
    private MockedStatic<ServiceLocator> mockServices(boolean provideGameArea) {
        MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
        sl.when(ServiceLocator::getEntityService).thenReturn(entityService);
        sl.when(ServiceLocator::getPlayer).thenReturn(player);
        sl.when(ServiceLocator::getTimeSource).thenReturn(time);
        if (provideGameArea) {
            sl.when(ServiceLocator::getGameArea).thenReturn(gameArea);
        } else {
            sl.when(ServiceLocator::getGameArea).thenReturn(null);
        }
        return sl;
    }

    /** 生成一个有效的 NPC 敌人 */
    private Entity makeEnemy(Vector2 center, boolean alive) {
        Entity enemy = mock(Entity.class);
        HitboxComponent hb = mock(HitboxComponent.class);
        when(hb.getLayer()).thenReturn(PhysicsLayer.NPC);
        when(enemy.getComponent(HitboxComponent.class)).thenReturn(hb);

        CombatStatsComponent cs = mock(CombatStatsComponent.class);
        when(cs.getHealth()).thenReturn(alive ? 10 : 0);
        when(enemy.getComponent(CombatStatsComponent.class)).thenReturn(cs);

        when(enemy.getCenterPosition()).thenReturn(center);
        return enemy;
    }

    /** 覆盖默认空列表，注入指定实体集合 */
    private void supplyEntities(Entity... entities) {
        Array<Entity> arr = new Array<>();
        for (Entity e : entities) arr.add(e);
        when(entityService.getEntities()).thenReturn(arr);
    }

    @Test
    void create_bindsPlayerFromServiceLocator() {
        try (var sl = mockServices(true)) {
            AutoCompanionShootComponent c = new AutoCompanionShootComponent();
            c.setEntity(self);
            c.create();

            // 只验证：update 时会调用时间源（说明 boundPlayer 非空并进入正常流程）
            c.update();
            verify(time, atLeastOnce()).getDeltaTime();
        }
    }

    @Test
    void update_firesAtNearestEnemy_spawnsViaGameArea_andSetsCooldown() {
        // 最近的存活敌人（向右 2m）
        Entity enemyNear = makeEnemy(new Vector2(5f, 4f), true);
        // 列表包含 self 与 player（会被 isEnemy 排除），以及 1 个敌人
        supplyEntities(self, player, enemyNear);

        // 子弹与弹道
        Entity bullet = mock(Entity.class);
        when(bullet.getScale()).thenReturn(new Vector2(1f, 1f));
        PhysicsProjectileComponent proj = mock(PhysicsProjectileComponent.class);
        when(bullet.getComponent(PhysicsProjectileComponent.class)).thenReturn(proj);

        try (var sl = mockServices(true);
             MockedStatic<ProjectileFactory> pf = mockStatic(ProjectileFactory.class)) {

            pf.when(() -> ProjectileFactory.createFireballBullet(weaponStats)).thenReturn(bullet);

            AutoCompanionShootComponent c = new AutoCompanionShootComponent();
            c.setEntity(self);
            c.create();

            // 第一次 update 会扫描并开火
            c.update();

            pf.verify(() -> ProjectileFactory.createFireballBullet(weaponStats), times(1));
            verify(bullet, times(1)).setPosition(eq(3f - 0.5f), eq(4f - 0.5f)); // 居中减半尺寸
            verify(gameArea, times(1)).spawnEntity(bullet);

            // 方向应为 (5,4)-(3,4)=(2,0)，速度=5
            verify(proj, times(1)).fire(argThat(v -> v.epsilonEquals(new Vector2(2f, 0f), 1e-5f)), eq(5f));
            verify(self.getEvents(), times(1)).trigger("fired");

            // 冷却生效：下一次 update 不再触发开火
            c.update();
            pf.verifyNoMoreInteractions();
            verify(proj, times(1)).fire(any(), anyFloat());
        }
    }

    @Test
    void update_fires_whenGameAreaNull_registersViaEntityService() {
        Entity enemyNear = makeEnemy(new Vector2(2f, 4f), true);
        supplyEntities(self, player, enemyNear);

        Entity bullet = mock(Entity.class);
        when(bullet.getScale()).thenReturn(new Vector2(1f, 1f));
        PhysicsProjectileComponent proj = mock(PhysicsProjectileComponent.class);
        when(bullet.getComponent(PhysicsProjectileComponent.class)).thenReturn(proj);

        try (var sl = mockServices(false);
             MockedStatic<ProjectileFactory> pf = mockStatic(ProjectileFactory.class)) {

            pf.when(() -> ProjectileFactory.createFireballBullet(weaponStats)).thenReturn(bullet);

            AutoCompanionShootComponent c = new AutoCompanionShootComponent();
            c.setEntity(self);
            c.create();
            c.update();

            // 无 GameArea 时，走 EntityService.register
            verify(entityService, times(1)).register(bullet);
            verify(proj, times(1)).fire(any(), eq(5f));
        }
    }

    @Test
    void update_doesNothing_whenAttackDisabled_orNoTarget() {
        // 情况1：attack=false，直接返回
        try (var sl = mockServices(true)) {
            AutoCompanionShootComponent c1 = new AutoCompanionShootComponent();
            c1.setEntity(self);
            c1.create();
            c1.setAttack(false);
            c1.update();

            verifyNoInteractions(gameArea);
            verify(entityService, never()).register(any());
        }

        // 情况2：目标无效（敌人死亡）→ 不开火
        Entity enemyDead = makeEnemy(new Vector2(4f, 4f), false);
        supplyEntities(self, player, enemyDead);

        try (var sl = mockServices(true);
             MockedStatic<ProjectileFactory> pf = mockStatic(ProjectileFactory.class)) {

            AutoCompanionShootComponent c2 = new AutoCompanionShootComponent();
            c2.setEntity(self);
            c2.create();
            c2.update();

            pf.verifyNoInteractions();
            verify(gameArea, never()).spawnEntity(any());
            verify(entityService, never()).register(any());
        }
    }
}


