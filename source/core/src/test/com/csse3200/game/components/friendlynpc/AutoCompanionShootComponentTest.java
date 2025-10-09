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
 * Unit tests for AutoCompanionShootComponent.
 */
class AutoCompanionShootComponentTest {

    private EntityService entityService;
    private GameArea gameArea;
    private GameTime time;
    private Entity player;
    private Entity self; // the entity that holds the component
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

        // Default to empty entity list to avoid NPEs in findTarget()
        when(entityService.getEntities()).thenReturn(new Array<>());

        // Self position & events
        when(self.getCenterPosition()).thenReturn(new Vector2(3f, 4f));
        when(self.getPosition()).thenReturn(new Vector2(3f, 4f));
        var events = mock(com.csse3200.game.events.EventHandler.class);
        when(self.getEvents()).thenReturn(events);

        // Player -> PlayerActions -> weapon stats
        when(player.getComponent(PlayerActions.class)).thenReturn(playerActions);
        when(playerActions.getCurrentWeaponStats()).thenReturn(weaponStats);

        // Time step equals the scan interval so each update triggers a scan
        when(time.getDeltaTime()).thenReturn(0.12f);
    }

    /** Helper to mock ServiceLocator static getters in one place. */
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

    /** Builds a valid NPC enemy entity at a given center with an alive/dead flag. */
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

    /** Replaces the default empty entity list with the provided entities. */
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

            // Sanity: update should call the time source, indicating boundPlayer is set and flow continues.
            c.update();
            verify(time, atLeastOnce()).getDeltaTime();
        }
    }

    @Test
    void update_firesAtNearestEnemy_spawnsViaGameArea_andSetsCooldown() {
        // Closest alive enemy (2m to the right)
        Entity enemyNear = makeEnemy(new Vector2(5f, 4f), true);
        // Supply list that contains self and player (filtered out by isEnemy) and the enemy
        supplyEntities(self, player, enemyNear);

        // Bullet + projectile behavior
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

            // First update scans and fires
            c.update();

            pf.verify(() -> ProjectileFactory.createFireballBullet(weaponStats), times(1));
            verify(bullet, times(1)).setPosition(eq(3f - 0.5f), eq(4f - 0.5f)); // center minus half size
            verify(gameArea, times(1)).spawnEntity(bullet);

            // Direction should be (5,4)-(3,4)=(2,0), speed=5
            verify(proj, times(1)).fire(argThat(v -> v.epsilonEquals(new Vector2(2f, 0f), 1e-5f)), eq(5f));
            verify(self.getEvents(), times(1)).trigger("fired");

            // Cooldown prevents firing on the next update
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

            // Without GameArea, it should register via EntityService
            verify(entityService, times(1)).register(bullet);
            verify(proj, times(1)).fire(any(), eq(5f));
        }
    }

    @Test
    void update_doesNothing_whenAttackDisabled_orNoTarget() {
        // Case 1: attack=false → early return
        try (var sl = mockServices(true)) {
            AutoCompanionShootComponent c1 = new AutoCompanionShootComponent();
            c1.setEntity(self);
            c1.create();
            c1.setAttack(false);
            c1.update();

            verifyNoInteractions(gameArea);
            verify(entityService, never()).register(any());
        }

        // Case 2: invalid target (enemy dead) → do not fire
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



