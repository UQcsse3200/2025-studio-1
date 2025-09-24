package com.csse3200.game.areas;

import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class AreaRouter {
    private final Map<String, BiFunction<TerrainFactory, CameraComponent, GameArea>> makers = new HashMap<>();

    public AreaRouter register(String id,
                               BiFunction<TerrainFactory, CameraComponent, GameArea> maker) {
        if (makers.putIfAbsent(id, maker) != null) {
            throw new IllegalStateException("Area id already registered: " + id);
        }
        return this;
    }

    /** Register by class; id inferred from simple name minus trailing "GameArea". */
    public AreaRouter register(Class<? extends GameArea> areaClass) {
        String id = infer(areaClass);
        return register(id, (tf, cam) -> {
            try {
                return areaClass.getConstructor(TerrainFactory.class, CameraComponent.class)
                        .newInstance(tf, cam);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can't construct " + areaClass.getSimpleName(), e);
            }
        });
    }

    /** Varargs */
    @SafeVarargs
    public final AreaRouter registerAll(Class<? extends GameArea>... areas) {
        for (var c : areas) register(c);
        return this;
    }

    /** Collection */
    public AreaRouter registerAll(Collection<Class<? extends GameArea>> areas) {
        for (var c : areas) register(c);
        return this;
    }

    public GameArea create(String id, TerrainFactory tf, CameraComponent cam) {
        var maker = makers.get(id);
        if (maker == null) throw new IllegalArgumentException("Unknown area id: " + id);
        return maker.apply(tf, cam);
    }

    private static String infer(Class<?> c) {
        String s = c.getSimpleName();
        return s.endsWith("GameArea") ? s.substring(0, s.length() - "GameArea".length()) : s;
    }
}

