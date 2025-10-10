package com.csse3200.game.files;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.files.FileLoader.MapEntitySpec;
import com.csse3200.game.files.RegistryEntityPlacer.SpawnHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistryEntityPlacerTest {

    private static MapEntitySpec spec(String name, String type, int x, int y) {
        return new MapEntitySpec(name, type, new GridPoint2(x, y));
    }

    /* -------------------- happy paths -------------------- */

    @Test
    void place_exactMatch_callsRegisteredHandler() {
        SpawnHandler handler = mock(SpawnHandler.class);
        SpawnHandler fallback = mock(SpawnHandler.class);
        var placer = new RegistryEntityPlacer(fallback).register("player", handler);

        var s = spec("p1", "player", 3, 4);
        placer.place(s);

        verify(handler).spawn("p1", "player", new GridPoint2(3, 4));
        verifyNoInteractions(fallback);
    }

    @Test
    void place_caseInsensitive_whenRegisteredWithRegisterCi() {
        SpawnHandler handler = mock(SpawnHandler.class);
        SpawnHandler fallback = mock(SpawnHandler.class);
        var placer = new RegistryEntityPlacer(fallback).registerCi("npc", handler);

        // Try a few case variants
        placer.place(spec("n1", "NPC", 0, 1));
        placer.place(spec("n2", "nPc", 2, 3));

        verify(handler).spawn("n1", "NPC", new GridPoint2(0, 1));
        verify(handler).spawn("n2", "nPc", new GridPoint2(2, 3));
        verifyNoInteractions(fallback);
    }

    @Test
    void place_usesFallback_whenNoHandler() {
        SpawnHandler fallback = mock(SpawnHandler.class);
        var placer = new RegistryEntityPlacer(fallback);

        var s = spec("x", "unknownType", 10, 20);
        placer.place(s);

        verify(fallback).spawn("x", "unknownType", new GridPoint2(10, 20));
    }

    @Test
    void register_replacesExistingHandler() {
        SpawnHandler first = mock(SpawnHandler.class);
        SpawnHandler second = mock(SpawnHandler.class);
        SpawnHandler fallback = mock(SpawnHandler.class);

        var placer = new RegistryEntityPlacer(fallback)
                .register("tree", first)     // initial
                .register("tree", second);   // replace

        var s = spec("t1", "tree", 5, 6);
        placer.place(s);

        verifyNoInteractions(first);
        verify(second).spawn("t1", "tree", new GridPoint2(5, 6));
        verifyNoInteractions(fallback);
    }

    /* -------------------- guard rails -------------------- */

    @Test
    void ctor_nullFallback_throwsNpe() {
        assertThrows(NullPointerException.class, () -> new RegistryEntityPlacer(null));
    }

    @Test
    void register_nullType_throwsNpe() {
        var placer = new RegistryEntityPlacer((n, t, g) -> {
        });
        assertThrows(NullPointerException.class, () -> placer.register(null, (n, t, g) -> {
        }));
    }

    @Test
    void register_nullHandler_throwsNpe() {
        var placer = new RegistryEntityPlacer((n, t, g) -> {
        });
        assertThrows(NullPointerException.class, () -> placer.register("player", null));
    }

    @Test
    void registerCi_nullHandlerOrType_throwsNpe_likeRegister() {
        var placer = new RegistryEntityPlacer((n, t, g) -> {
        });
        assertThrows(NullPointerException.class, () -> placer.registerCi(null, (n, t, g) -> {
        }));
        assertThrows(NullPointerException.class, () -> placer.registerCi("npc", null));
    }

    /* -------------------- integration-ish sanity -------------------- */

    @Test
    void place_passesThroughOriginalArgs_verbatim() {
        SpawnHandler handler = mock(SpawnHandler.class);
        var placer = new RegistryEntityPlacer((n, t, g) -> {
        }).register("Boss", handler);

        var gp = new GridPoint2(9, 9);
        var s = new MapEntitySpec("B-01", "Boss", gp); // reuse same instance to ensure equality
        placer.place(s);

        verify(handler).spawn("B-01", "Boss", gp);
    }

    @Test
    void defaultCtor_usesLoggingFallback_and_knownHandlersStillWork() {
        AtomicBoolean handled = new AtomicBoolean(false);
        RegistryEntityPlacer.SpawnHandler knownHandler = (n, t, g) -> handled.set(true);

        var placer = new RegistryEntityPlacer().register("known", knownHandler);

        assertDoesNotThrow(() -> placer.place(new FileLoader.MapEntitySpec("u1", "UNKNOWN", new GridPoint2(1, 2))));
        assertFalse(handled.get(), "Fallback should not call handlers for other types");

        placer.place(new FileLoader.MapEntitySpec("k1", "known", new GridPoint2(0, 0)));
        assertTrue(handled.get(), "Registered handler should be invoked for 'known'");
    }

}
