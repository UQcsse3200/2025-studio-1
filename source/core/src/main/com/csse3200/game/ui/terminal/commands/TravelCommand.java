package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.csse3200.game.ui.terminal.commands.util.CommandPlayers.resolve;

public class TravelCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(TravelCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        GameArea area = ServiceLocator.getGameArea();
        if (area == null) { logger.warn("travel: no active GameArea"); return false; }

        EntityService es = ServiceLocator.getEntityService();
        if (es == null) { logger.warn("travel: EntityService not registered"); return false; }

        Entity player = resolvePlayer(es);
        if (player == null) { logger.warn("travel: player not found"); return false; }

        if (args == null || args.isEmpty()) return toCameraCenter(player);
        if (args.size() == 2)               return toCoordinates(args, player);
        if (args.size() == 1)               return handleSingleArg(area, args.getFirst(), player);

        logger.debug("travel: invalid args {}. Usage: travel | travel center | travel <x> <y> | travel <RoomName>", args);
        return false;
    }

    /* ---------- Single-purpose helpers (keep action() tiny) ---------- */

    private Entity resolvePlayer(EntityService es) {
        Entity p = ServiceLocator.getPlayer();
        return (p != null) ? p : resolve(es);
    }

    private boolean handleSingleArg(GameArea area, String a0, Entity player) {
        if (equalsIgnoreCaseTrim(a0)) return toCameraCenter(player);
        if (isNumeric(a0)) {
            logger.debug("travel: invalid single arg '{}'. Usage: travel center | travel <RoomName>", a0);
            return false;
        }
        return transitionIfDiscovered(area, a0.trim());
    }

    private boolean toCoordinates(ArrayList<String> args, Entity player) {
        Float x = parseFloat(args.get(0));
        Float y = parseFloat(args.get(1));
        if (x == null || y == null) {
            logger.debug("travel: invalid coordinates {}. Usage: travel <x> <y>", args);
            return false;
        }
        player.setPosition(new Vector2(x, y));
        logger.info("travel: moved player to ({}, {})", x, y);
        return true;
    }

    private boolean transitionIfDiscovered(GameArea area, String room) {
        DiscoveryService ds = ServiceLocator.getDiscoveryService();
        if (ds == null) { logger.warn("travel: DiscoveryService missing; cannot verify progression"); return false; }
        if (!ds.isDiscovered(room)) { logger.info("travel: '{}' not discovered yet. Discovered={}", room, ds.getDiscovered()); return false; }
        boolean ok = area.transitionToArea(room);
        if (ok) logger.info("travel: transitioning to discovered area '{}'", room);
        else    logger.warn("travel: unknown area '{}'", room);
        return ok;
    }

    private boolean toCameraCenter(Entity player) {
        RenderService rs = ServiceLocator.getRenderService();
        if (rs == null || rs.getCamera() == null) { logger.warn("travel: RenderService or camera not available"); return false; }
        OrthographicCamera cam = rs.getCamera();
        player.setPosition(new Vector2(cam.position.x, cam.position.y));
        logger.info("travel: moved player to camera center at ({}, {})", cam.position.x, cam.position.y);
        return true;
    }

    private static boolean equalsIgnoreCaseTrim(String a) {
        return a != null && a.trim().equalsIgnoreCase("center");
    }
    private static boolean isNumeric(String s) { return parseFloat(s) != null; }
    private static Float parseFloat(String s) {
        if (s == null) return null;
        try { return Float.parseFloat(s.trim()); } catch (Exception e) { return null; }
    }
}
