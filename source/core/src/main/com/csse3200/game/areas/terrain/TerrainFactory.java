package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainComponent.TerrainOrientation;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.RandomUtils;

/**
 * Factory for creating game terrains.
 */
public class TerrainFactory {
    private static final GridPoint2 MAP_SIZE = new GridPoint2(30, 30);
    private static final int TUFT_TILE_COUNT = 30;
    private static final int ROCK_TILE_COUNT = 30;

    private final OrthographicCamera camera;
    private final TerrainOrientation orientation;

    /**
     * Create a terrain factory with Orthogonal orientation
     *
     * @param cameraComponent Camera to render terrains to. Must be ortographic.
     */
    public TerrainFactory(CameraComponent cameraComponent) {
        this(cameraComponent, TerrainOrientation.ORTHOGONAL);
    }

    /**
     * Create a terrain factory
     *
     * @param cameraComponent Camera to render terrains to. Must be orthographic.
     * @param orientation     orientation to render terrain at
     */
    public TerrainFactory(CameraComponent cameraComponent, TerrainOrientation orientation) {
        this.camera = (OrthographicCamera) cameraComponent.getCamera();
        this.orientation = orientation;
    }

    /**
     * Create a terrain of the given type, using the orientation of the factory. This can be extended
     * to add additional game terrains.
     *
     * @param terrainType Terrain to create
     * @return Terrain component which renders the terrain
     */
    public TerrainComponent createTerrain(TerrainType terrainType) {
        ResourceService resourceService = ServiceLocator.getResourceService();
        switch (terrainType) {
            case SPAWN_ROOM:
                TextureRegion spawnBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/SpawnResize.png", Texture.class));
                return createGameRooms(0.5f, spawnBackground);
            case STORAGE:
                TextureRegion storageBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Storage.png", Texture.class));
                return createGameRooms(0.5f, storageBackground);
            case LOBBY:
                TextureRegion lobbyBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Reception.png", Texture.class));
                return createGameRooms(0.5f, lobbyBackground);
            case OFFICE:
                TextureRegion officeBackground =
                        new TextureRegion(resourceService.getAsset("images/Office and elevator/Office Background.png", Texture.class));
                return createGameRooms(0.5f, officeBackground);
            case SERVER_ROOM:
                TextureRegion serverBackground =
                        new TextureRegion(resourceService.getAsset("images/ServerRoomBackgroundResize.png", Texture.class));
                return createGameRooms(0.5f, serverBackground);
            case TUNNEL_ROOM:
                TextureRegion tunnelBackground =
                        new TextureRegion(resourceService.getAsset("images/TunnelRoomBackgResize.png", Texture.class));
                return createGameRooms(0.5f, tunnelBackground);
            case SECURITY_ROOM:
                TextureRegion securityBackground =
                        new TextureRegion(resourceService.getAsset("images/SecurityBackground.png", Texture.class));
                return createGameRooms(0.5f, securityBackground);
            case RESEARCH_ROOM:
                TextureRegion researchBackground =
                        new TextureRegion(resourceService.getAsset("images/ResearchBackground.png", Texture.class));
                return createGameRooms(0.5f, researchBackground);
            case MAIN_HALL:
                TextureRegion hallBackground =
                        new TextureRegion(resourceService.getAsset("images/mainHall-background.png", Texture.class));
                return createGameRooms(0.5f, hallBackground);
            case SHIPPING:
                TextureRegion shippingBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Shipping.png", Texture.class));
                return createGameRooms(0.5f, shippingBackground);
            case ELEVATOR:
                TextureRegion elevatorBackground =
                        new TextureRegion(resourceService.getAsset("images/Elevator background.png", Texture.class));
                return createGameRooms(0.5f, elevatorBackground);
            case FOREST_DEMO:
                TextureRegion orthoGrass =
                        new TextureRegion(resourceService.getAsset("images/grass_1.png", Texture.class));
                TextureRegion orthoTuft =
                        new TextureRegion(resourceService.getAsset("images/grass_2.png", Texture.class));
                TextureRegion orthoRocks =
                        new TextureRegion(resourceService.getAsset("images/grass_3.png", Texture.class));
                return createForestDemoTerrain(0.5f, orthoGrass, orthoTuft, orthoRocks);
            case FOREST_DEMO_ISO:
                TextureRegion isoGrass =
                        new TextureRegion(resourceService.getAsset("images/iso_grass_1.png", Texture.class));
                TextureRegion isoTuft =
                        new TextureRegion(resourceService.getAsset("images/iso_grass_2.png", Texture.class));
                TextureRegion isoRocks =
                        new TextureRegion(resourceService.getAsset("images/iso_grass_3.png", Texture.class));
                return createForestDemoTerrain(1f, isoGrass, isoTuft, isoRocks);
            case FOREST_DEMO_HEX:
                TextureRegion hexGrass =
                        new TextureRegion(resourceService.getAsset("images/hex_grass_1.png", Texture.class));
                TextureRegion hexTuft =
                        new TextureRegion(resourceService.getAsset("images/hex_grass_2.png", Texture.class));
                TextureRegion hexRocks =
                        new TextureRegion(resourceService.getAsset("images/hex_grass_3.png", Texture.class));
                return createForestDemoTerrain(1f, hexGrass, hexTuft, hexRocks);
            default:
                return null;
        }
    }

