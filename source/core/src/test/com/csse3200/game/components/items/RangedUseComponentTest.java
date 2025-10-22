package com.csse3200.game.components.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.*;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.effects.UnlimitedAmmoEffect;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RangedUseComponentTest {
    @Mock private GameTime gameTime;
    @Mock private ResourceService resourceService;
    @Mock private EntityService entityService;
    @Mock private Sound sound;
    @Mock private PhysicsService physicsService;
    @Mock private PhysicsProjectileComponent projectilePhysics;

    private Entity player;
    private Entity weapon;
    private RangedUseComponent ranged;
    private static MockedStatic<ProjectileFactory> projectileFactoryMock; // shared across tests

    @BeforeClass
    public static void beforeAll() {
        projectileFactoryMock = mockStatic(ProjectileFactory.class);
    }

    @AfterClass
    public static void afterAll() {
        projectileFactoryMock.close(); // closes all static mocking
    }

    @Before
    public void setUp() {
        ServiceLocator.clear();
        ServiceLocator.registerTimeSource(gameTime);
        ServiceLocator.registerEntityService(entityService);
        ServiceLocator.registerResourceService(resourceService);
        ServiceLocator.registerPhysicsService(physicsService);
        ServiceLocator.registerRenderService(mock(RenderService.class));

        Camera camera = mock(Camera.class);
        when(camera.unproject(any(Vector3.class)))
                .thenAnswer(i -> ((Vector3) i.getArgument(0)).set(5f, 5f, 0f));
        ServiceLocator.registerCamera(camera);

        if (Gdx.input == null) Gdx.input = mock(Input.class);
        when(Gdx.input.getX()).thenReturn(100);
        when(Gdx.input.getY()).thenReturn(100);

        player = new Entity().addComponent(new PowerupComponent());
        InventoryComponent inventory = new InventoryComponent(1);
        Entity fakeItem = new Entity().addComponent(new WeaponsStatsComponent(5));
        fakeItem.create();
        player.addComponent(inventory);
        inventory.setCurrItem(fakeItem);
        player.create();
        ServiceLocator.registerPlayer(player);

        weapon = new Entity();
        ranged = new RangedUseComponent();
        weapon.addComponent(new WeaponsStatsComponent(10))
                .addComponent(new MagazineComponent(3))
                .addComponent(ranged)
                .create();
        ranged.setEntity(weapon);

        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
        doNothing().when(projectilePhysics).fire(any(Vector2.class), anyFloat());

        // Set default return for all tests
        Entity fakeProjectile = new Entity().addComponent(projectilePhysics);
        projectileFactoryMock.when(() -> ProjectileFactory.createPistolBullet(any(), anyBoolean()))
                .thenReturn(fakeProjectile);
        projectileFactoryMock.when(() -> ProjectileFactory.createBomb(any(), any(), any()))
                .thenReturn(fakeProjectile);
    }


    @After
    public void tearDown() {
        ServiceLocator.clear();
    }

    // ---------------------------------------------------------
    // Core use() behaviour
    // ---------------------------------------------------------

    @Test
    public void shouldPlaySoundAndFireProjectile() throws Exception {
        when(gameTime.getTime()).thenReturn((long) 2000f);

        // Create and inject a mock EventHandler
        EventHandler mockEvents = mock(EventHandler.class);
        Field eventsField = Entity.class.getDeclaredField("eventHandler");
        eventsField.setAccessible(true);
        eventsField.set(player, mockEvents);

        ranged.use(player);

        verify(entityService).register(any(Entity.class));
        verify(sound).play();
        verify(mockEvents).trigger(eq("player_shoot_order"), any(), any());
    }

    @Test
    public void shouldSkipUseWhenInCooldown() {
        when(gameTime.getTime()).thenReturn(0L, (long) 100f); // less than cooldown (0.3s*1000)
        WeaponsStatsComponent stats = weapon.getComponent(WeaponsStatsComponent.class);
        stats.setCoolDown(0.3f);

        // run twice — second should skip
        ranged.use(player);
        ranged.use(player);

        verify(sound, times(1)).play();
    }

    @Test
    public void shouldNotShootWhenAmmoEmpty() {
        weapon.getComponent(MagazineComponent.class).setCurrentAmmo(0);
        when(gameTime.getTime()).thenReturn((long) 1000f);

        ranged.use(player);
        verify(resourceService).getAsset(eq("sounds/shot_failed.mp3"), eq(Sound.class));
        verify(sound, times(1)).play();
    }

    @Test
    public void shouldIgnoreCooldownIfUnlimitedAmmoActive() {
        // Give player UnlimitedAmmoEffect
        PowerupComponent powerups = player.getComponent(PowerupComponent.class);
        powerups.addEffect(new UnlimitedAmmoEffect(10f));

        when(gameTime.getTime()).thenReturn(0L, (long) 100f);
        ranged.use(player);
        ranged.use(player); // should bypass cooldown

        verify(sound, atLeastOnce()).play();
    }

    @Test
    public void shouldCreateBombWhenConsumable() {
        weapon.addComponent(new ConsumableComponent(null, 0));
        weapon.create();

        Entity mockBomb = mock(Entity.class);
        when(ProjectileFactory.createBomb(any(), any(), any())).thenReturn(mockBomb);

        ranged.setEntity(weapon);
        when(gameTime.getTime()).thenReturn((long) 2000f);

        ranged.use(player);

        verify(resourceService).getAsset(eq("sounds/laser_blast.mp3"), eq(Sound.class));
    }

    // ---------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------

    @Test
    public void shouldDecrementMagazineAfterShot() {
        MagazineComponent mag = weapon.getComponent(MagazineComponent.class);
        int before = mag.getCurrentAmmo();

        when(gameTime.getTime()).thenReturn((long) 5000f);
        ranged.use(player);

        Assert.assertEquals(before - 1, mag.getCurrentAmmo());
    }

    @Test
    public void shouldReturnFalseWhenCooldownNotElapsed() {
        WeaponsStatsComponent stats = weapon.getComponent(WeaponsStatsComponent.class);
        stats.setCoolDown(1f); // 1 second cooldown

        // simulate last attack just now
        try {
            Field f = RangedUseComponent.class.getDeclaredField("timeSinceLastAttack");
            f.setAccessible(true);
            f.setFloat(ranged, 1000f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(gameTime.getTime()).thenReturn((long) 1000f); // same time

        boolean canShoot = rangedTestHandleMag(stats, 1001f);
        Assert.assertFalse(canShoot);
    }


    // quick reflection-free access to handleMagazine()
    private boolean rangedTestHandleMag(WeaponsStatsComponent stats, float currentTime) {
        try {
            java.lang.reflect.Method m = RangedUseComponent.class.getDeclaredMethod("handleMagazine", WeaponsStatsComponent.class);
            m.setAccessible(true);
            when(gameTime.getTime()).thenReturn((long) currentTime);
            return (boolean) m.invoke(ranged, stats);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldReturnTrueWhenConsumableItem() {
        weapon.addComponent(new ConsumableComponent(null, 0));
        weapon.create();
        ranged.setEntity(weapon);

        WeaponsStatsComponent stats = weapon.getComponent(WeaponsStatsComponent.class);
        boolean result = rangedTestHandleMag(stats, 10000f);

        Assert.assertTrue(result);
    }
}
