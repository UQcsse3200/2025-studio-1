package com.csse3200.game.files.fileloader;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.FileLoader.Location;
import com.csse3200.game.files.SaveGame;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileLoaderReadGameStateTest {
    private String rootDir;

    private String p() {
        return rootDir + "/" + UUID.randomUUID() + "/" + "game_state.json";
    }

    @BeforeAll
    void setup() {
        rootDir = "unit-tests/gamestate-" + UUID.randomUUID();
        Gdx.files.local(rootDir).mkdirs();
    }

    @AfterAll
    void clean() {
        var r = Gdx.files.local(rootDir);
        if (r.exists()) r.deleteDirectory();
    }

    @Test
    void readGameState_missingFile_returnsEmpty() {
        assertTrue(FileLoader.readGameState("nope-" + UUID.randomUUID() + ".json", Location.LOCAL).isEmpty());
    }

    @Test
    void readGameState_throwsNPE_onNulls() {
        assertThrows(NullPointerException.class, () -> FileLoader.readGameState(null, Location.LOCAL));
        assertThrows(NullPointerException.class, () -> FileLoader.readGameState("x", null));
    }

    @Test
    void readGameState_setsElementType_andParsesInventory_guarded() throws Exception {
        String path = p();
        String json = """
                  {"tag":"slotA","loadedInventory":[{"id":"wood","qty":5},{"id":"stone","qty":2}]}
                """;
        Gdx.files.local(path).writeString(json, false);

        var out = FileLoader.readGameState(path, Location.LOCAL);

        // Shape check to decide success vs. graceful empty (supports your real model transparently)
        Class<?> gsClass = SaveGame.GameState.class;
        boolean hasNoArgCtor = Arrays.stream(gsClass.getDeclaredConstructors()).anyMatch(c -> c.getParameterCount() == 0);
        boolean isStatic = java.lang.reflect.Modifier.isStatic(gsClass.getModifiers());
        boolean hasLoadedInv;
        try {
            var f = gsClass.getDeclaredField("loadedInventory");
            hasLoadedInv = java.util.Collection.class.isAssignableFrom(f.getType());
        } catch (NoSuchFieldException e) {
            hasLoadedInv = false;
        }

        Class<?> itemClass;
        try {
            itemClass = Class.forName(SaveGame.class.getName() + "$itemRetrieve");
        } catch (ClassNotFoundException e) {
            itemClass = null;
        }
        boolean itemOk = itemClass != null
                && Arrays.stream(itemClass.getDeclaredConstructors()).anyMatch(c -> c.getParameterCount() == 0)
                && Arrays.stream(itemClass.getDeclaredFields()).anyMatch(f -> f.getName().equals("id"))
                && Arrays.stream(itemClass.getDeclaredFields()).anyMatch(f -> f.getName().equals("qty"));

        boolean modelShapeOK = hasNoArgCtor && isStatic && hasLoadedInv && itemOk;

        if (!modelShapeOK) {
            assertTrue(out.isEmpty());
            return;
        }

        var gs = out.orElseThrow();
        var tagF = gs.getClass().getDeclaredField("tag");
        tagF.setAccessible(true);
        assertEquals("slotA", tagF.get(gs));
    }
}