    private TerrainComponent createForestDemoTerrain(
            float tileWorldSize, TextureRegion grass, TextureRegion grassTuft, TextureRegion rocks) {
        GridPoint2 tilePixelSize = new GridPoint2(grass.getRegionWidth(), grass.getRegionHeight());
        TiledMap tiledMap = createForestDemoTiles(tilePixelSize, grass, grassTuft, rocks);
        TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
        return new TerrainComponent(camera, tiledMap, renderer, orientation, tileWorldSize);
    }

    private TerrainComponent createGameRooms(
            float tileWorldSize, TextureRegion background) {
        GridPoint2 tilePixelSize = new GridPoint2(background.getRegionWidth(), background.getRegionHeight());
        TiledMap tiledMap = createGameRoomsBackground(tilePixelSize, background);
        TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / 30);
        return new TerrainComponent(camera, tiledMap, renderer, orientation, tileWorldSize);
    }

    private TiledMapRenderer createRenderer(TiledMap tiledMap, float tileScale) {
        switch (orientation) {
            case ORTHOGONAL:
                return new OrthogonalTiledMapRenderer(tiledMap, tileScale);
            case ISOMETRIC:
                return new IsometricTiledMapRenderer(tiledMap, tileScale);
            case HEXAGONAL:
                return new HexagonalTiledMapRenderer(tiledMap, tileScale);
            default:
                return null;
        }
    }

    private TiledMap createForestDemoTiles(
            GridPoint2 tileSize, TextureRegion grass, TextureRegion grassTuft, TextureRegion rocks) {
        TiledMap tiledMap = new TiledMap();
        TerrainTile grassTile = new TerrainTile(grass);
        TerrainTile grassTuftTile = new TerrainTile(grassTuft);
        TerrainTile rockTile = new TerrainTile(rocks);
        TiledMapTileLayer layer = new TiledMapTileLayer(MAP_SIZE.x, MAP_SIZE.y, tileSize.x, tileSize.y);

        // Create base grass
        fillTiles(layer, MAP_SIZE, grassTile);

        // Add some grass and rocks
        fillTilesAtRandom(layer, MAP_SIZE, grassTuftTile, TUFT_TILE_COUNT);
        fillTilesAtRandom(layer, MAP_SIZE, rockTile, ROCK_TILE_COUNT);

        tiledMap.getLayers().add(layer);
        return tiledMap;
    }

    private TiledMap createGameRoomsBackground(
            GridPoint2 tileSize, TextureRegion background) {
        TiledMap tiledMap = new TiledMap();
        TiledMapTileLayer layer = new TiledMapTileLayer(1, 1, tileSize.x, tileSize.y);
        TerrainTile backgroundTile = new TerrainTile(background);


        fillBackground(layer, MAP_SIZE, backgroundTile);

        tiledMap.getLayers().add(layer);
        return tiledMap;
    }

    private static void fillTilesAtRandom(
            TiledMapTileLayer layer, GridPoint2 mapSize, TerrainTile tile, int amount) {
        GridPoint2 min = new GridPoint2(0, 0);
        GridPoint2 max = new GridPoint2(mapSize.x - 1, mapSize.y - 1);

        for (int i = 0; i < amount; i++) {
            GridPoint2 tilePos = RandomUtils.random(min, max);
            Cell cell = layer.getCell(tilePos.x, tilePos.y);
            cell.setTile(tile);
        }
    }

    private static void fillTiles(TiledMapTileLayer layer, GridPoint2 mapSize, TerrainTile tile) {
        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                Cell cell = new Cell();
                cell.setTile(tile);
                layer.setCell(x, y, cell);
            }
        }
    }

    private static void fillBackground(TiledMapTileLayer layer, GridPoint2 mapSize, TerrainTile tile) {
        Cell cell = new Cell();
        cell.setTile(tile);
        layer.setCell(0, 0, cell);
    }

    /**
     * This enum should contain the different terrains in your game, e.g. forest, cave, home, all with
     * the same oerientation. But for demonstration purposes, the base code has the same level in 3
     * different orientations.
     */
    public enum TerrainType {
        FOREST_DEMO,
        FOREST_DEMO_ISO,
        FOREST_DEMO_HEX,
        SPAWN_ROOM,
        SERVER_ROOM,
        LOBBY,
        SECURITY_ROOM,
        SHIPPING,
        ELEVATOR,
        MAIN_HALL,
        TUNNEL_ROOM,
        STORAGE,
        RESEARCH_ROOM,
        OFFICE
    }
}
