package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that setRoomSpawn(...) updates the static playerSpawn used by areas
 * so doorway-dependent spawns take effect.
 */
class PlayerSpawnConfigTest {

    private static void assertPlayerSpawnUpdated(Class<?> areaClass, GridPoint2 newSpawn) throws Exception {
        // Call the public static setRoomSpawn on the area class
        areaClass.getMethod("setRoomSpawn", GridPoint2.class).invoke(null, newSpawn);

        // Read the static playerSpawn field via reflection
        Field f = areaClass.getDeclaredField("playerSpawn");
        f.setAccessible(true);

        // Ensure it's not final so updates can take effect
        assertFalse(Modifier.isFinal(f.getModifiers()), areaClass.getSimpleName() + ": playerSpawn should be mutable");

        GridPoint2 actual = (GridPoint2) f.get(null);
        assertNotNull(actual, areaClass.getSimpleName() + ": playerSpawn should not be null");
        assertEquals(newSpawn.x, actual.x, areaClass.getSimpleName() + ": x mismatch");
        assertEquals(newSpawn.y, actual.y, areaClass.getSimpleName() + ": y mismatch");
    }

    @Test
    void forestUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(ForestGameArea.class, new GridPoint2(24, 8));
    }

    @Test
    void receptionUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(Reception.class, new GridPoint2(6, 10));
    }

    @Test
    void mainHallUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(MainHall.class, new GridPoint2(8, 8));
    }

    @Test
    void officeUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(OfficeGameArea.class, new GridPoint2(27, 22));
    }

    @Test
    void researchUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(ResearchGameArea.class, new GridPoint2(6, 8));
    }

    @Test
    void securityUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(SecurityGameArea.class, new GridPoint2(24, 20));
    }

    @Test
    void shippingUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(ShippingGameArea.class, new GridPoint2(24, 8));
    }

    @Test
    void serverUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(ServerGameArea.class, new GridPoint2(25, 24));
    }

    @Test
    void tunnelUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(TunnelGameArea.class, new GridPoint2(4, 8));
    }

    @Test
    void storageUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(StorageGameArea.class, new GridPoint2(26, 20));
    }

    @Test
    void movingBossRoomUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(MovingBossRoom.class, new GridPoint2(2, 14));
    }

    @Test
    void flyingBossRoomUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(FlyingBossRoom.class, new GridPoint2(6, 8));
    }

    @Test
    void staticBossRoomUpdatesSpawn() throws Exception {
        assertPlayerSpawnUpdated(StaticBossRoom.class, new GridPoint2(26, 8));
    }
}


