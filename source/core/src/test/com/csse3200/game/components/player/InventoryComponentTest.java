package com.csse3200.game.components.player;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class InventoryComponentTest {
  private int processor = 100;

  @Nested
  @DisplayName("Testing: addItem, setItem, get, getInventory, remove, isFull, isEmpty")
  class inventoryMethodsTest {
    private int processor = 100;
    ArrayList<Entity> testInven;
    InventoryComponent inventory;

    @BeforeEach
    void setup() {
      testInven = new ArrayList<Entity>();
      for (int idx = 0; idx < 5; idx++) {
        testInven.add(null);
      }
      inventory = new InventoryComponent(this.processor);
    }

    @Test
    void testGetInventoryEmpty() {
      for (int idx = 0; idx < 5; idx++) {
        assertNull(inventory.get(idx));
      }

      assertFalse(inventory.isFull(), "Starting inventory isn't full");
      assertTrue(inventory.isEmpty(), "Starting inventory is empty");
      assertEquals(testInven, inventory.getInventory(), "Starting inventory is empty");
      assertEquals(0, inventory.getSize());
    }

    @Test
    void testGetInventorySingleItem() {
      Entity item = new Entity();
      inventory.addItem(item);
      testInven.set(0, item);

      assertSame(testInven.get(0), inventory.getInventory().get(0),
              "Inventory of 1 item holds 1 item");
      for (int idx = 1; idx < 5; idx++) {
        assertNull(inventory.getInventory().get(idx),
                String.format("Slot %d is null (\'empty\')", idx));
      }
      assertFalse(inventory.isEmpty());
      assertFalse(inventory.isFull());
      assertEquals(5, inventory.getInventory().size(), "inventory size doesn't increase");
      assertEquals(1, inventory.getSize());
    }

    @Test
    void testGetInventoryFullInventory() {
      Entity[] testSet = new Entity[5];
      for (int idx = 0; idx < 5; idx++) {
        testSet[idx] = new Entity();
        inventory.addItem(testSet[idx]);
        testInven.set(idx, testSet[idx]);
      }

      // check full inventory
      for (int idx = 0; idx < 5; idx++) {
          assertSame(testInven.get(idx), inventory.getInventory().get(idx),
                  "Returns full inventory");
      }
      assertTrue(inventory.isFull());
      assertFalse(inventory.isEmpty());
      assertEquals(5, inventory.getSize());

      for (int idx = 0; idx < 5; idx++) {
          assertNotNull(inventory.get(idx));
      }
    }

    @Test
    void shouldGet() {
      Entity[] testSet = new Entity[5];
      for (int idx = 0; idx < 5; idx++) {
        assertNull(inventory.get(idx), String.format("pos: %d in inventory is Empty", idx));
      }

      for (int idx = 0; idx < 5; idx++) {
        testSet[idx] = new Entity();
        inventory.addItem(testSet[idx]);
        assertSame(testSet[idx], inventory.get(idx),
                String.format("inventory should have an \'item\' with an id of: %d", idx));
      }

      assertNull(inventory.get(6), "Invalid index returns null");
      assertNull(inventory.get(5), "Invalid index returns null");
      assertNull(inventory.get(-1), "Invalid index returns null");
      assertNotNull(inventory.get(4), "Valid index returns an Entity");
      assertNotNull(inventory.get(0), "Valid index returns an Entity");
    }

    @Test
    void shouldRemoveItem() {
      for (int idx = 0; idx < 5; idx++) {
        inventory.addItem(new Entity());
        testInven.set(idx, new Entity());
      }

      assertTrue(inventory.remove(0), "Removing works");
      assertTrue(inventory.remove(4), "Removing works");
      assertFalse(inventory.remove(-1), "Removing doesn't work on an invalid index");
      assertFalse(inventory.remove(5), "Removing doesn't work on an invalid index");

      assertNull(inventory.get(0), "Removed item is replaced with null");
      assertEquals(3, inventory.getSize(), "Inventory should contain 2 Entities");

      assertTrue(inventory.remove(1), "Removing works");
      assertTrue(inventory.remove(2), "Removing works");
      assertTrue(inventory.remove(3), "Removing works");

      assertEquals(0, inventory.getSize(), "Inventory should be empty");
    }

    @Test
    void shouldSetItem() {
      Entity item1 = new Entity();
      assertTrue(inventory.setItem(0, item1), "Successfully set first item");
      assertEquals(1, inventory.getSize(), "Size should be 1 after adding first item");
      assertEquals(item1, inventory.get(0), "First item should be in slot 0");

      Entity item2 = new Entity();
      assertTrue(inventory.setItem(3, item2), "Successfully set second item");
      assertEquals(2, inventory.getSize(), "Size should be 2 after adding second item");
      assertEquals(item2, inventory.get(3), "Second item in slot 3");

      Entity item3 = new Entity();
      Entity item4 = new Entity();
      Entity item5 = new Entity();
      assertTrue(inventory.setItem(1, item3), "Should set item3");
      assertTrue(inventory.setItem(2, item4), "Should set item4");
      assertTrue(inventory.setItem(4, item5), "Should set item5");

      assertEquals(5, inventory.getSize(), "Should have 5 items");
      assertTrue(inventory.isFull(), "Inventory should be full");

      assertEquals(item1, inventory.get(0), "Item1 in slot 0") ;
      assertEquals(item2, inventory.get(3), "Item2 in slot 3") ;
      assertEquals(item3, inventory.get(1), "Item3 in slot 1") ;
      assertEquals(item4, inventory.get(2), "Item4 in slot 2") ;
      assertEquals(item5, inventory.get(4), "Item5 in slot 4") ;

    }

    @Test
    void shouldAddItem() {
      Entity item1 = new Entity();
      assertTrue(inventory.addItem(item1), "Should successfully add first item");
      assertEquals(1, inventory.getSize(), "Size should be 1 after adding first item");
      assertEquals(item1, inventory.get(0), "First item should be in slot 0");

      Entity item2 = new Entity();
      assertTrue(inventory.addItem(item2), "Should successfully add second item");
      assertEquals(2, inventory.getSize(), "Size should be 2 after adding second item");
      assertEquals(item2, inventory.get(1), "Second item should be in slot 1");

      Entity item3 = new Entity();
      Entity item4 = new Entity();
      Entity item5 = new Entity();
      assertTrue(inventory.addItem(item3), "Should add third item");
      assertTrue(inventory.addItem(item4), "Should add fourth item");
      assertTrue(inventory.addItem(item5), "Should add fifth item");

      assertEquals(5, inventory.getSize(), "Should have 5 items");
      assertTrue(inventory.isFull(), "Inventory should be full");
    }

  }

  @Test
  void shouldSetGetProcessor() {
    InventoryComponent inventory = new InventoryComponent(100);
    assertEquals(100, inventory.getProcessor());

    inventory.setProcessor(150);
    assertEquals(150, inventory.getProcessor());

    inventory.setProcessor(-50);
    assertEquals(0, inventory.getProcessor());
  }

  @Test
  void shouldCheckHasProcessor() {
    InventoryComponent inventory = new InventoryComponent(150);
    assertTrue(inventory.hasProcessor(100));
    assertFalse(inventory.hasProcessor(200));
  }

  @Test
  void shouldAddProcessor() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addGold(-500);
    assertEquals(0, inventory.getProcessor());

    inventory.addGold(100);
    inventory.addGold(-20);
    assertEquals(80, inventory.getProcessor());
  }
}
