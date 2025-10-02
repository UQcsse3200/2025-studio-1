package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PlayerEquipComponentTest {
    PlayerEquipComponent component;

    @BeforeEach
    public void setup() {
        component = new PlayerEquipComponent();
    }

    @Test
    public void checkInitialiseTest() throws NoSuchFieldException, IllegalAccessException {
        Entity item = (Entity) getPrivateMember(component, "item");
        Vector2 offset = (Vector2) getPrivateMember(component, "offset");
        assertNull(item);
        assertEquals(new Vector2(), offset);
    }

    @Test
    public void setNotNullTest() throws NoSuchFieldException, IllegalAccessException {
        Entity testItem = new Entity();
        Vector2 testOffset = new Vector2(1f, 2f);
        component.setItem(testItem, new Vector2(1f, 2f));

        assertEquals(getPrivateMember(component, "item"), testItem);
        assertEquals(getPrivateMember(component, "offset"), testOffset);
    }

    @Test
    public void setNullTest() throws NoSuchFieldException, IllegalAccessException {
        Entity player = new Entity().addComponent(component);

        Entity testItem = mock(Entity.class);
        Vector2 testOffset = new Vector2(1f, 2f);

        component.setItem(testItem, testOffset);
        component.setItem(null, testOffset);

        assertNotEquals(getPrivateMember(component, "item"), testItem);
        assertNull(getPrivateMember(component, "item"));
    }

    /**
     * Gets the private member with the given name -> must be "item" or "offset"
     *
     * @param component An initialised PlayerEquipComponent
     * @param name      The string of the name of the member that is wanted
     * @return The value that the private member is holding
     * @throws NoSuchFieldException   If the name given does not exist
     * @throws IllegalAccessException If the field attempting to retrieve is static
     */
    private Object getPrivateMember(PlayerEquipComponent component, String name)
            throws NoSuchFieldException,  IllegalAccessException {
        Field field = PlayerEquipComponent.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(component);
    }
}
