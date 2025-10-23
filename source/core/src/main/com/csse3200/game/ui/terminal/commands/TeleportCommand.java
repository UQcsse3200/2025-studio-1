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
import java.util.List;

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
    private static final String USAGE =
            "Usage: teleport | teleport center | teleport <x> <y> | teleport <RoomName>";

    /**
     * Case-insensitive comparison after trimming whitespace.
     * @param a First string to be compared
     * @param b Second string to be compared
     * @return {@code true} if they are equaled
     */
    private static boolean equalsIgnoreCaseTrim(String a, String b) {
        return a != null && a.trim().equalsIgnoreCase(b);
    }

    /**
     * Checks if the string can be parsed as a float.
     * @param s string to be checked
     * @return {@code true} if it can be parsed as a float
     */
    private static boolean isNumeric(String s) {
        return parseFloat(s) != null;
    }

    /**
     * Parses a string to a Float
     * @param s the string to be parsed to a Float
     * @return the parsed Float if successful, or {@code null} if invalid
     */
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
        Ctx ctx = resolveContext();
        if (ctx == null) return false;

        if (args == null || args.isEmpty()) {
            return teleportToCameraCenter(ctx.player());
        }

        // Java 21: List.getFirst()
        return switch (args.size()) {
            case 1 -> handleSingleArg(args.getFirst(), ctx);
            case 2 -> handleTwoArgs(args, ctx.player());
            default -> invalidArgs(args);
        };
    }


    /**
     * Resolves the game context
     * @return Ctx a record containing GameArea area and Entity player
     */
    private Ctx resolveContext() {
        GameArea area = ServiceLocator.getGameArea();
        if (area == null) {
            logger.warn("teleport: no active GameArea");
            return null;
        }

        EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.warn("teleport: EntityService not registered");
            return null;
        }

        Entity player = ServiceLocator.getPlayer();
        if (player == null) player = resolve(es);
        if (player == null) {
            logger.warn("teleport: player not found");
            return null;
        }
        return new Ctx(area, player);
    }

    /**
     * Handles teleport command with a single argument
     * @param raw the argument
     * @param ctx context containing area and player
     * @return {@code true} if teleport successful, {@code false} otherwise
     */
    private boolean handleSingleArg(String raw, Ctx ctx) {
        if (equalsIgnoreCaseTrim(raw, "center")) {
            return teleportToCameraCenter(ctx.player());
        }
        if (!isNumeric(raw)) {
            String areaName = raw.trim();
            boolean ok = ctx.area().transitionToArea(areaName);
            if (!ok) {
                logger.warn("teleport: unknown area '{}'. Try one of: Forest, Reception, Mainhall, Security, Office, Elevator, Research, Shipping, Storage, Server, Tunnel", areaName);
            } else {
                logger.info("teleport: transitioning to area '{}'", areaName);
            }
            return ok;
        }
        logger.debug("teleport: invalid single-arg '{}'. {}", raw, USAGE);
        return false;
    }

    /**
     * Handles teleport command with two arguments.
     * @param args the argument
     * @param player player entity to move
     * @return @code true} if teleport successful, {@code false} otherwise
     */
    private boolean handleTwoArgs(List<String> args, Entity player) {
        Float x = parseFloat(args.getFirst());
        Float y = parseFloat(args.get(1));
        if (x == null || y == null) {
            logger.debug("teleport: invalid coordinates {}. {}", args, USAGE);
            return false;
        }
        player.setPosition(new com.badlogic.gdx.math.Vector2(x, y));
        logger.info("teleport: moved player to ({}, {})", x, y);
        return true;
    }

    /**
     * Handles invalid argument lengths.
     * @param args arguments received
     * @return false
     */
    private boolean invalidArgs(List<String> args) {
        logger.debug("teleport: invalid arguments {}. {}", args, USAGE);
        return false;
    }

    /**
     * Moves the player to the current camera center position.
     *
     * @param player player entity to move
     * @return {@code true} if teleport successful, {@code false} if camera or RenderService unavailable
     */
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

    private record Ctx(GameArea area, Entity player) {
    }
}
