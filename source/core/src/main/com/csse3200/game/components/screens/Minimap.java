package com.csse3200.game.components.screens;

import com.badlogic.gdx.math.Vector2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
 */
public class Minimap {
    /**
     * Path to the texture used for undiscovered or locked rooms.
     */
    private static final String LOCKED = "images/minimap-images/Locked.png";
    /**
     * Logger instance for error and debugging output.
     */
    private static final Logger logger = LoggerFactory.getLogger(Minimap.class);
    /**
     * Default image height for a room in pixels.
     */
    public static final int IMAGE_HEIGHT = 720;
    /**
     * Default image width for a room in pixels.
     */
    public static final int IMAGE_WIDTH = 1280;

    /**
     * Maps grid coordinates to corresponding minimap image paths.
     */
    private final Map<Vector2, String> grid;
    /**
     * Maps room names to their positions in the minimap grid.
     */
    private final Map<String, Vector2> roomPositions;
    /**
     * Current zoom scale of the minimap (1 = default, >1 = zoomed in).
     */
    private float scale;
    /**
     * The current center of the minimap in terms of map coordinates.
     */
    private Vector2 centre;
    /**
     * The height of the screen (in pixels).
     */
    private final int screenHeight;
    /**
     * The width of the screen (in pixels).
     */
    private final int screenWidth;

    /**
     * Creates a new Minimap with the given screen dimensions.
     *
     * @param screenHeight the height of the screen in pixels
     * @param screenWidth  the width of the screen in pixels
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
     * @param screenWidth  the width of the screen in pixels
     * @param grid         a map of grid coordinates to room names
     */
    public Minimap(int screenHeight, int screenWidth, Map<Vector2, String> grid) {
        this(screenHeight, screenWidth);
        for (Map.Entry<Vector2, String> room : grid.entrySet()) {
            addRoom(room.getKey(), room.getValue());
        }
    }

    /**
     * Creates a new {@code Minimap} using predefined config file.
     *
     * @param screenHeight the height of the screen in pixels
     * @param screenWidth  the width of the screen in pixels
     * @param filepath     a path to the config file
     */
    public Minimap(int screenHeight, int screenWidth, String filepath) {
        this(screenHeight, screenWidth);

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(", ");
                String roomName = tokens[0];
                int x = Integer.parseInt(tokens[1]);
                int y = Integer.parseInt(tokens[2]);
                Vector2 coordinates = new Vector2(x, y);

                addRoom(coordinates, roomName);
            }
        } catch (IOException e) {
            logger.error("IO Exception occurred");
        }

    }

    /**
     * Adds a room to the minimap at the specified grid coordinates.
     * Each grid coordinate can only hold one room, and each room name
     * must be unique across the minimap.
     *
     * @param coordinates the grid coordinates (integer x, y) of the room
     * @param roomName    the name of the room
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
    public Map<Vector2, String> render() {
        if (centre == null) {
            logger.error("Attempted to render the map before opening it");
            return null;
        }

        Map<Vector2, String> output = new HashMap<>();

        // Determine how much of the minimap is visible horizontally and vertically
        float horizontalReach = screenWidth * (1 / scale) / 2;
        float verticalReach = screenHeight * (1 / scale) / 2;

        // Calculate visible region bounds in map image coordinates
        float minX = (centre.x - horizontalReach);
        float maxX = (centre.x + horizontalReach);
        float minY = (centre.y - verticalReach);
        float maxY = (centre.y + verticalReach);

        // Include all visible rooms within the computed bounds
        // Convert the map image coordinates to grid room coordinates
        for (int i = (int) Math.floor(minX / IMAGE_WIDTH); i <= (int) Math.ceil(maxX / IMAGE_WIDTH); i++) {
            for (int j = (int) Math.floor(minY / IMAGE_HEIGHT); j <= (int) Math.ceil(maxY / IMAGE_HEIGHT); j++) {
                Vector2 roomCoordinates = new Vector2(i, j);

                // Skip coordinates that don't contain a room
                if (!grid.containsKey(roomCoordinates)) {
                    continue;
                }

                // Compute the room's position on the screen
                float screenX = (IMAGE_WIDTH * roomCoordinates.x + (float) IMAGE_WIDTH / 2 - minX) * scale;
                float screenY = (IMAGE_HEIGHT * roomCoordinates.y + (float) IMAGE_HEIGHT / 2 - minY) * scale;
                Vector2 screenCoords = new Vector2(screenX, screenY);
                output.put(screenCoords, grid.get(roomCoordinates));
            }
        }

        return output;
    }

    /**
     * Zooms the minimap in or out by a specified percentage.
     * Requires: percentage is in the range (-100, infinity)
     *
     * @param percentage the percentage to zoom (positive = zoom in, negative = zoom out)
     */
    public void zoom(float percentage) {
        if (percentage <= -100) {
            logger.error("Attempted to decrease zoom by more than or equal to 100%");
        }
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
     * Pans the images across by moving the centre of the minimap area.
     * It will shift the images across by the number of screen pixels specified in vector.
     *
     * @param vector magnitude and direction of pixel shift when panning.
     */
    public void pan(Vector2 vector) {
        centre.x += vector.x * (1 / scale);
        centre.y += vector.y * (1 / scale);
    }

    /**
     * Resets the minimap zoom back to its default (scale = 1) and centres on the player again.
     */
    public void reset() {
        scale = 1;
        zoom(0);

        String currentRoom = ServiceLocator.getGameArea().toString();
        centre = calculateCentre(currentRoom);
    }

    /**
     * Opens (initializes) the minimap by centering on the middle of the current room.
     * All discovered rooms have their image changed from the locked image to their respective images.
     */
    public void open() {
        DiscoveryService discoveryService = ServiceLocator.getDiscoveryService();

        // Get the current room and set the center to the player's location
        String currentRoom = ServiceLocator.getGameArea().toString();
        centre = calculateCentre(currentRoom);
        scale = 1;

        // Replace unlocked rooms' images with their actual images
        for (Map.Entry<String, Vector2> room : roomPositions.entrySet()) {
            Vector2 coordinates = room.getValue();

            if (discoveryService.isDiscovered(room.getKey())) {
                grid.put(coordinates, "images/minimap-images/" + room.getKey() + ".png");
            }
        }
    }

    /**
     * Returns the centre of the minimap.
     *
     * @return The centre of the minimap as a {@link Vector2}
     */
    public Vector2 getCentre() {
        return centre.cpy();
    }

    /**
     * Calculates the minimap center position based on the current room
     * and player's normalized position within that room.
     *
     * @param currentRoom the current room name
     * @return the minimap center coordinates in map space
     */
    private Vector2 calculateCentre(String currentRoom) {
        Vector2 roomPosition = roomPositions.get(currentRoom);
        float x = (roomPosition.x + 0.5f) * IMAGE_WIDTH;
        float y = (roomPosition.y + 0.5f) * IMAGE_HEIGHT;

        return new Vector2(x, y);
    }

    /**
     * Handles closing of the minimap.
     */
    public void close() {
        centre = null;
    }
}