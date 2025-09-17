package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.utils.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling boosted outgoing weapon damage (cheat code).
 */
public class DamageMultiplierCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(DamageMultiplierCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        // validate arg count
        if (args == null || args.size() != 1) return false;

        // parse + validate multiplier
        String raw = args.get(0).trim();
        final float multiplier;
        try {
            multiplier = Float.parseFloat(raw);
        } catch (NumberFormatException e) {
            return false; // non-numeric
        }
        if (!Float.isFinite(multiplier) || multiplier < 0f) {
            return false; // NaN/Inf/negative
        }

        // fetch services only after validation
        var es = ServiceLocator.getEntityService();
        if (es == null) {
            // choose behaviour; false = can't apply
            return false;
        }

        var entities = es.getEntities();
        if (entities == null || entities.size == 0) {
            // parsed fine; nothing to update
            return true;
        }

        setDamageMultiplier(entities, multiplier);
        return true;
    }

    boolean isValid(ArrayList<String> args) { return args.size() == 1; }

    void setDamageMultiplier(com.badlogic.gdx.utils.Array<Entity> entityList, float value) {
        if (entityList == null) return; // <-- null-safe
        for (Entity entity : entityList) {
            var comp = entity.getComponent(WeaponsStatsComponent.class);
            if (comp != null) {
                comp.setDamageMultiplier(value);
                logger.info("Damage multiplier set to {} for entity {}", value, entity);
            }
        }
    }
}