package com.csse3200.game.components.items;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.*;


import static org.mockito.Mockito.*;

/**
 * Tests for the MeleeUseComponent class.
 * Covers cooldown handling, animation triggering,
 * hit detection, and missing component edge cases.
 */
public class MeleeUseComponentTest {

    private Sound mockSound;
    private EntityService entityService;
    private GameTime gameTime;
    private Entity player;
    private MeleeUseComponent meleeUse;
    private WeaponsStatsComponent weaponStats;

    @Before
    public void setUp() {
        ServiceLocator.clear();

        // Mock services
        mockSound = mock(Sound.class);
        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(mockSound);
        ServiceLocator.registerResourceService(resourceService);

        gameTime = mock(GameTime.class);
        when(gameTime.getTime()).thenReturn(0L);
        ServiceLocator.registerTimeSource(gameTime);

        entityService = new EntityService();
        ServiceLocator.registerEntityService(entityService);

        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Player entity setup
        player = new Entity();
        weaponStats = new WeaponsStatsComponent(10);
        weaponStats.setCoolDown(0.3f);
        meleeUse = new MeleeUseComponent();

        player.addComponent(weaponStats);
        player.addComponent(meleeUse);
        player.create();
    }

    @After
    public void tearDown() {
        ServiceLocator.clear();
    }

    /**
     * Should not attack if no WeaponsStatsComponent is present.
     */
    @Test
    public void shouldReturnIfNoWeaponStats() {
        Entity item = new Entity().addComponent(new MeleeUseComponent());
        item.create();

        // Replace ServiceLocator time to avoid NPE
        when(gameTime.getTime()).thenReturn((long) 1f);

        item.getComponent(MeleeUseComponent.class).use(player);
        verify(mockSound, never()).play();
    }

    /**
     * Should respect cooldown and not attack again too soon.
     */
    @Test
    public void shouldRespectCooldown() {
        Entity enemy = createEnemy();
        entityService.register(enemy);

        // First attack
        when(gameTime.getTime()).thenReturn(0L);
        meleeUse.use(player);
        verify(mockSound, times(1)).play();

        // Immediately try again (before cooldown)
        when(gameTime.getTime()).thenReturn(0L);
        meleeUse.use(player);
        verifyNoMoreInteractions(mockSound);
    }

    /**
     * Should apply damage and play sound when enemy is in range.
     */
    @Test
    public void shouldDealDamageAndPlaySound() {
        CombatStatsComponent enemyStats = spy(new CombatStatsComponent(100, 0));
        Entity enemy = new Entity()
                .addComponent(enemyStats)
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC));
        enemy.setPosition(1f, 1f);
        entityService.register(enemy);

        when(gameTime.getTime()).thenReturn(0L);
        meleeUse.use(player);

        verify(enemyStats).takeDamage(weaponStats.getBaseAttack());
        verify(mockSound).play();
    }

    /**
     * Should not hit entities beyond attack range.
     */
    @Test
    public void shouldNotHitOutOfRangeEnemy() {
        CombatStatsComponent enemyStats = mock(CombatStatsComponent.class);
        Entity enemy = new Entity()
                .addComponent(enemyStats)
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC));
        enemy.setPosition(10f, 10f);
        entityService.getEntities().add(enemy);

        meleeUse.use(player);
        verify(enemyStats, never()).takeDamage((int) anyFloat());
    }

    /**
     * Should ignore entities missing CombatStatsComponent.
     */
    @Test
    public void shouldIgnoreEnemiesWithoutCombatStats() {
        Entity enemy = new Entity()
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC));
        enemy.setPosition(1f, 1f);
        entityService.register(enemy);

        meleeUse.use(player);
        verify(mockSound, never()).play();
    }

    /**
     * Should trigger animation when AnimationRenderComponent is present.
     */
    @Test
    public void shouldTriggerAnimEvent() {
        // Mock the entity and its event handler
        Entity mockEntity = mock(Entity.class);
        EventHandler mockEvents = mock(EventHandler.class);
        when(mockEntity.getComponent(AnimationRenderComponent.class))
                .thenReturn(mock(AnimationRenderComponent.class));
        when(mockEntity.getEvents()).thenReturn(mockEvents);

        // Attach the entity normally
        MeleeUseComponent melee = new MeleeUseComponent();
        melee.setEntity(mockEntity);  // <- built-in setter, no reflection

        // Act
        melee.weaponAnimation();

        // Assert
        verify(mockEvents, times(1)).trigger("anim");
    }

    /**
     * Should not trigger animation when AnimationRenderComponent is absent.
     */
    @Test
    public void shouldNotTriggerAnimationWhenNoAnimationComponent() {
        // Arrange: create the entity and attach the component BEFORE create()
        Entity weaponEntity = new Entity();
        WeaponsStatsComponent stats = new WeaponsStatsComponent(5);
        MeleeUseComponent meleeUse = spy(new MeleeUseComponent());

        weaponEntity
                .addComponent(stats)
                .addComponent(meleeUse);

        weaponEntity.create(); // <-- sets meleeUse.entity

        // Act
        meleeUse.use(player);

        // Assert: verify helper called, but no animation component exists
        verify(meleeUse, times(1)).weaponAnimation();
        Assert.assertNull(
                weaponEntity.getComponent(com.csse3200.game.rendering.AnimationRenderComponent.class)
        );
    }

    /**
     * Should skip attack when game is paused.
     */
    @Test
    public void shouldNotAttackWhenGamePaused() {
        // Game time mocked as paused
        when(ServiceLocator.getTimeSource().isPaused()).thenReturn(true);

        Entity enemy = createEnemy();
        entityService.register(enemy);

        meleeUse.use(player);

        verify(mockSound, never()).play();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private Entity createEnemy() {
        CombatStatsComponent enemyStats = spy(new CombatStatsComponent(50, 0));
        HitboxComponent hitbox = (HitboxComponent) new HitboxComponent().setLayer(PhysicsLayer.NPC);
        Entity enemy = new Entity().addComponent(enemyStats).addComponent(hitbox);
        enemy.setPosition((float) 1.0, (float) 1.0);
        return enemy;
    }
}
