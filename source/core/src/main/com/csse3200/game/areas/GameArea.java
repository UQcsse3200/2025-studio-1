package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.entities.PromptFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.cutscenes.BadWinAnimationScreen;
import com.csse3200.game.areas.cutscenes.GoodWinAnimationScreen;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.enemy.EnemyWaves;
import com.csse3200.game.components.enemy.EnemyWavesDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

import java.util.*;
import java.util.function.Supplier;

/**
 * Represents an area in the game, such as a level, indoor area, etc. An area has a terrain and
 * other entities to spawn on that terrain.
 *
 * <p>Support for enabling/disabling game areas could be added by making this a Component instead.
 */
public abstract class GameArea implements Disposable {
    protected TerrainComponent terrain;
    protected List<Entity> areaEntities;
    protected TerrainFactory terrainFactory;
    protected CameraComponent cameraComponent;
    protected float baseScaling = 0f;
    /** Global flag preventing re-entrant room transitions across any area */
    protected static boolean isTransitioning = false;

    // Enemy name constants (standard + variants)
    private static final String DEEP_SPIN = "DeepSpin";
    private static final String DEEP_SPIN_RED = "DeepSpinRed";
    private static final String DEEP_SPIN_BLUE = "DeepSpinBlue";
    private static final String GHOST_GPT = "GhostGPT";
    private static final String GHOST_GPT_RED = "GhostGPTRed";
    private static final String GHOST_GPT_BLUE = "GhostGPTBlue";
    private static final String VROOMBA = "Vroomba";
    private static final String VROOMBA_RED = "VroombaRed";
    private static final String VROOMBA_BLUE = "VroombaBlue";
    private static final String GROK_DROID = "GrokDroid";
    private static final String GROK_DROID_RED = "GrokDroidRed";
    private static final String GROK_DROID_BLUE = "GrokDroidBlue";
    private static final String TURRET = "Turret";

    protected EnemyWaves wavesManager; // manage waves via terminal command

