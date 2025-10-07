package com.csse3200.game.ui.terminal.commands.util;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;

/** Utilities to resolve/find a player entity for terminal commands. */
public final class CommandPlayers {
    private CommandPlayers() {}

    /** Prefer the globally-registered player, otherwise find the first keyboard-controlled entity. */
    public static Entity resolve(EntityService es) {
        Entity p = ServiceLocator.getPlayer();
        if (p != null) {
            return p;
        }

        return findKeyboardControlled(es != null ? es.getEntities() : null);
    }

    /** Finds the first entity with KeyboardPlayerInputComponent (index loop avoids non-reentrant iterator). */
    public static Entity findKeyboardControlled(Array<Entity> entities) {
        if (entities == null) return null;
        for (int i = 0, n = entities.size; i < n; i++) {
            Entity e = entities.get(i);
            if (e.getComponent(KeyboardPlayerInputComponent.class) != null) {
                return e;
            }
        }
        return null;
    }
}
