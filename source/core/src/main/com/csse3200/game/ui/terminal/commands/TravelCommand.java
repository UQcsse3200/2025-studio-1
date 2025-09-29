package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Travel command (progress-gated). Same surface usage as teleport but enforces that
 * you may only travel to rooms you have already discovered.
 *
 * Usage:
 *  - travel                 -> move player to camera center
 *  - travel center          -> same as above
 *  - travel <x> <y>         -> move player to world coordinates (floats)
 *  - travel <RoomName>      -> transition to another room/area by name, if discovered
 */
public class TravelCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(TravelCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        GameArea area = ServiceLocator.getGameArea();
        if (area == null) {
            logger.warn("travel: no active GameArea");
            return false;
        }

        EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.warn("travel: EntityService not registered");
            return false;
        }

        Entity player = ServiceLocator.getPlayer();
        if (player == null) player = findPlayer(es.getEntities());
        if (player == null) {
            logger.warn("travel: player not found");
            return false;
        }

        // center
        if (args == null || args.isEmpty() || (args.size() == 1 && equalsIgnoreCaseTrim(args.get(0), "center"))) {
            return toCameraCenter(player);
        }

        // coordinates
        if (args.size() == 2 && isNumeric(args.get(0)) && isNumeric(args.get(1))) {
            float x = Float.parseFloat(args.get(0));
            float y = Float.parseFloat(args.get(1));
            player.setPosition(new Vector2(x, y));
            logger.info("travel: moved player to ({}, {})", x, y);
            return true;
        }

        // area name (gated)
        if (args.size() == 1 && !isNumeric(args.get(0))) {
            String requested = args.get(0).trim();
            DiscoveryService ds = ServiceLocator.getDiscoveryService();
            if (ds == null) {
                logger.warn("travel: DiscoveryService missing; cannot verify progression");
                return false;
            }
            if (!ds.isDiscovered(requested)) {
                logger.info("travel: '{}' not discovered yet. Discovered={} ", requested, ds.getDiscovered());
                return false;
            }
            boolean ok = area.transitionToArea(requested);
            if (ok) {
                logger.info("travel: transitioning to discovered area '{}'", requested);
            } else {
                logger.warn("travel: unknown area '{}'", requested);
            }
            return ok;
        }

        logger.debug("travel: invalid args {}. Usage: travel | travel center | travel <x> <y> | travel <RoomName>", args);
        return false;
    }

    private boolean toCameraCenter(Entity player) {
        RenderService rs = ServiceLocator.getRenderService();
        if (rs == null || rs.getCamera() == null) {
            logger.warn("travel: RenderService or camera not available");
            return false;
        }
        OrthographicCamera cam = rs.getCamera();
        Vector2 target = new Vector2(cam.position.x, cam.position.y);
        player.setPosition(target);
        logger.info("travel: moved player to camera center at ({}, {})", target.x, target.y);
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

