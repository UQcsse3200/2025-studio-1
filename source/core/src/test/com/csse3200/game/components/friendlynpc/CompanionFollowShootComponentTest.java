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

/**
 * Unit tests for CompanionFollowShootComponent.
 * Covers: binding to current player, re-binding on player change,
 * firing logic with/without GameArea, cooldown behavior, and early-returns.
 */
class CompanionFollowShootComponentTest {

    private Entity self;             // entity hosting the component
    private Entity player1;          // initial player
    private Entity player2;          // new player (to test rebind)
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

        // host entity world center
        when(self.getCenterPosition()).thenReturn(new Vector2(3f, 4f));

        // player action/weapon stats chain
        playerActions = mock(PlayerActions.class);
        weaponStats = mock(WeaponsStatsComponent.class);
        when(player1.getComponent(PlayerActions.class)).thenReturn(playerActions);
        when(player2.getComponent(PlayerActions.class)).thenReturn(playerActions);
        when(playerActions.getCurrentWeaponStats()).thenReturn(weaponStats);

        // services
        entityService = mock(EntityService.class);
        gameArea = mock(GameArea.class);
        time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(0.016f); // ~60fps dt
    }

    /** Helper to mock ServiceLocator. */
    private MockedStatic<ServiceLocator> mockServices(Entity currentPlayer, boolean areaPresent) {
        MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
        sl.when(ServiceLocator::getPlayer).thenReturn(currentPlayer);
        sl.when(ServiceLocator::getEntityService).thenReturn(entityService);
        sl.when(ServiceLocator::getTimeSource).thenReturn(time);
        sl.when(ServiceLocator::getGameArea).thenReturn(areaPresent ? gameArea : null);
        return sl;
    }

    /** Build a valid bullet pack (entity + PhysicsProjectileComponent). */
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

            // On create() it should bind to player1
            c.create();
            verify(player1Events, times(1))
                    .addListener(eq("player_shoot_order"), any(EventListener2.class));

            // Switch current player and call update() -> should rebind once
            sl.when(ServiceLocator::getPlayer).thenReturn(player2);
            c.update(); // triggers tryBindToCurrentPlayer()
            verify(player2Events, times(1))
                    .addListener(eq("player_shoot_order"), any(EventListener2.class));
        }
    }

    @Test
    void onShootOrder_happyPath_spawnsViaGameArea_setsCooldown_andFiresEvent() {
        // Capture the listener we register to the player's events
        ArgumentCaptor<EventListener2<Vector2, Vector2>> cap = ArgumentCaptor.forClass(EventListener2.class);

        try (var sl = mockServices(player1, true);
             MockedStatic<ProjectileFactory> pf = mockStatic(ProjectileFactory.class)) {

            // Component under test
            CompanionFollowShootComponent c = new CompanionFollowShootComponent();
            c.setEntity(self);
            c.setAttack(true);    // allow firing
            c.cooldown(0.25f);    // cooldown 0.25s
            c.create();

            // Grab the registered callback
            verify(player1Events).addListener(eq("player_shoot_order"), cap.capture());
            EventListener2<Vector2, Vector2> listener = cap.getValue();
            assertNotNull(listener);

            // Prepare a bullet
            BulletPack pack = new BulletPack();
            pf.when(() -> ProjectileFactory.createFireballBullet(weaponStats))
                    .thenReturn(pack.bullet);

            // world != from(3,4) -> direction should be world - from = (2,0)
            Vector2 world = new Vector2(5f, 4f);
            Vector2 playerDir = new Vector2(-1f, 0f); // not used in this branch

            // Fire once
            listener.handle(world, playerDir);

            // Uses GameArea path
            verify(gameArea, times(1)).spawnEntity(pack.bullet);
            // Direction and speed
            verify(pack.proj, times(1))
                    .fire(argThat(v -> v.epsilonEquals(new Vector2(2f, 0f), 1e-6f)), eq(5f));
            // Event fired on the host entity
            verify(selfEvents, times(1)).trigger("fired");

            // During cooldown, second call should not fire
            listener.handle(world, playerDir);
            verify(gameArea, times(1)).spawnEntity(any());
            verify(pack.proj, times(1)).fire(any(), anyFloat());

            // Advance time a bit (< cooldown): still blocked
            when(time.getDeltaTime()).thenReturn(0.1f);
            c.update();
            listener.handle(world, playerDir);
            verify(pack.proj, times(1)).fire(any(), anyFloat());

            // Advance enough to end cooldown
            when(time.getDeltaTime()).thenReturn(0.2f);
            c.update();

            // Fire again should work
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
        // Capture the listener
        ArgumentCaptor<EventListener2<Vector2, Vector2>> cap = ArgumentCaptor.forClass(EventListener2.class);

        try (var sl = mockServices(player1, false);  // GameArea = null
             MockedStatic<ProjectileFactory> pf = mockStatic(ProjectileFactory.class)) {

            CompanionFollowShootComponent c = new CompanionFollowShootComponent();
            c.setEntity(self);
            c.setAttack(true);
            c.create();

            verify(player1Events).addListener(eq("player_shoot_order"), cap.capture());
            EventListener2<Vector2, Vector2> listener = cap.getValue();

            // Bullet
            BulletPack pack = new BulletPack();
            pf.when(() -> ProjectileFactory.createFireballBullet(weaponStats))
                    .thenReturn(pack.bullet);

            // world == from -> dirToSameWorld is (0,0), should fallback to playerDir
            Vector2 world = new Vector2(3f, 4f);
            Vector2 playerDir = new Vector2(0f, 1f);

            listener.handle(world, playerDir);

            // Uses EntityService.register branch
            verify(entityService, times(1)).register(pack.bullet);
            // fire() should use playerDir
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

            // 1) attack=false -> early return
            CompanionFollowShootComponent c1 = new CompanionFollowShootComponent();
            c1.setEntity(self);
            c1.setAttack(false);
            c1.create();

            verify(player1Events).addListener(eq("player_shoot_order"), cap.capture());
            EventListener2<Vector2, Vector2> l1 = cap.getValue();
            l1.handle(new Vector2(10, 10), new Vector2(1, 0));
            pf.verifyNoInteractions();

            // 2) attack=true but stats == null -> early return
            clearInvocations(player1Events);
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

            // Fluent setter: just verify it returns this and does not throw
            assertSame(c, c.cooldown(0.1f));
        }
    }
}