    protected GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        this.terrainFactory = terrainFactory;
        this.cameraComponent = cameraComponent;
        areaEntities = new ArrayList<>();
        PromptFactory.createPrompt();
    }

    /**
     * Create the game area in the world.
     */
    public abstract void create();

    /**
     * Dispose of all internal entities in the area
     */
    public void dispose() {
        for (Entity entity : areaEntities) {
            entity.dispose();
        }
    }

    /**
     * Attempt to start a room transition. Returns false if one is already in progress.
     */
    protected boolean beginTransition() {
        if (isTransitioning || ServiceLocator.isTransitioning()) return false;
        isTransitioning = true;
        ServiceLocator.setTransitioning(true);
        return true;
    }

    /**
     * Mark the end of a room transition.
     */
    protected void endTransition() {
        isTransitioning = false;
        ServiceLocator.setTransitioning(false);
    }

    /**
     * Spawn entity at its current position
     *
     * @param entity Entity (not yet registered)
     */
    public void spawnEntity(Entity entity) {
        areaEntities.add(entity);
        if (ServiceLocator.isTransitioning()) {
            Gdx.app.postRunnable(() -> ServiceLocator.getEntityService().register(entity));
        } else {
            ServiceLocator.getEntityService().register(entity);
        }
    }

    protected void spawnFloor() {
        for (int i = 0; i < 25; i += 4) {
            GridPoint2 floorspawn = new GridPoint2(i, 6);

            Entity floor = ObstacleFactory.createLongFloor();
            spawnEntityAt(floor, floorspawn, false, false);
        }
    }

    protected void spawnVisibleFloor() {
        for (int i = 0; i < 25; i += 4) {
            GridPoint2 floorspawn = new GridPoint2(i, 3);

            Entity floor = ObstacleFactory.createVisibleLongFloor();
            spawnEntityAt(floor, floorspawn, false, false);
        }
    }

    /**
     * Start enemy waves from terminal command by typing "waves".
     */
    public void startWaves(Entity player) {
        if (wavesManager == null || wavesManager.allWavesFinished()) {
            int room = getRoomNumber();
            int maxWaves = room > 4 ? 2 : 1; // mimic original behaviour: higher rooms get 2 waves
            wavesManager = new EnemyWaves(maxWaves, this, player);
            EnemyWavesDisplay waveDisplay = new EnemyWavesDisplay(wavesManager);
            Gdx.app.log("GameArea", "Initializing waves: room=" + room + " maxWaves=" + maxWaves);
        }
        wavesManager.startWave();
    }

    /**
     * Returns the room name corresponding to the current floor.
     *
     * @return the name of the current room
     */
    public String getRoomName() {
        return this.toString();
    }

    /**
     * Returns the room number corresponding to the current floor.
     *
     * @return Room number as an int if the floor name is in the format "Floor2"
     * with 2 being any number, otherwise returns 1.
     */
    public int getRoomNumber() { // changed from protected to public for EnemyWaves access
        return switch (this.toString()) {
            case "Reception" -> 2;
            case "Mainhall" -> 3;
            case "Security" -> 4;
            case "Office" -> 5;
            case "Elevator" -> 6;
            case "Research" -> 7;
            case "Shipping" -> 8;
            case "Storage" -> 9;
            case "Server" -> 10;
            case "Tunnel" -> 11;
            case "GoodWinAnimation" -> 101; //Animation start from 101
            case "BadWinAnimation" -> 102;
            default -> 1;
        };
    }

    /**
     * Returns the base difficulty scale of the current room.
     *
     * @return Scaling factor as a float.
     */
    public float getBaseDifficultyScale() {
        int room = getRoomNumber();
        // +40% per room after first (tweak as needed)
        return 1f + 0.4f * Math.max(0, room - 1);
    }

    protected void spawnEntityAt(
            Entity entity, GridPoint2 tilePos, boolean centerX, boolean centerY) {
        Vector2 worldPos = terrain.tileToWorldPosition(tilePos);
        float tileSize = terrain.getTileSize();

        if (centerX) {
            worldPos.x += (tileSize / 2) - entity.getCenterPosition().x;
        }
        if (centerY) {
            worldPos.y += (tileSize / 2) - entity.getCenterPosition().y;
        }

        entity.setPosition(worldPos);
        spawnEntity(entity);
    }

    /**
     * Spawns the enemies based on the room number given.
     *
     * @param roomName    The name of the current floor/room.
     * @param total       The total number of enemies to be spawned.
     * @param scaleFactor The scaling factor of the difficulty of the enemies to be spawned.
     * @param player      The player {@link Entity} that is to be targeted by the enemies.
     */
    public void spawnEnemies(String roomName, int total, float scaleFactor, Entity player) {
        Map<String, ArrayList<Vector2>> positions = getEnemySpawnPosition(roomName);
        switch (roomName) {
            case "Forest":
                spawnDeepspin(total, scaleFactor, player, positions);
                spawnTurret(total, scaleFactor, player, positions);
                break;
            case "Reception":
                spawnVroomba(total, scaleFactor, player, positions);
                spawnGhostGPT(total, scaleFactor, player, positions);
                break;
            case "Mainhall":
                spawnVroomba(total, scaleFactor, player, positions);
                spawnDeepspin(total, scaleFactor, player, positions);
                break;
            case "Security":
                spawnGhostGPT(total, scaleFactor, player, positions);
                spawnDeepspin(total, scaleFactor, player, positions);
                break;
            case "Office":
                spawnGhostGPT(total, scaleFactor, player, positions);
                spawnDeepspin(total, scaleFactor, player, positions);
                spawnVroomba(total, scaleFactor, player, positions);
                break;
            case "Elevator":
                spawnGhostGPT(total, scaleFactor, player, positions);
                spawnGrokDroid(total, scaleFactor, player, positions);
                break;
            case "Research":
                spawnTurret(total, scaleFactor, player, positions);
                spawnGhostGPT(total, scaleFactor, player, positions);
                spawnGrokDroid(total, scaleFactor, player, positions);
                break;
            case "Shipping":
                spawnGhostGPT(total, scaleFactor, player, positions);
                spawnGrokDroid(total, scaleFactor, player, positions);
                spawnVroomba(total, scaleFactor, player, positions);
                break;
            case "Storage":
                spawnTurret(total, scaleFactor, player, positions);
                spawnGrokDroid(total, scaleFactor, player, positions);
                break;
            case "Server":
                spawnGhostGPT(total, scaleFactor, player, positions);
                spawnDeepspin(total, scaleFactor, player, positions);
                spawnTurret(total, scaleFactor, player, positions);
                break;
            case "Tunnel":
                spawnGhostGPT(total, scaleFactor, player, positions);
                spawnGrokDroid(total, scaleFactor, player, positions);
                spawnTurret(total, scaleFactor, player, positions);
                spawnVroomba(total, scaleFactor, player, positions);
                break;
            default:
                spawnGhostGPT(total, scaleFactor, player, positions);
                spawnGrokDroid(total, scaleFactor, player, positions);
                break;
        }
    }

    /**
     * Spawns the enemies based on the enemy name
     *
     * @param roomName    The number of the current floor/room.
     * @param total       The total number of enemies to be spawned.
     * @param scaleFactor The scaling factor of the difficulty of the enemies to be spawned.
     * @param player      The player {@link Entity} that is to be targeted by the enemies.
     */
    public void spawn(String name, String roomName, int total, float scaleFactor, Entity player) {
        HashMap<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> respectiveSpawns = new ArrayList<>();
        respectiveSpawns.add(new Vector2(12f, 4f));
        switch (name) {
            case (GHOST_GPT):
                positions.put(GHOST_GPT, respectiveSpawns);
                spawnGhostGPT(total, scaleFactor, player, positions);
                break;
            case (GHOST_GPT_RED):
                positions.put(GHOST_GPT_RED, respectiveSpawns);
                spawnGhostGPTRed(total, scaleFactor, player, positions);
                break;
            case (GHOST_GPT_BLUE):
                positions.put(GHOST_GPT_BLUE, respectiveSpawns);
                spawnGhostGPTBlue(total, scaleFactor, player, positions);
                break;
            case (GROK_DROID):
                positions.put(GROK_DROID, respectiveSpawns);
                spawnGrokDroid(total, scaleFactor, player, positions);
                break;
            case (GROK_DROID_RED):
                positions.put(GROK_DROID_RED, respectiveSpawns);
                spawnGrokDroidRed(total, scaleFactor, player, positions);
                break;
            case (GROK_DROID_BLUE):
                positions.put(GROK_DROID_BLUE, respectiveSpawns);
                spawnGrokDroidBlue(total, scaleFactor, player, positions);
                break;
            case (DEEP_SPIN):
                positions.put(DEEP_SPIN, respectiveSpawns);
                spawnDeepspin(total, scaleFactor, player, positions);
                break;
            case (DEEP_SPIN_RED):
                positions.put(DEEP_SPIN_RED, respectiveSpawns);
                spawnDeepspinRed(total, scaleFactor, player, positions);
                break;
            case (DEEP_SPIN_BLUE):
                positions.put(DEEP_SPIN_BLUE, respectiveSpawns);
                spawnDeepspinBlue(total, scaleFactor, player, positions);
                break;
            case (TURRET):
                positions.put(TURRET, respectiveSpawns);
                spawnTurret(total, scaleFactor, player, positions);
                break;
            case (VROOMBA):
                positions.put(VROOMBA, respectiveSpawns);
                spawnVroomba(total, scaleFactor, player, positions);
                break;
            case (VROOMBA_RED):
                positions.put(VROOMBA_RED, respectiveSpawns);
                spawnVroombaRed(total, scaleFactor, player, positions);
                break;
            case (VROOMBA_BLUE):
                positions.put(VROOMBA_BLUE, respectiveSpawns);
                spawnVroombaBlue(total, scaleFactor, player, positions);
                break;
        }
    }

    /**
     * Retrieves current wave count for services
     */
    public int currentWave() {
        return wavesManager.getWaveNumber();
    }

    /**
     * Adds GhostGPT enemies onto the map.
     *
     * @param total       The total number of GhostGPT to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the GhostGPT
     */
    public void spawnGhostGPT(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(GHOST_GPT);
        for (Vector2 pos : spawnPositions) {
            Entity ghostGpt = NPCFactory.createGhostGPT(player, this, scaleFactor);
            ghostGpt.setPosition(pos);
            spawnEntity(ghostGpt);
        }
    }
    /**
     * Adds GhostGPTRed enemies onto the map.
     *
     * @param total       The total number of GhostGPT to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the GhostGPT
     */
    public void spawnGhostGPTRed(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(GHOST_GPT_RED);
        for (Vector2 pos : spawnPositions) {
            Entity ghostGptRed = NPCFactory.createGhostGPTRed(player, this, scaleFactor);
            ghostGptRed.setPosition(pos);
            spawnEntity(ghostGptRed);
        }
    }
    /**
     * Adds GhostGPTBlue enemies onto the map.
     *
     * @param total       The total number of GhostGPT to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the GhostGPT
     */
    public void spawnGhostGPTBlue(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(GHOST_GPT_BLUE);
        for (Vector2 pos : spawnPositions) {
            Entity ghostGptBlue = NPCFactory.createGhostGPTBlue(player, this, scaleFactor);
            ghostGptBlue.setPosition(pos);
            spawnEntity(ghostGptBlue);
        }
    }
    /**
     * Adds DeepSpin enemies onto the map.
     *
     * @param total       The total number of DeepSpins to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the DeepSpin
     */
    public void spawnDeepspin(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(DEEP_SPIN);

        for (Vector2 pos : spawnPositions) {
            Entity deepSpin = NPCFactory.createDeepspin(player, this, scaleFactor);
            deepSpin.setPosition(pos);
            spawnEntity(deepSpin);
        }
    }
    /**
     * Adds DeepSpinRed enemies onto the map.
     *
     * @param total       The total number of DeepSpins to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the DeepSpin
     */
    public void spawnDeepspinRed(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(DEEP_SPIN_RED);

        for (Vector2 pos : spawnPositions) {
            Entity deepSpinRed = NPCFactory.createDeepspinRed(player, this, scaleFactor);
            deepSpinRed.setPosition(pos);
            spawnEntity(deepSpinRed);
        }
    }
    /**
     * Adds DeepSpinBlue enemies onto the map.
     *
     * @param total       The total number of DeepSpins to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the DeepSpin
     */
    public void spawnDeepspinBlue(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(DEEP_SPIN_BLUE);

        for (Vector2 pos : spawnPositions) {
            Entity deepSpinBlue = NPCFactory.createDeepspinBlue(player, this, scaleFactor);
            deepSpinBlue.setPosition(pos);
            spawnEntity(deepSpinBlue);
        }
    }
    /**
     * Adds GrokDroid enemies onto the map.
     *
     * @param total       The total number of GrokDroid to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the GrokDroid
     */
    public void spawnGrokDroid(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(GROK_DROID);
        for (Vector2 pos : spawnPositions) {
            Entity grokDroid = NPCFactory.createGrokDroid(player, this, scaleFactor);
            grokDroid.setPosition(pos);
            spawnEntity(grokDroid);
        }
    }
    /**
     * Adds GrokDroidRed enemies onto the map.
     *
     * @param total       The total number of GrokDroid to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the GrokDroid
     */
    public void spawnGrokDroidRed(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(GROK_DROID_RED);
        for (Vector2 pos : spawnPositions) {
            Entity grokDroidRed = NPCFactory.createGrokDroidRed(player, this, scaleFactor);
            grokDroidRed.setPosition(pos);
            spawnEntity(grokDroidRed);
        }
    }
    /**
     * Adds GrokDroidBlue enemies onto the map.
     *
     * @param total       The total number of GrokDroid to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the GrokDroid
     */
    public void spawnGrokDroidBlue(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(GROK_DROID_BLUE);
        for (Vector2 pos : spawnPositions) {
            Entity grokDroidBlue = NPCFactory.createGrokDroidBlue(player, this, scaleFactor);
            grokDroidBlue.setPosition(pos);
            spawnEntity(grokDroidBlue);
        }
    }

    /**
     * Adds Vroomba enemies onto the map.
     *
     * @param total       The total number of Vroomba to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the Vroomba
     */
    public void spawnVroomba(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(VROOMBA);
        for (Vector2 pos : spawnPositions) {
            Entity vroombaEntity = NPCFactory.createVroomba(player, scaleFactor);
            vroombaEntity.setPosition(pos);
            spawnEntity(vroombaEntity);
        }
    }
    /**
     * Adds VroombaRed enemies onto the map.
     *
     * @param total       The total number of Vroomba to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the Vroomba
     */
    public void spawnVroombaRed(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(VROOMBA_RED);
        for (Vector2 pos : spawnPositions) {
            Entity vroombaRed = NPCFactory.createVroombaRed(player, scaleFactor);
            vroombaRed.setPosition(pos);
            spawnEntity(vroombaRed);
        }
    }
    /**
     * Adds VroombaBlue enemies onto the map.
     *
     * @param total       The total number of Vroomba to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the Vroomba
     */
    public void spawnVroombaBlue(
            int total, float scaleFactor, Entity player, Map<String, ArrayList<Vector2>> positions) {
        ArrayList<Vector2> spawnPositions = positions.get(VROOMBA_BLUE);
        for (Vector2 pos : spawnPositions) {
            Entity vroombaBlue = NPCFactory.createVroombaBlue(player, scaleFactor);
            vroombaBlue.setPosition(pos);
            spawnEntity(vroombaBlue);
        }
    }
    /**
     * Adds Turret enemies onto the map.
     *
     * @param total       The total number of Turret to be spawned.
     * @param scaleFactor The scale of increase in difficulty of the GhostGPT
     */
    public void spawnTurret(
            int total, float scaleFactor, Entity player,
            Map<String, ArrayList<Vector2>> positions
    ) {
        ArrayList<Vector2> spawnPositions = positions.get(TURRET);
        for (Vector2 pos : spawnPositions) {
            Entity turretEntity = NPCFactory.createTurret(player, this, scaleFactor);
            turretEntity.setPosition(pos);
            spawnEntity(turretEntity);
        }
    }

    /**
     * Spawns the projectile used by the Ghost GPT Enemy
     *
     * @param directionToFire The direction in which the projectile is to be fired.
     * @param source          The damage and other statistics that the projectile will use.
     * @return The spawned projectile {@link Entity}
     */
    public Entity spawnGhostGPTProjectile(Vector2 directionToFire, WeaponsStatsComponent source) {
        Entity laser = ProjectileFactory.createEnemyLaserProjectile(directionToFire, source);
        spawnEntityAt(laser, new GridPoint2(0, 0), true, true);
        PhysicsProjectileComponent laserPhysics = laser.getComponent(PhysicsProjectileComponent.class);
        int projectileSpeed = 5; // Should be abstracted from WeaponsStatsComponent in future implementation
        laserPhysics.fire(directionToFire, projectileSpeed);
        return laser;
    }

    protected Map<String, ArrayList<Vector2>> getEnemySpawnPosition(String roomName) {
        HashMap<String, ArrayList<Vector2>> positions = new HashMap<>();
        ArrayList<Vector2> respectiveSpawns = new ArrayList<>();
        switch (roomName) {
            case "Forest" -> {
                respectiveSpawns.add(new Vector2(2.5f, 11f));
                respectiveSpawns.add(new Vector2(5.4f, 11f));
                respectiveSpawns.add(new Vector2(8.2f, 11f));
                respectiveSpawns.add(new Vector2(11.1f, 10f));
                positions.put(DEEP_SPIN, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(12f, 5f));
                positions.put(TURRET, respectiveSpawns);
            }
            case "Reception" -> {
                respectiveSpawns.add(new Vector2(5.7f, 5f));
                respectiveSpawns.add(new Vector2(1.5f, 7f));
                positions.put(VROOMBA, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(11.5f, 10f));
                positions.put(GHOST_GPT, respectiveSpawns);
            }
            case "Mainhall" -> {
                respectiveSpawns.add(new Vector2(10f, 10f));
                respectiveSpawns.add(new Vector2(2f, 10f));
                positions.put(DEEP_SPIN, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(11f, 6f));
                respectiveSpawns.add(new Vector2(2f, 8f));
                positions.put(VROOMBA, respectiveSpawns);
            }
            case "Security" -> {
                respectiveSpawns.add(new Vector2(12f, 10f));
                respectiveSpawns.add(new Vector2(2f, 5f));
                positions.put(GHOST_GPT, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(7f, 11f));
                respectiveSpawns.add(new Vector2(3f, 10f));
                positions.put(DEEP_SPIN, respectiveSpawns);
            }
            case "Office" -> {
                respectiveSpawns.add(new Vector2(12f, 3f));
                positions.put(GHOST_GPT, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(6f, 6f));
                positions.put(VROOMBA, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(8.2f, 11f));
                respectiveSpawns.add(new Vector2(2f, 10f));
                positions.put(DEEP_SPIN, respectiveSpawns);
            }
            case "Elevator" -> {
                respectiveSpawns.add(new Vector2(13f, 4f));
                respectiveSpawns.add(new Vector2(11f, 8f));
                positions.put(GHOST_GPT, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(8.4f, 10f));
                respectiveSpawns.add(new Vector2(2f, 8f));
                positions.put(GROK_DROID, respectiveSpawns);
            }
            case "Research" -> {
                respectiveSpawns.add(new Vector2(12f, 11f));
                positions.put(TURRET, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(2f, 7f));
                respectiveSpawns.add(new Vector2(11f, 4f));
                positions.put(GHOST_GPT, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(3f, 10f));
                respectiveSpawns.add(new Vector2(5f, 10f));
                positions.put(GROK_DROID, respectiveSpawns);
            }
            case "Shipping" -> {
                respectiveSpawns.add(new Vector2(12f, 11f));
                respectiveSpawns.add(new Vector2(2f, 5f));
                positions.put(GHOST_GPT, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(12f, 5f));
                positions.put(VROOMBA, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(3f, 10f));
                respectiveSpawns.add(new Vector2(5f, 10f));
                positions.put(GROK_DROID, respectiveSpawns);
            }
            case "Storage" -> {
                respectiveSpawns.add(new Vector2(12f, 5f));
                positions.put(TURRET, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(12f, 11f));
                respectiveSpawns.add(new Vector2(12f, 8f));
                respectiveSpawns.add(new Vector2(8f, 8f));
                positions.put(GROK_DROID, respectiveSpawns);
            }
            case "Server" -> {
                respectiveSpawns.add(new Vector2(12f, 11f));
                respectiveSpawns.add(new Vector2(7.6f, 4f));
                respectiveSpawns.add(new Vector2(11f, 8f));
                positions.put(GHOST_GPT, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(2f, 4f));
                positions.put(TURRET, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(3f, 10f));
                respectiveSpawns.add(new Vector2(5f, 10f));
                positions.put(DEEP_SPIN, respectiveSpawns);
            }
            case "Tunnel" -> {
                respectiveSpawns.add(new Vector2(12f, 4f));
                respectiveSpawns.add(new Vector2(3f, 4f));
                positions.put(GHOST_GPT, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(10f, 10f));
                positions.put(TURRET, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(4f, 10f));
                positions.put(VROOMBA, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(6f, 10f));
                positions.put(GROK_DROID, respectiveSpawns);
            }
            default -> {
                respectiveSpawns.add(new Vector2(12f, 11f));
                respectiveSpawns.add(new Vector2(7.6f, 4f));
                respectiveSpawns.add(new Vector2(2f, 4f));
                positions.put(GHOST_GPT, respectiveSpawns);
                respectiveSpawns = new ArrayList<>();
                respectiveSpawns.add(new Vector2(5f, 10f));
                respectiveSpawns.add(new Vector2(2f, 10f));
                positions.put(GROK_DROID, respectiveSpawns);
            }
        }
        return positions;
    }

    /**
     * Remove an entity
     *
     * @param entity to be removed
     */
    public void removeEntity(Entity entity) {
        entity.setEnabled(false);
        areaEntities.remove(entity);
        Gdx.app.postRunnable(entity::dispose);
    }

    /**
     * Gets all the current entities
     *
     * @return the entities on the map
     */
    public List<Entity> getEntities() {
        return this.areaEntities;
    }

    /**
     * Spawn an entity inside the specified room. Requires the terrain to be set first.
     *
     * @param entity entity to spawn (not yet registered)
     */
    public void spawnEntityInRoom(String roomName, Entity entity) {
        Vector2 pos = getRoomSpawnPosition(roomName);
        entity.setPosition(pos);
        spawnEntity(entity);
    }

    protected Vector2 getRoomSpawnPosition(String roomName) {
        switch (roomName) {
            case "Floor1":
                return randomInBounds(2f, 8f, 2f, 8f);
            case "Floor2":
                return randomInBounds(4f, 18f, 4f, 18f);
            case "Floor3":
                return randomInBounds(5f, 20f, 5f, 20f);
            case "Floor4":
                return randomInBounds(6f, 22f, 6f, 22f);
            case "Floor5":
                return randomInBounds(7f, 24f, 7f, 24f);
            case "Floor6":
                return randomInBounds(8f, 26f, 8f, 26f);
            case "Floor7":
                return randomInBounds(9f, 28f, 9f, 28f);
            default:
                return new Vector2(0f, 0f);
        }
    }

    private Vector2 randomInBounds(float minX, float maxX, float minY, float maxY) {
        float x = MathUtils.random(minX, maxX);
        float y = MathUtils.random(minY, maxY);
        return new Vector2(x, y);
    }

    /**
     * Convenience to load textures if not already loaded.
     */
    protected void ensureTextures(String[] texturePaths) {
        ResourceService rs = ServiceLocator.getResourceService();
        List<String> toLoad = new ArrayList<>();
        for (String path : texturePaths) {
            if (!rs.containsAsset(path, Texture.class)) {
                toLoad.add(path);
            }
        }
        if (!toLoad.isEmpty()) {
            rs.loadTextures(toLoad.toArray(new String[0]));
            rs.loadAll();
        }
    }

    /**
     * Convenience to load atlases if not already loaded.
     */
    protected void ensureAtlases(String[] atlasPaths) {
        ResourceService rs = ServiceLocator.getResourceService();
        List<String> toLoad = new ArrayList<>();
        for (String path : atlasPaths) {
            if (!rs.containsAsset(path, TextureAtlas.class)) {
                toLoad.add(path);
            }
        }
        if (!toLoad.isEmpty()) {
            rs.loadTextureAtlases(toLoad.toArray(new String[0]));
            rs.loadAll();
        }
    }

    /**
     * Ensure the common player atlas is available.
     */
    protected void ensurePlayerAtlas() {
        ResourceService rs = ServiceLocator.getResourceService();
        if (!rs.containsAsset("images/player.atlas", TextureAtlas.class)) {
            rs.loadTextureAtlases(new String[]{"images/player.atlas"});
            rs.loadAll();
        }
    }

    /**
     * Unload a set of assets if loaded.
     */
    protected void unloadAssets(String[] assetPaths) {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.unloadAssets(assetPaths);
    }

    /**
     * Create terrain of a given type and add an optional color overlay.
     */
    protected void setupTerrainWithOverlay(TerrainFactory factory,
                                           TerrainFactory.TerrainType type,
                                           Color overlayColor) {
        terrain = factory.createTerrain(type);
        spawnEntity(new Entity().addComponent(terrain));
        if (overlayColor != null) {
            Entity overlay = new Entity();
            overlay.setScale(1000f, 1000f);
            overlay.setPosition(-500f, -500f);
            overlay.addComponent(new SolidColorRenderComponent(overlayColor));
            spawnEntity(overlay);
        }
    }

    protected Bounds getCameraBounds(CameraComponent cameraComponent) {
        OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
        Vector2 camPos = cameraComponent.getEntity().getPosition();
        float viewWidth = cam.viewportWidth;
        float viewHeight = cam.viewportHeight;
        float leftX = camPos.x - viewWidth / 2f;
        float rightX = camPos.x + viewWidth / 2f;
        float verticalHeightOffset = 9.375f;
        float bottomY = camPos.y - verticalHeightOffset / 2f;
        float topY = camPos.y + verticalHeightOffset / 2f;
        return new Bounds(leftX, rightX, bottomY, topY, viewWidth, viewHeight, camPos);
    }

    /**
     * Solid walls on edges.
     */
    protected void addSolidWallLeft(Bounds b, float wallWidth) {
        Entity w = ObstacleFactory.createWall(wallWidth, b.viewHeight);
        w.setPosition(b.leftX, b.bottomY);
        spawnEntity(w);
    }

    protected void addSolidWallRight(Bounds b, float wallWidth) {
        Entity w = ObstacleFactory.createWall(wallWidth, b.viewHeight);
        w.setPosition(b.rightX - wallWidth, b.bottomY);
        spawnEntity(w);
    }

    protected void addSolidWallTop(Bounds b, float wallWidth) {
        Entity w = ObstacleFactory.createWall(b.viewWidth, wallWidth);
        w.setPosition(b.leftX, b.topY - wallWidth);
        spawnEntity(w);
    }

    protected void addSolidWallBottom(Bounds b, float wallWidth) {
        Entity w = ObstacleFactory.createWall(b.viewWidth, wallWidth);
        w.setPosition(b.leftX, b.bottomY);
        spawnEntity(w);
    }

    /**
     * Add a vertical door on the left edge, splitting the wall into two segments.
     */
    protected void addVerticalDoorLeft(Bounds b, float wallWidth, Runnable onEnter) {
        float doorHeight = Math.max(1f, b.viewHeight * 0.2f);
        float doorY = b.camPos.y - doorHeight / 2f;
        float topSegHeight = Math.max(0f, b.topY - (doorY + doorHeight));
        if (topSegHeight > 0f) {
            Entity top = ObstacleFactory.createWall(wallWidth, topSegHeight);
            top.setPosition(b.leftX, doorY + doorHeight);
            spawnEntity(top);
        }
        float bottomSegHeight = Math.max(0f, doorY - b.bottomY);
        if (bottomSegHeight > 0f) {
            Entity bottom = ObstacleFactory.createWall(wallWidth, bottomSegHeight);
            bottom.setPosition(b.leftX, b.bottomY);
            spawnEntity(bottom);
        }
        Entity door = ObstacleFactory.createDoorTrigger(wallWidth, doorHeight);
        door.setPosition(b.leftX + 0.001f, doorY);
        door.addComponent(new DoorComponent(onEnter));
        spawnEntity(door);
    }

    /**
     * Add a vertical door on the right edge.
     */
    protected void addVerticalDoorRight(Bounds b, float wallWidth, Runnable onEnter) {
        float doorHeight = Math.max(1f, b.viewHeight * 0.2f);
        float doorY = b.camPos.y - doorHeight / 2f;
        float topSegHeight = Math.max(0f, b.topY - (doorY + doorHeight));
        if (topSegHeight > 0f) {
            Entity top = ObstacleFactory.createWall(wallWidth, topSegHeight);
            top.setPosition(b.rightX - wallWidth, doorY + doorHeight);
            spawnEntity(top);
        }
        float bottomSegHeight = Math.max(0f, doorY - b.bottomY);
        if (bottomSegHeight > 0f) {
            Entity bottom = ObstacleFactory.createWall(wallWidth, bottomSegHeight);
            bottom.setPosition(b.rightX - wallWidth, b.bottomY);
            spawnEntity(bottom);
        }
        Entity door = ObstacleFactory.createDoorTrigger(wallWidth, doorHeight);
        door.setPosition(b.rightX - wallWidth - 0.001f, doorY);
        door.addComponent(new DoorComponent(onEnter));
        spawnEntity(door);
    }

    protected <T extends GameArea> void loadArea(Class<T> areaClass) {
        clearAndLoad(() -> {
            try {
                // Pass the concrete terrainFactory and cameraComponent
                return areaClass
                        .getConstructor(TerrainFactory.class, CameraComponent.class)
                        .newInstance(this.terrainFactory, this.cameraComponent);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create " + areaClass.getSimpleName(), e);
            }

        });
    }

    /**
     * Helper to clear current entities and transition to a new area.
     */
    protected void clearAndLoad(Supplier<GameArea> nextAreaSupplier) {
        if (!beginTransition()) return;

        for (Entity entity : areaEntities) {
            // Don't disable the player entity or partner NPCs during transitions
            if (entity != ServiceLocator.getPlayer() && !isPartnerNPC(entity)) {
                entity.setEnabled(false);
            }
        }

        /** Ensure transition happens on the render thread to avoid race conditions **/
        Gdx.app.postRunnable(() -> {
            /** Preserve player entity and partner NPCs instead of disposing them **/
            Entity currentPlayer = ServiceLocator.getPlayer();
            List<Entity> partnerNPCs = new ArrayList<>();

            /** Phase 1: dispose all entities except the player and partner NPCs **/
            for (Entity entity : areaEntities) {
                if (entity == currentPlayer) {
                    // Keep player
                } else if (isPartnerNPC(entity)) {
                    // Keep partner NPCs
                    partnerNPCs.add(entity);
                } else {
                    // Dispose everything else
                    entity.dispose();
                }
            }
            areaEntities.clear();

            /** Re-add the preserved player and partner NPCs to the new area's entity list **/
            if (currentPlayer != null) {
                areaEntities.add(currentPlayer);
            }
            areaEntities.addAll(partnerNPCs);

            /* Phase 2: on the next frame, build the next area to avoid Box2D world-locked/native races */
            Gdx.app.postRunnable(() -> {
                try {
                    GameArea next = nextAreaSupplier.get();
                    ServiceLocator.registerGameArea(next);
                    next.create();
                    // mark next area as discovered when entered
                    DiscoveryService ds = ServiceLocator.getDiscoveryService();
                    if (ds != null) {
                        ds.discover(next.toString());
                    }
                } finally {
                    endTransition();
                }
            });
        });
    }
    /**
     * Checks if an entity is a partner NPC that should be preserved during transitions.
     *
     * @param entity The entity to check
     * @return true if the entity is a partner NPC
     */
    protected boolean isPartnerNPC(Entity entity) {
        if (entity == null) return false;
        
        // Check if entity has CompanionFollowShootComponent (partner NPCs have this)
        return entity.getComponent(com.csse3200.game.components.friendlynpc.CompanionFollowShootComponent.class) != null;
    }

    /**
     * Spawns or repositions the player entity. If a player already exists globally,
     * it repositions them instead of creating a new one.
     *
     * @param spawnPosition The position to spawn/reposition the player
     * @return The player entity (either existing or newly created)
     */
    protected Entity spawnOrRepositionPlayer(GridPoint2 spawnPosition) {
        Entity existingPlayer = ServiceLocator.getPlayer();

        if (existingPlayer != null) {
            // Reposition existing player
            existingPlayer.setPosition(terrain.tileToWorldPosition(spawnPosition));
            existingPlayer.setEnabled(true);
            ServiceLocator.getEntityService().register(existingPlayer);
            spawnEntity(existingPlayer);
            return existingPlayer;
        } else {
            // Create new player (first time only)
            Entity newPlayer = PlayerFactory.createPlayer();
            spawnEntityAt(newPlayer, spawnPosition, true, true);
            return newPlayer;
        }
    }
    /**
     * Spawns decorative object doors (non-functional) at given positions.
     *
     * @param leftDoorPos  grid position for the left/bottom decorative door
     * @param rightDoorPos grid position for the right/top decorative door
     */
    protected void spawnObjectDoors(GridPoint2 leftDoorPos, GridPoint2 rightDoorPos) {
        if (rightDoorPos != null) {
            Entity rightDoor = ObstacleFactory.createDoor();
            spawnEntityAt(rightDoor, rightDoorPos, false, false);
        }

        if (leftDoorPos != null) {
            Entity leftDoor = ObstacleFactory.createDoor();
            spawnEntityAt(leftDoor, leftDoorPos, false, false);
        }
    }

    public void spawnItem(Entity item, GridPoint2 position) {
        spawnEntityAt(item, position, false, false);
    }

    @Override
    public String toString() {
        return "GameArea";
    }

    /**
     * allows manipulation of player character by loading function
     *
     * @return player entity
     */
    public abstract Entity getPlayer();

