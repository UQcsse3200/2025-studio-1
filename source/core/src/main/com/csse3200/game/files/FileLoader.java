package com.csse3200.game.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.ItemPickUpComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.SaveLoadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Wrapper for reading Java objects from JSON files.
 *
 * <p>A generic method is provided already, but methods for reading specific classes can be added
 * for more control.
 */
public class FileLoader {
    private static final Logger logger = LoggerFactory.getLogger(FileLoader.class);
    static final Json json = new Json();
    public static Json jsonSave = new Json();

    /**
     * Read generic Java classes from a JSON file. Properties in the JSON file will override class
     * defaults.
     *
     * @param type     class type
     * @param filename file to read from
     * @param <T>      Class type to read JSON into
     * @return instance of class, may be null
     */
    public static <T> T readClass(Class<T> type, String filename) {
        return readClass(type, filename, Location.INTERNAL);
    }

    /**
     * Read generic Java classes from a JSON file. Properties in the JSON file will override class
     * defaults.
     *
     * @param type     class type
     * @param filename file to read from
     * @param location File storage type. See
     *                 https://github.com/libgdx/libgdx/wiki/File-handling#file-storage-types
     * @param <T>      Class type to read JSON into
     * @return instance of class, may be null
     */
    public static <T> T readClass(Class<T> type, String filename, Location location) {
        logger.debug("Reading class {} from {}", type.getSimpleName(), filename);
        FileHandle file = getFileHandle(filename, location);

        if (file == null) {
            logger.error("Failed to create file handle for {}", filename);
            return null;
        }
        T object;
        try {
            object = json.fromJson(type, file);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        if (object == null) {
            String path = file.path();
            logger.error("Error creating {} class instance from {}", type.getSimpleName(), path);
        }
        return object;
    }

    /**
     * Read playerinfo from a JSON file and write into class.
     *
     * @param player   class type
     * @param filename file to read from
     * @param location File storage type. See
     * @param <T>      Class type to read JSON into
     * @return instance of class, may be null
     */
    public static <T> T readPlayer(Class<SaveLoadService.PlayerInfo> player, String filename, Location location) {
        logger.debug("Reading class {} from {}", player.getSimpleName(), filename);
        FileHandle file = getFileHandle(filename, location);

        if (file == null) {
            logger.error("Failed to create file handle for {}", filename);
            return null;
        }
        Object object;
        try {
            object = json.fromJson(player, file);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }


        if (object == null) {
            String path = file.path();
            logger.error("Error creating {} class instance from {}", player.getSimpleName(), path);
        }
        logger.info(json.prettyPrint(object));

        return (T) object;
    }


    /**
     * Reads Inventory component of a save file json
     * - currently a placeholder for refacotring end sprint 2 / into sprint 3
     *
     * @param inventory string representation of the items
     * @param playerInventory Inventory that will be populated
     */
    public static InventoryComponent readInventory(List<String> inventory, InventoryComponent playerInventory) {
        ItemPickUpComponent testLoading = new ItemPickUpComponent(playerInventory);
        //repopulates the inventory
        if (inventory != null) {
            for (int i = 0; i < inventory.size(); i++) {
                Entity placehold = testLoading.createItemFromTexture(inventory.get(i));
                playerInventory.addItem(placehold);
            }
        }
        return playerInventory;
    }


    /**
     * Write generic Java classes to a JSON file.
     *
     * @param object   Java object to write.
     * @param filename File to write to.
     */
    public static void writeClass(Object object, String filename) {
        writeClass(object, filename, Location.EXTERNAL);
    }

    /**
     * Write generic Java classes to a JSON file.
     *
     * @param object   Java object to write.
     * @param filename File to write to.
     * @param location File storage type. See
     *                 https://github.com/libgdx/libgdx/wiki/File-handling#file-storage-types
     */
    public static void writeClass(Object object, String filename, Location location) {
        logger.debug("Reading class {} from {}", object.getClass().getSimpleName(), filename);
        FileHandle file = getFileHandle(filename, location);
        assert file != null;
        file.writeString(json.prettyPrint(object), false);
    }

    /**
     * Write specific player instance to a JSON file.
     *
     * @param playerInfo information required to load the player back in
     * @param filename   File to write to.
     * @param location   File storage type. See
     *                   https://github.com/libgdx/libgdx/wiki/File-handling#file-storage-types
     */
    public static void writeClass(SaveLoadService.PlayerInfo playerInfo, String filename, Location location) {
        logger.debug("Reading class {} from {}", playerInfo.getClass().getSimpleName(), filename);
        FileHandle file = getFileHandle(filename, location);
        assert file != null;
        file.writeString(json.prettyPrint(playerInfo), false);
    }


    private static FileHandle getFileHandle(String filename, Location location) {
        switch (location) {
            case CLASSPATH:
                return Gdx.files.classpath(filename);
            case INTERNAL:
                return Gdx.files.internal(filename);
            case LOCAL:
                return Gdx.files.local(filename);
            case EXTERNAL:
                return Gdx.files.external(filename);
            case ABSOLUTE:
                return Gdx.files.absolute(filename);
            default:
                return null;
        }
    }

    public enum Location {
        CLASSPATH,
        INTERNAL,
        LOCAL,
        EXTERNAL,
        ABSOLUTE
    }
}
