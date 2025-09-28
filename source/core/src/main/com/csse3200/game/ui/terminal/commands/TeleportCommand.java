package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Teleport the player.
 * Usage:
 *  - teleport                 -> move player to camera center
 *  - teleport center          -> same as above
 *  - teleport <x> <y>         -> move player to world coordinates (floats)
 *  - teleport <RoomName>      -> transition to another room/area by name (e.g., Reception, Office)
 */
public class TeleportCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(TeleportCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
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

        // Prefer the game-wide player if registered; otherwise, search entities for a keyboard-controlled player
        Entity player = ServiceLocator.getPlayer();
        if (player == null) {
            player = findPlayer(es.getEntities());
        }
        if (player == null) {
            logger.warn("teleport: player not found");
            return false;
        }

        // No args or 'center' -> camera center
        if (args == null || args.isEmpty() || (args.size() == 1 && equalsIgnoreCaseTrim(args.get(0), "center"))) {
            return teleportToCameraCenter(player);
        }

        // 2 numeric args -> coordinates
        if (args.size() == 2 && isNumeric(args.get(0)) && isNumeric(args.get(1))) {
            float x = Float.parseFloat(args.get(0));
            float y = Float.parseFloat(args.get(1));
            player.setPosition(new Vector2(x, y));
            logger.info("teleport: moved player to ({}, {})", x, y);
            return true;
        }

        // 1 arg non-numeric -> treat as area name
        if (args.size() == 1 && !isNumeric(args.get(0))) {
            String areaName = args.get(0).trim();
            boolean ok = area.transitionToArea(areaName);
            if (!ok) {
                logger.warn("teleport: unknown area '{}'. Try one of: Forest, Reception, Mainhall, Security, Office, Elevator, Research, Shipping, Storage, Server, Tunnel", areaName);
            } else {
                logger.info("teleport: transitioning to area '{}'", areaName);
            }
            return ok;
        }

        logger.debug("teleport: invalid arguments {}. Usage: teleport | teleport center | teleport <x> <y> | teleport <RoomName>", args);
        return false;
    }

    private boolean teleportToCameraCenter(Entity player) {
        RenderService rs = ServiceLocator.getRenderService();
        if (rs == null || rs.getCamera() == null) {
            logger.warn("teleport: RenderService or camera not available");
            return false;
        }
        OrthographicCamera cam = rs.getCamera();
        Vector2 target = new Vector2(cam.position.x, cam.position.y);
        player.setPosition(target);
        logger.info("teleport: moved player to camera center at ({}, {})", target.x, target.y);
        return true;
    }

    private static boolean equalsIgnoreCaseTrim(String a, String b) {
        return a != null && a.trim().equalsIgnoreCase(b);

    }

    private static boolean isNumeric(String s) {
        if (s == null) return false;
        try {
            Float.parseFloat(s.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
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
