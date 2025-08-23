package com.csse3200.game.components.player;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(GameExtension.class)
class InventoryComponentTest {

  @Test
  void shouldGetInventory() {
    InventoryComponent inventory = new InventoryComponent(100);
    assertFalse(inventory.isFull(), "Starting inventory isn't full");
    assertTrue(inventory.isEmpty(), "Starting inventory is empty");
    assertEquals(new ArrayList<String>(), inventory.getInventory(),
            "Starting inventory is empty");

    inventory.addItem("Monster Rehab Wild Berry Tea");
    assertEquals(new ArrayList<>(List.of("Monster Rehab Wild Berry Tea")),
            inventory.getInventory(),
            "Inventory of 1 item holds 1 item");
    assertFalse(inventory.isEmpty());
    assertFalse(inventory.isFull());
    assertEquals(1, inventory.getInventory().size());

    for (int idx = 0; idx < 4; idx++) {
      inventory.addItem("item:" + idx);
    }
    ArrayList <String> items = new ArrayList<>(
            List.of("Monster Rehab Wild Berry Tea", "item:0", "item:1", "item:2", "item:3"));
    assertEquals(items, inventory.getInventory(), "Returns full inventory");
  }

  @Test
  void shouldGetItem() {
    InventoryComponent inventory = new InventoryComponent(100);

  }

  @Test shouldRemoveItem() { }

  @Test
  void shouldSetGetGold() {
    InventoryComponent inventory = new InventoryComponent(100);
    assertEquals(100, inventory.getGold());

    inventory.setGold(150);
    assertEquals(150, inventory.getGold());

    inventory.setGold(-50);
    assertEquals(0, inventory.getGold());
  }

  @Test
  void shouldCheckHasGold() {
    InventoryComponent inventory = new InventoryComponent(150);
    assertTrue(inventory.hasGold(100));
    assertFalse(inventory.hasGold(200));
  }

  @Test
  void shouldAddGold() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addGold(-500);
    assertEquals(0, inventory.getGold());

    inventory.addGold(100);
    inventory.addGold(-20);
    assertEquals(80, inventory.getGold());
  }
}
