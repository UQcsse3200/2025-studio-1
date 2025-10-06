package com.csse3200.game.components.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import com.badlogic.gdx.graphics.*;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Minimap {
    private static final String UNDISCOVERED = "images/minimap-images/Locked.png";
    private static final Logger logger = LoggerFactory.getLogger(Minimap.class);
    private static final int IMAGE_HEIGHT = 720;
    private static final int IMAGE_WIDTH = 1280;

    private Map<Vector2, Image> grid; // Stores the images in the minimap as pairs of screen coordinates and images
    private Map<String, Vector2> roomPositions;
    private float scale; // Holds the zoom percentage of the minimap
    private Vector2 centre; // Holds the centre of the minimap in terms of the coordinates on the image.
    private int screenHeight;
    private int screenWidth;

    /**
     *
     * @param screenHeight
     * @param screenWidth
     */
    public Minimap(int screenHeight, int screenWidth) {
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        scale = 1;
        centre = null;
        grid = new HashMap<>();
        roomPositions = new HashMap<>();
    }

    /**
     *
     * @param screenHeight
     * @param screenWidth
     * @param grid
     */
    public Minimap(int screenHeight, int screenWidth, Map<Vector2, String> grid) {
        this(screenHeight, screenWidth);
        for (Vector2 coordinates : grid.keySet()) {
            addRoom(coordinates, grid.get(coordinates));
        }
    }

    /**
     *
     * @param coordinates Vector2 containing the grid coordinates (ints) of each room
     * @param roomName the name of the room to be added
     * @return boolean corresponding to whether the add operation was successful
     */
    public boolean addRoom(Vector2 coordinates, String roomName) {
        // Cannot have two rooms in the same grid coordinate
        if (grid.containsKey(coordinates)) {
            logger.error("Attempted to enter multiple rooms at the same coordinates");
            return false;
        }
        // Cannot have two rooms with the same
        if (roomPositions.containsKey(roomName)) {
            logger.error("Attempted to enter multiple rooms with the same name");
            return false;
        }

        Image image = new Image(
                new TextureRegionDrawable(
                        new Texture("images/minimap-images/" + roomName + ".png")
                )
        );
        image.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        grid.put(coordinates, image);
        roomPositions.put(roomName, coordinates);
        return true;
    }

    /**
     *
     * @return a map from the image to the position
     * All coordinates are given in terms of screen coordinates, images are already scaled for rendering.
     */
    public Map<Image, Vector2> render() {
        Map<Image, Vector2> output = new HashMap<>();

        double horizontalReach = screenWidth * (1 / scale) / 2;
        double verticalReach = screenHeight * (1 / scale) / 2;
        int minX = (int) Math.floor((centre.x - horizontalReach) / IMAGE_WIDTH);
        int maxX = (int) Math.ceil((centre.x + horizontalReach) / IMAGE_WIDTH);
        int minY = (int) Math.floor((centre.y - verticalReach) / IMAGE_HEIGHT);
        int maxY = (int) Math.ceil((centre.y + verticalReach) / IMAGE_HEIGHT);

        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                Vector2 roomCoordinates = new Vector2(i, j);

                if (grid.containsKey(roomCoordinates)) {
                }
            }
        }

        return output;
    }

    /**
     *
     * @param percentage
     */
    public void zoom(double percentage) {
        scale *= (100 + percentage) / 100;

        for (Image image : grid.values()) {
            image.setScale(scale, scale);
        }
    }

    /**
     *
     * @return
     */
    public float getScale() {
        return scale;
    }

    /**
     *
     */
    public void reset() {
        scale = 1;
    }

    /**
     *
     */
    public void open() {
        Vector2 normalisedPlayerPosition = normalisePosition(ServiceLocator.getPlayer().getPosition());
        DiscoveryService discoveryService = ServiceLocator.getDiscoveryService();

        String currentRoom = ServiceLocator.getGameArea().toString();
        centre = calculateCentre(currentRoom, normalisedPlayerPosition); // Centres on the player's position by default
        scale = 100;
    }

    private Vector2 calculateCentre(String currentRoom, Vector2 normalisedPlayerPosition) {
        Vector2 roomPosition = roomPositions.get(currentRoom);
        float x = (roomPosition.x + normalisedPlayerPosition.x) * IMAGE_WIDTH;
        float y = (roomPosition.y + normalisedPlayerPosition.y) * IMAGE_HEIGHT;

        return new Vector2(x, y);
    }

    private Vector2 normalisePosition(Vector2 position) {
        float x = position.x;
        float y = position.y;
        return new Vector2(x / Gdx.graphics.getWidth(), y / Gdx.graphics.getHeight());
    }

    /**
     *
     */
    public void close() {
        centre = null;
    }
}
