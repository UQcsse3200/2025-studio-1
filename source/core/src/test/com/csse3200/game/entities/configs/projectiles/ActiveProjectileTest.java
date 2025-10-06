package com.csse3200.game.entities.configs.projectiles;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ActiveProjectileTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActiveProjectileTest {
    ActiveProjectile activeProjectile;

    @BeforeEach
    public void setup() {
        activeProjectile = new ActiveProjectile();
    }

    @Test
    void shouldSetGravityStrength() throws NoSuchFieldException, IllegalAccessException {
        activeProjectile.setGravityStrength(10f);
        assertEquals(10f, getPrivateMember(activeProjectile, "gravityStrength"));
    }

    @Test
    void shouldSetTarget() throws NoSuchFieldException, IllegalAccessException {
        Entity player = new Entity();
        activeProjectile.setTarget(player);
        assertEquals(player, getPrivateMember(activeProjectile, "target"));
    }

    @Test
    void shouldSetActiveProjectileType() throws NoSuchFieldException, IllegalAccessException {
        activeProjectile.setActiveProjectileType(ActiveProjectileTypes.ARC);
        assertEquals(ActiveProjectileTypes.ARC, getPrivateMember(activeProjectile, "activeProjectileType"));
    }

    @Test
    void shouldSetProjectileSpeed() throws NoSuchFieldException, IllegalAccessException {
        activeProjectile.setProjectileSpeed(10f);
        assertEquals(10f, getPrivateMember(activeProjectile, "projectileSpeed"));
    }

    /**
     * Gets the private member with the given name
     * The name must be one of
     * ["gravityStrength", "target", "activeProjectileType", "projectileSpeed"]
     *
     * @param component An initialised PlayerEquipComponent
     * @param name      The string of the name of the member that is wanted
     * @return The value that the private member is holding
     * @throws NoSuchFieldException   If the name given does not exist
     * @throws IllegalAccessException If the field attempting to retrieve is static
     */
    private Object getPrivateMember(ActiveProjectile component, String name)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = ActiveProjectile.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(component);
    }
}