//  public abstract Entity spawnPlayer(List<String> inventory, int CPU, int health);

    /**
     * Transition to another area by its name (case-insensitive), if known.
     * Returns true if a transition was initiated.
     */
    public boolean transitionToArea(String areaName) {
        if (areaName == null || areaName.isBlank()) return false;

        String lower = areaName.strip()
                .replaceAll("[\\s_-]+", "")   // normalize "Main Hall", "main-hall", etc.
                .toLowerCase(Locale.ROOT);

        Class<? extends GameArea> target = switch (lower) {
            case "forest" -> ForestGameArea.class;
            case "elevator" -> ElevatorGameArea.class;
            case "office" -> OfficeGameArea.class;
            case "mainhall", "mainhallway", "hall" -> MainHall.class;
            case "reception" -> Reception.class;
            case "tunnel" -> TunnelGameArea.class;
            case "security" -> SecurityGameArea.class;
            case "storage" -> StorageGameArea.class;
            case "shipping" -> ShippingGameArea.class;
            case "server" -> ServerGameArea.class;
            case "research" -> ResearchGameArea.class;
            case "goodwinanimation" -> GoodWinAnimationScreen.class;
            case "badwinanimation" -> BadWinAnimationScreen.class;
            default -> {
                Gdx.app.log("GameArea", "transitionToArea: unknown area name '" + areaName + "'");
                yield null;
            }
        };

        if (target == null) return false;
        loadArea(target);
        return true;
    }

    /**
     * A helper record to store the calculated boundaries of the camera's viewport.
     *
     * @param leftX      The leftmost x-coordinate of the camera's viewport.
     * @param rightX     The rightmost x-coordinate of the camera's viewport.
     * @param bottomY    The bottommost y-coordinate of the camera's viewport.
     * @param topY       The topmost y-coordinate of the camera's viewport.
     * @param viewWidth  The full width of the camera's viewport.
     * @param viewHeight The full height of the camera's viewport.
     * @param camPos     The center position of the camera.
     */
    protected record Bounds(float leftX, float rightX, float bottomY, float topY, float viewWidth, float viewHeight,
                            Vector2 camPos) {
    }
}
