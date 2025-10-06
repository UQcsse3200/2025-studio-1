package com.csse3200.game.components.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.Map;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Minimap class represents a 2D overview of the game world, showing discovered
 * rooms as images arranged in a grid layout. Each room corresponds to a position in the
 * minimap grid and can be dynamically updated depending on whether the player has discovered it.
 * The minimap supports scaling (zoom in/out), panning centered on the player's current position,
 * and rendering only the visible rooms within the current viewport. Each minimap image is
 * represented as a com.badlogic.gdx.scenes.scene2d.ui.Image}.
 *
 */
public class Minimap {
    /** Path to the texture used for undiscovered or locked rooms. */
    private static final String LOCKED = "images/minimap-images/Locked.png";
    /** Logger instance for error and debugging output. */
    private static final Logger logger = LoggerFactory.getLogger(Minimap.class);
    /** Default image height for a room in pixels. */
    private static final int IMAGE_HEIGHT = 720;
    /** Default image width for a room in pixels. */
    private static final int IMAGE_WIDTH = 1280;

    /** Maps grid coordinates to corresponding minimap image paths. */
    private Map<Vector2, String> grid;
    /** Maps room names to their positions in the minimap grid. */
    private Map<String, Vector2> roomPositions;
    /** Current zoom scale of the minimap (1 = default, >1 = zoomed in). */
    private float scale;
    /** The current center of the minimap in terms of map coordinates. */
    private Vector2 centre;
    /** The height of the screen (in pixels). */
    private int screenHeight;
    /** The width of the screen (in pixels). */
    private int screenWidth;

    /**
     * Creates a new Minimap with the given screen dimensions.
     *
     * @param screenHeight the height of the screen in pixels
     * @param screenWidth the width of the screen in pixels
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
     * Creates a new {@code Minimap} using predefined room grid data.
     *
     * @param screenHeight the height of the screen in pixels
     * @param screenWidth the width of the screen in pixels
     * @param grid a map of grid coordinates to room names
     */
    public Minimap(int screenHeight, int screenWidth, Map<Vector2, String> grid) {
        this(screenHeight, screenWidth);
        for (Vector2 coordinates : grid.keySet()) {
            addRoom(coordinates, grid.get(coordinates));
        }
    }

    /**
     * Adds a room to the minimap at the specified grid coordinates.
     * Each grid coordinate can only hold one room, and each room name
     * must be unique across the minimap.
     *
     * @param coordinates the grid coordinates (integer x, y) of the room
     * @param roomName the name of the room
     * @return true if the room was successfully added, false otherwise
     */
    public boolean addRoom(Vector2 coordinates, String roomName) {
        // Prevent overlapping rooms
        if (grid.containsKey(coordinates)) {
            logger.error("Attempted to enter multiple rooms at the same coordinates");
            return false;
        }

        // Prevent duplicate room names
        if (roomPositions.containsKey(roomName)) {
            logger.error("Attempted to enter multiple rooms with the same name");
            return false;
        }

        grid.put(coordinates, LOCKED);
        roomPositions.put(roomName, coordinates);
        return true;
    }

    /**
     * Renders the minimap and returns a map of visible room images and their
     * on-screen positions. This method computes which rooms are currently visible
     * based on the minimap's center and scale.
     *
     * @return a mapping of images to their on-screen positions in pixels
     */
    public Map<String, Vector2> render() {
        if (centre == null) {
            logger.error("Attempted to render the map before opening it");
            return null;
        }

        Map<String, Vector2> output = new HashMap<>();

        // Determine how much of the minimap is visible horizontally and vertically
        float horizontalReach = screenWidth * (1 / scale) / 2;
        float verticalReach = screenHeight * (1 / scale) / 2;

        // Calculate visible region bounds in map coordinates
        float minX = (centre.x - horizontalReach) / IMAGE_WIDTH;
        float maxX = (centre.x + horizontalReach) / IMAGE_WIDTH;
        float minY = (centre.y - verticalReach) / IMAGE_HEIGHT;
        float maxY = (centre.y + verticalReach) / IMAGE_HEIGHT;

        // Include all visible rooms within the computed bounds
        for (int i = (int) Math.floor(minX); i <= (int) Math.ceil(maxX); i++) {
            for (int j = (int) Math.floor(minY); j <= (int) Math.ceil(maxY); j++) {
                Vector2 roomCoordinates = new Vector2(i, j);

                // Skip coordinates that don't contain a room
                if (!grid.containsKey(roomCoordinates)) {
                    continue;
                }

                // Compute the room's position on the screen
                float screenX = (roomCoordinates.x - minX) * (screenWidth / (maxX - minX));
                float screenY = (roomCoordinates.y - minY) * (screenHeight / (maxY - minY));
                Vector2 screenCoords = new Vector2(screenX, screenY);
                output.put(grid.get(roomCoordinates), screenCoords);
            }
        }

        return output;
    }

    /**
     * Zooms the minimap in or out by a specified percentage.
     *
     * @param percentage the percentage to zoom (positive = zoom in, negative = zoom out)
     */
    public void zoom(float percentage) {
        scale *= (100 + percentage) / 100;
    }

    /**
     * Returns the current zoom scale of the minimap.
     *
     * @return the minimap scale factor
     */
    public float getScale() {
        return scale;
    }

    /**
     * Resets the minimap zoom back to its default (scale = 1).
     */
    public void reset() {
        scale = 1;
        zoom(0);
    }

    /**
     * Opens (initializes) the minimap by centering on the player's position
     * and revealing all discovered rooms using the {@link DiscoveryService}.
     */
    public void open() {
        // Normalize the player's position
        Vector2 normalisedPlayerPosition = normalisePosition(ServiceLocator.getPlayer().getPosition());
        DiscoveryService discoveryService = ServiceLocator.getDiscoveryService();

        // Get the current room and set the center to the player's location
        String currentRoom = ServiceLocator.getGameArea().toString();
        centre = calculateCentre(currentRoom, normalisedPlayerPosition);
        scale = 1;

        // Replace unlocked rooms' images with their actual images
        for (String name : roomPositions.keySet()) {
            Vector2 coordinates = roomPositions.get(name);

            if (discoveryService.isDiscovered(name)) {
                grid.put(coordinates, "images/minimap-images/" + name + ".png");
            }
        }
    }

    /**
     * Calculates the minimap center position based on the current room
     * and player's normalized position within that room.
     *
     * @param currentRoom the current room name
     * @param normalisedPlayerPosition the normalized position of the player
     * @return the minimap center coordinates in map space
     */
    private Vector2 calculateCentre(String currentRoom, Vector2 normalisedPlayerPosition) {
        Vector2 roomPosition = roomPositions.get(currentRoom);
        float x = (roomPosition.x + normalisedPlayerPosition.x) * IMAGE_WIDTH;
        float y = (roomPosition.y + normalisedPlayerPosition.y) * IMAGE_HEIGHT;

        return new Vector2(x, y);
    }

    /**
     * Converts a player's screen position to a normalized coordinate (0.0–1.0)
     * relative to the screen dimensions.
     *
     * @param position the player's position in screen pixels
     * @return the normalized position as a Vector2
     */
    private Vector2 normalisePosition(Vector2 position) {
        float x = position.x;
        float y = position.y;
        return new Vector2(x / Gdx.graphics.getWidth(), y / Gdx.graphics.getHeight());
    }

    /**
     * Closes the minimap by clearing the current center point.
     */
    public void close() {
        centre = null;
    }
}