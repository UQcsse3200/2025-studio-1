package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Cheat command to toggle the door-unlock override on all doors.
 * Usage: door_override on|off
 */
public class DoorOverrideCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DoorOverrideCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments for 'door_override': {}", args);
            return false;
        }

        boolean enable = args.getFirst().trim().equalsIgnoreCase("on");

        // 1) Flip global keycard gate override (add the static flag/methods in KeycardGateComponent)
        //    public static void setGlobalOverride(boolean);
        KeycardGateComponent.setGlobalOverride(enable);

        // 2) (Optional) Also flip per-door override flags if any DoorComponent exists in scene
        EntityService es = ServiceLocator.getEntityService();
        int updated = 0;
        if (es != null) {
            Array<Entity> entities = es.getEntities();
            if (entities != null) {
                for (Entity e : entities) {
                    DoorComponent door = e.getComponent(DoorComponent.class);
                    if (door != null) {
                        door.setOverrideUnlocked(enable);
                        updated++;
                    }
                }
            }
        } else {
            logger.warn("door_override: EntityService not registered");
        }

        logger.info(
                "door_override: {} ({} door(s)); keycard override={}",
                enable ? "enabled" : "disabled",
                updated,
                enable
        );
        return true;
    }

    /**
     * Valid args are exactly one token: "on" or "off".
     */
    boolean isValid(ArrayList<String> args) {
        if (args == null || args.size() != 1) return false;
        String a = args.getFirst();
        return a != null && (a.equalsIgnoreCase("on") || a.equalsIgnoreCase("off"));
    }
}
