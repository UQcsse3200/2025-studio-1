package com.csse3200.game.components;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Projectiles;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@ExtendWith(GameExtension.class)
public class ProjectileLauncherComponentTest {
    private GameArea mockArea;
    private Entity mockEntity;
    private Entity mockProjectile;
    private WeaponsStatsComponent mockWeaponStats;
    MockedStatic<Timer> timerMock;

    private ProjectileLauncherComponent mockLauncher;

    @BeforeEach
    void priorSetup() {
        mockArea = mock(GameArea.class);
        mockEntity = mock(Entity.class);
        mockProjectile = mock(Entity.class);
        mockWeaponStats = mock(WeaponsStatsComponent.class);

        PhysicsService mockPhysics = mock(PhysicsService.class);
        ServiceLocator.registerPhysicsService(mockPhysics);

        when(mockArea.spawnGhostGPTProjectile(any(Vector2.class), any())).thenReturn(mockProjectile);

        mockLauncher = new ProjectileLauncherComponent(mockArea, mock(Entity.class), Projectiles.GHOSTGPT_LASER);
        mockLauncher.setEntity(mockEntity);
        when(mockEntity.getComponent(WeaponsStatsComponent.class)).thenReturn(mockWeaponStats);
        when(mockEntity.getPosition()).thenReturn(new Vector2(0f, 0f));
    }

    @Test
    void fireProjectileCorrectly() {
        Vector2 dir = new Vector2(1, 0);
        Vector2 offset = new Vector2(2, 3);
        Vector2 scale = new Vector2(0.5f, 1f);

        mockLauncher.fireProjectile(dir, offset, scale);

        // Verify a projectile was spawned
        verify(mockArea).spawnGhostGPTProjectile(eq(dir), eq(mockWeaponStats));

        // Verify its position and scaling were set
        verify(mockProjectile).setPosition(new Vector2(2f, 3f));
        verify(mockProjectile).scaleWidth(0.5f);
        verify(mockProjectile).scaleHeight(1f);
    }

    @Test
    void fireProjectileMultishotCorrectly() {
        Vector2 dir = new Vector2(1, 0);
        Vector2 offset = new Vector2(2, 3);
        Vector2 scale = new Vector2(0.5f, 1f);

        int amount = 5;
        float angleDifferences = 30;

        ProjectileLauncherComponent spyLauncher = spy(mockLauncher);

        spyLauncher.fireProjectileMultishot(amount, angleDifferences, dir, offset, scale);

        verify(spyLauncher, times(amount)).fireProjectile(any(), any(), any());
    }

    @Test
    void fireProjectileBurstFireCorrectly() {
        Vector2 dir = new Vector2(1, 0);
        Vector2 offset = new Vector2(2, 3);
        Vector2 scale = new Vector2(0.5f, 1f);

        int burstAmount = 6;
        float timeBetweenShots = 0.05f;

        ProjectileLauncherComponent spyLauncher = spy(mockLauncher);
        spyLauncher.setEntity(mockEntity);

        timerMock = mockStatic(Timer.class);
        timerMock.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                .thenAnswer(invocation -> {
                    Timer.Task task = invocation.getArgument(0);
                    float timeBetweenShotsFromTimer = invocation.getArgument(2);

                    // Ensure time between shots is correct.
                    assertEquals(timeBetweenShots, timeBetweenShotsFromTimer, 1e-6);

                    // Calculate how many times to run: burstAmount
                    for (int i = 0; i < burstAmount; i++) {
                        task.run();
                        verify(spyLauncher, times(i+1)).fireProjectile(any(), any(), any());
                    }
                    return null;
                });

        spyLauncher.fireProjectileBurstFire(burstAmount, timeBetweenShots, dir, offset, scale);

        // Final verification
        verify(spyLauncher, times(burstAmount)).fireProjectile(any(), any(), any());
    }

    @AfterEach
    void tearDown() {
        if (timerMock != null) timerMock.close();
    }
}
