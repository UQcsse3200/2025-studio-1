package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.player.PlayerEquipComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <p>
 *  These tests validate the behavior of the component when equipping,
 *  replacing, and updating item entities relative to a player entity.
 *  A {@link FakeEntity} test double is used in place of the real {@link Entity}
 *  to avoid framework dependencies and to simplify position/dispose checks.
 *  </p>
 */

public class PlayerEquipComponentTest {
    /**
     * A simple test double for {@link Entity} that stores its position
     * and tracks whether it has been disposed.
     */
    static class FakeEntity extends Entity {
        private Vector2 position = new Vector2();
        boolean disposed = false;

        /**
         * Constructs a fake entity with an initial position
         * @param initialPosition the starting position of this entity
         */
        FakeEntity(Vector2 initialPosition) {
            setPosition(initialPosition);
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public void setPosition(Vector2 pos) {
            this.position = pos.cpy();
        }

        @Override
        public void dispose() {
            disposed = true;
        }
    }

    @Test
    /**
     * Ensures when no item is equipped update method does not throw
     * an exception or attempt to move an item.
     */
    void testInitialState_NoItemEquipped() {
        PlayerEquipComponent equip = new PlayerEquipComponent();
        FakeEntity player = new FakeEntity(new Vector2(5, 5));
        equip.setEntity(player);

        // No item set yet, update should not throw
        assertDoesNotThrow(equip::update);
    }

    @Test
    /**
     * Verifies that equipping an item positions it correctly
     * relative to the player's current position and the given offset.
     */
    void testSetItem_UpdatesPositionCorrectly() {
        PlayerEquipComponent equip = new PlayerEquipComponent();
        FakeEntity player = new FakeEntity(new Vector2(5, 5));
        equip.setEntity(player);

        FakeEntity item = new FakeEntity(new Vector2());
        equip.setItem(item, new Vector2(2, 3));

        equip.update();
        assertEquals(new Vector2(7, 8), item.getPosition());
    }

    @Test
    /**
     * Ensures that when a new item is equipped, the previously
     * equipped item is properly disposed.
     */
    void testReplaceItem_DisposesOldItem() {
        PlayerEquipComponent equip = new PlayerEquipComponent();
        FakeEntity player = new FakeEntity(new Vector2(0, 0));
        equip.setEntity(player);

        FakeEntity oldItem = new FakeEntity(new Vector2());
        equip.setItem(oldItem, new Vector2(1, 1));

        FakeEntity newItem = new FakeEntity(new Vector2());
        equip.setItem(newItem, new Vector2(2, 2));

        assertTrue(oldItem.disposed, "Old item should have been disposed");
    }

    @Test
    /**
     * Confirms that the equipped item follows the player's position
     * across multiple updates, adjusting each frame based on offset.
     */
    void testUpdateMovesItemEveryFrame() {
        PlayerEquipComponent equip = new PlayerEquipComponent();
        FakeEntity player = new FakeEntity(new Vector2(10, 10));
        equip.setEntity(player);

        FakeEntity item = new FakeEntity(new Vector2());
        equip.setItem(item, new Vector2(-1, -1));

        equip.update();
        assertEquals(new Vector2(9, 9), item.getPosition());

        // Move player
        player.setPosition(new Vector2(20, 5));
        equip.update();
        assertEquals(new Vector2(19, 4), item.getPosition());
    }
}
