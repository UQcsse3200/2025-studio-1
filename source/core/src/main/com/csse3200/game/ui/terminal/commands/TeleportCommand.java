package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.csse3200.game.ui.terminal.commands.util.CommandPlayers.resolve;

/**
 * Teleport the player.
 * Usage:
 * - teleport                 -> move player to camera center
 * - teleport center          -> same as above
 * - teleport x y             -> move player to world coordinates (floats)
 * - teleport RoomName        -> transition to another room/area by name (e.g., Reception, Office)
 */
public class TeleportCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(TeleportCommand.class);

    private static boolean equalsIgnoreCaseTrim(String a, String b) {
        return a != null && a.trim().equalsIgnoreCase(b);
    }

    private static boolean isNumeric(String s) {
        return parseFloat(s) != null;
    }

    private static Float parseFloat(String s) {
        if (s == null) return null;
        try {
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

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

        // Prefer a globally-registered player; fallback: find keyboard-controlled entity
        Entity player = ServiceLocator.getPlayer();
        if (player == null) {
            player = resolve(es);
        }
        if (player == null) {
            logger.warn("teleport: player not found");
            return false;
        }

        // No args -> center
        if (args == null || args.isEmpty()) {
            return teleportToCameraCenter(player);
        }

        // Java 21: use List.getFirst() for cleaner branching
        switch (args.size()) {
            case 1 -> {
                String a0 = args.getFirst();
                if (equalsIgnoreCaseTrim(a0, "center")) {
                    return teleportToCameraCenter(player);
                }
                if (!isNumeric(a0)) {
                    String areaName = a0.trim();
                    boolean ok = area.transitionToArea(areaName);
                    if (!ok) {
                        logger.warn("teleport: unknown area '{}'. Try one of: Forest, Reception, Mainhall, Security, Office, Elevator, Research, Shipping, Storage, Server, Tunnel", areaName);
                    } else {
                        logger.info("teleport: transitioning to area '{}'", areaName);
                    }
                    return ok;
                }
                // single numeric arg is invalid
                logger.debug("teleport: invalid single-arg '{}'. Usage: teleport | teleport center | teleport <x> <y> | teleport <RoomName>", a0);
                return false;
            }
            case 2 -> {
                Float x = parseFloat(args.get(0));
                Float y = parseFloat(args.get(1));
                if (x != null && y != null) {
                    player.setPosition(new Vector2(x, y));
                    logger.info("teleport: moved player to ({}, {})", x, y);
                    return true;
                }
                logger.debug("teleport: invalid coordinates {}. Usage: teleport <x> <y>", args);
                return false;
            }
            default -> {
                logger.debug("teleport: invalid arguments {}. Usage: teleport | teleport center | teleport <x> <y> | teleport <RoomName>", args);
                return false;
            }
        }
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
}
