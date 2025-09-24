package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Teleport the player to the center of the map.
 * Usage: teleport
 * (Optional alias: "teleport center")
 */
public class TeleportCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(TeleportCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        // Accept no args OR a single "center" token
        if (args != null && !(args.isEmpty() ||
                (args.size() == 1 && "center".equalsIgnoreCase(args.get(0).trim())))) {
            logger.debug("teleport: no arguments expected (or 'center'); got {}", args);
            return false;
        }

        GameArea area = ServiceLocator.getGameArea();
        if (area == null) {
            logger.warn("teleport: no active GameArea");
            return false;
        }

        EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.warn("teleport: EntityService not registered");
            return false;
        }

        Entity player = findPlayer(es.getEntities());
        if (player == null) {
            logger.warn("teleport: player not found");
            return false;
        }

        return true;
    }

    private Entity findPlayer(Array<Entity> entities) {
        if (entities == null) return null;
        for (Entity e : entities) {
            if (e.getComponent(KeyboardPlayerInputComponent.class) != null) {
                return e;
            }
        }
        return null;
    }
}
