package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EnemyWavesTest{
    private GameArea gameArea;
    private Entity player;
    private EntityService entityService;
    private EnemyWaves enemyWaves;
    private Array<Entity> entities;

    private MockedStatic<ServiceLocator> serviceLocatorMock;
    private MockedStatic<Timer> timerMock;

    private static final long WAVE_DELAY_MS = 5000;

    @BeforeEach
    void setUp() {
        // Mock Gdx.app so logging calls don’t crash
        Gdx.app = mock(Application.class);
        when(Gdx.app.getLogLevel()).thenReturn(Application.LOG_DEBUG);

        // Mock Timer.schedule so tasks don't run in the background while testing
        timerMock = Mockito.mockStatic(Timer.class);
        timerMock.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        gameArea = mock(GameArea.class);
        player = mock(Entity.class);
        entityService = mock(EntityService.class);
        entities = new Array<>();

        // Mock ServiceLocator.getEntityService()
        serviceLocatorMock = Mockito.mockStatic(ServiceLocator.class);
        serviceLocatorMock.when(ServiceLocator::getEntityService).thenReturn(entityService);
        when(entityService.getEntities()).thenReturn(entities);

        enemyWaves = new EnemyWaves(3, gameArea, player); // 3 waves max
    }

    @AfterEach
    void tearDown() {
        serviceLocatorMock.close();
        timerMock.close();
    }

    @Test
    @DisplayName("Start wave function actually spawns the wave")
    void testStartWaveSpawnsImmediatelyOnFirstCall() {
        enemyWaves.startWave();
        verify(gameArea).spawnGhostGPT(anyInt(), anyFloat(), eq(player));
        verify(gameArea).spawnVroomba(anyInt(), anyFloat(), eq(player));
    }

    @Test
    @DisplayName("Enemies are scaled with each wave")
    void testSpawnWaveScalesDifficulty() {
        when(gameArea.getBaseDifficultyScale()).thenReturn(2f);

        enemyWaves.startWave(); // spawns wave 1
        verify(gameArea).spawnGhostGPT(eq(1), eq(2f), eq(player));
        verify(gameArea).spawnVroomba(eq(2), eq(2f), eq(player));

        entities.clear();
        advanceTimeAndTick(WAVE_DELAY_MS + 100);

        // Scaling test
        verify(gameArea).spawnGhostGPT(eq(1), eq(2.5f), eq(player));
        verify(gameArea).spawnVroomba(eq(2), eq(2.5f), eq(player));
    }

    @Test
    @DisplayName("New wave is not spawned when enemies are alive")
    void testDoesNotSpawnNewWaveIfEnemiesStillAlive() {
        Entity enemy = makeEnemy(10);
        entities.add(enemy);

        enemyWaves.startWave();

        // Remove previous calls to the gameArea class
        reset(gameArea);

        enemyWaves.tick();
        verifyNoInteractions(gameArea);
    }

    @Test
    @DisplayName("There is an actual delay between spawning of the next wave and the clearing of the previous")
    void testWaveDelayBeforeSpawningNextWave() {
        enemyWaves.startWave();
        reset(gameArea);

        entities.clear();
        advanceTimeAndTick(WAVE_DELAY_MS - 100);
        verifyNoInteractions(gameArea);

        advanceTimeAndTick(WAVE_DELAY_MS + 100);
        verify(gameArea).spawnGhostGPT(anyInt(), anyFloat(), eq(player));
    }

    @Test
    @DisplayName("Task is canceled when all waves are cleared in the room")
    void testAllWavesCompletedStopsTicking() {
        enemyWaves.startWave();

        for (int i = 0; i < 3; i++) {
            entities.clear();
            advanceTimeAndTick(WAVE_DELAY_MS + 100);
        }

        Assertions.assertTrue(enemyWaves.allWavesFinished());

        // Remove previous calls to the gameArea class
        reset(gameArea);

        entities.clear();
        advanceTimeAndTick(WAVE_DELAY_MS + 100);

        verifyNoInteractions(gameArea);
    }

    @Test
    @DisplayName("Restarting waves after completion of all waves actually resets the enemy scaling")
    void testRestartAfterCompletionResetsWaves() {
        enemyWaves.startWave();

        for (int i = 0; i < 3; i++) {
            entities.clear();
            advanceTimeAndTick(WAVE_DELAY_MS + 100);
        }
        Assertions.assertTrue(enemyWaves.allWavesFinished());

        // Remove previous calls to the gameArea class
        reset(gameArea);

        when(gameArea.getBaseDifficultyScale()).thenReturn(2f);
        enemyWaves.startWave();

        verify(gameArea).spawnGhostGPT(eq(1), eq(2f), eq(player));
        verify(gameArea).spawnVroomba(eq(2), eq(2f), eq(player));
    }

    @Test
    @DisplayName("Non enemy entities are ignored in the wave spawning logic")
    void testNonEnemyEntitiesIgnored() {
        Entity neutral = mock(Entity.class);
        when(neutral.getComponent(CombatStatsComponent.class)).thenReturn(new CombatStatsComponent(10));
        entities.add(neutral);

        enemyWaves.startWave();

        // Remove previous calls to the gameArea class
        reset(gameArea);

        advanceTimeAndTick(WAVE_DELAY_MS + 100);
        verify(gameArea).spawnGhostGPT(anyInt(), anyFloat(), eq(player));
    }

    /**
     * Create one enemy with the given health
     * @param health The health that the enemy should have.
     * @return The enemy {@link Entity} with the given health
     */
    private Entity makeEnemy(int health) {
        Entity enemy = mock(Entity.class);
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(stats.getHealth()).thenReturn(health);
        when(enemy.getComponent(CombatStatsComponent.class)).thenReturn(stats);
        when(enemy.getComponent(GhostAnimationController.class)).thenReturn(mock(GhostAnimationController.class));
        return enemy;
    }

    /**
     * Simulate time progression for tick evaluation
     * @param ms The time that is added on to the waveEndTime
     */
    private void advanceTimeAndTick(long ms) {
        try {
            var field = EnemyWaves.class.getDeclaredField("waveEndTime");
            field.setAccessible(true);

            long fakePast = System.currentTimeMillis() - ms;
            field.setLong(enemyWaves, fakePast);

            enemyWaves.tick();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}