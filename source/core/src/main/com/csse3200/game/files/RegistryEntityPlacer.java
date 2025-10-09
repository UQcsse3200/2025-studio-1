package com.csse3200.game.files;

import com.badlogic.gdx.math.GridPoint2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pattern-matching entity placer. Register handlers per "type" string.
 * When LevelPatternLoader iterates entities, we look up and invoke the handler.
 */
public final class RegistryEntityPlacer implements LevelPatternLoader.EntityPlacer {
    private static final Logger log = LoggerFactory.getLogger(RegistryEntityPlacer.class);
    private final Map<String, SpawnHandler> handlers = new ConcurrentHashMap<>();
    private final SpawnHandler fallback;
    public RegistryEntityPlacer() {
        this((name, type, grid) ->
                log.atWarn().setMessage("No handler for type={} (entity={}, at={})")
                        .addArgument(type).addArgument(name).addArgument(grid).log());
    }

    public RegistryEntityPlacer(SpawnHandler fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    /**
     * Register/replace a handler for a given type (case-sensitive).
     */
    public RegistryEntityPlacer register(String type, SpawnHandler handler) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(handler, "handler");
        handlers.put(type, handler);
        return this;
    }

    /**
     * Case-insensitive convenience registration.
     */
    public RegistryEntityPlacer registerCi(String type, SpawnHandler handler) {
        return register(type.toLowerCase(), handler);
    }

    @Override
    public void place(FileLoader.MapEntitySpec spec) {
        var key = spec.type();
        var h = handlers.get(key);
        if (h == null) {
            // try case-insensitive if not found
            h = handlers.get(key.toLowerCase());
        }
        if (h == null) h = fallback;
        h.spawn(spec.name(), spec.type(), spec.location());
    }

    @FunctionalInterface
    public interface SpawnHandler {
        void spawn(String name, String type, GridPoint2 grid);
    }
}
