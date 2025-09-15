package com.csse3200.game.files;

import com.badlogic.gdx.utils.Json;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.services.SaveLoadService;

/**
 * Class extending and building on the current file loading and reading
 * component currently to do
 * reading al classes that will need to be received including:
 * * Weapons
 * *Inventory
 * *Current Map Area
 * *Health
 * *stamina
 * *wave #
 * *May need to receive the keycard as well
 * *GameTime ?? may not be needed depending on how other teams have implemented things
 * The  game service and Game itself should preload before loading and saving takes place
 */
public class GameLoader extends FileLoader {
    private Json fileReader = new Json();

    /**
     * Base constructor extending to allow unique class types
     * See Filehandle.java and json.java
     */
    public GameLoader(SaveLoadService.PlayerInfo loadStats) {

    }

    /**
     * loads the players stats
     */
    public void loadPLayer() {

    }


    /**
     * loads the current area the player is in
     */
    public void loadArea() {

    }

    /**
     * acts as a registry of all classes that are saved to make retrieval easier
     * this function is currently placeholder and may need to be put into
     * a seperate class
     */
    public void loadableObjects() {

    }




}
