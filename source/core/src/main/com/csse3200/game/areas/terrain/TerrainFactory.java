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

/**
 * Factory for creating game terrains.
 */
public class TerrainFactory {
    private static final GridPoint2 MAP_SIZE = new GridPoint2(30, 30);

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

    private static void fillBackground(TiledMapTileLayer layer, GridPoint2 mapSize, TerrainTile tile) {
        Cell cell = new Cell();
        cell.setTile(tile);
        layer.setCell(0, 0, cell);
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
                        new TextureRegion(resourceService.getAsset("backgrounds/Spawn.png", Texture.class));
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
                        new TextureRegion(resourceService.getAsset("backgrounds/Office.png", Texture.class));
                return createGameRooms(0.5f, officeBackground);
            case SERVER_ROOM:
                TextureRegion serverBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Server.png", Texture.class));
                return createGameRooms(0.5f, serverBackground);
            case TUNNEL_ROOM:
                TextureRegion tunnelBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Tunnel.png", Texture.class));
                return createGameRooms(0.5f, tunnelBackground);
            case SECURITY_ROOM:
                TextureRegion securityBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Security.png", Texture.class));
                return createGameRooms(0.5f, securityBackground);
            case RESEARCH_ROOM:
                TextureRegion researchBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Research.png", Texture.class));
                return createGameRooms(0.5f, researchBackground);
            case MAIN_HALL:
                TextureRegion hallBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/MainHall.png", Texture.class));
                return createGameRooms(0.5f, hallBackground);
            case SHIPPING:
                TextureRegion shippingBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Shipping.png", Texture.class));
                return createGameRooms(0.5f, shippingBackground);
            case ELEVATOR:
                TextureRegion elevatorBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Elevator.png", Texture.class));
                return createGameRooms(0.5f, elevatorBackground);
            case WIN_SCREEN:
                TextureRegion factoryBackground =
                        new TextureRegion(resourceService.getAsset("images/WinscreenAnimationBackground.png",
                                Texture.class));
                return createGameRooms(0.5f, factoryBackground);
            case CASINO:
                TextureRegion casinoBackground =
                        new TextureRegion(resourceService.getAsset("images/casino.png", Texture.class));
                return createGameRooms(0.5f, casinoBackground);
            case SECRET:
                TextureRegion secretBackground =
                        new TextureRegion(resourceService.getAsset("backgrounds/Secret.png", Texture.class));
                return createGameRooms(0.5f, secretBackground);
            default:
                return null;
        }
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

    private TiledMap createGameRoomsBackground(
            GridPoint2 tileSize, TextureRegion background) {
        TiledMap tiledMap = new TiledMap();
        TiledMapTileLayer layer = new TiledMapTileLayer(1, 1, tileSize.x, tileSize.y);
        TerrainTile backgroundTile = new TerrainTile(background);


        fillBackground(layer, MAP_SIZE, backgroundTile);

        tiledMap.getLayers().add(layer);
        return tiledMap;
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
        OFFICE,
        WIN_SCREEN,
        CASINO,
        SECRET
    }
}
